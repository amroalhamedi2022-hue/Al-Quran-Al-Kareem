"""Offline gallery tests use placeholder bytes, never Quran text."""
import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from make_review_gallery import make_gallery, OFFICIAL_SHA

class ReviewGalleryTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.rows = []
        for page in range(1, 4):
            name = f"{page:03d}.png"
            payload = ("NOT_A_QURAN_IMAGE_%d" % page).encode()
            (self.root / name).write_bytes(payload)
            self.rows.append({"page": page, "file": name,
                              "sha256": hashlib.sha256(payload).hexdigest(),
                              "review_status": "PENDING"})
        self.meta = {"status": "UNAPPROVED_RENDER_ONLY", "source_sha256": OFFICIAL_SHA,
                     "page_count": 3, "pages": self.rows}
        self.write_report()

    def tearDown(self):
        self.temp.cleanup()

    def write_report(self):
        (self.root / "UNAPPROVED_RENDER_REPORT.json").write_text(json.dumps(self.meta))

    def test_produces_offline_only_review_ui(self):
        html = make_gallery(self.root, count=3).read_text("utf-8")
        self.assertIn("001.png", html)
        self.assertIn("HUMAN_REVIEW_RECORD_ONLY_NOT_APPROVAL", html)
        self.assertIn("localStorage", html)
        self.assertIn('default-src \'none\'', html)
        self.assertNotIn("<script src=", html)
        self.assertNotIn("fetch(", html)

    def test_no_missing_page_accepted(self):
        (self.root / "002.png").unlink()
        with self.assertRaisesRegex(ValueError, "missing"):
            make_gallery(self.root, count=3)

    def test_no_wrong_publisher_source(self):
        self.meta["source_sha256"] = "f" * 64
        self.write_report()
        with self.assertRaisesRegex(ValueError, "not verified"):
            make_gallery(self.root, count=3)

    def test_no_automatic_approval_status(self):
        self.meta["pages"][1]["review_status"] = "APPROVED"
        self.write_report()
        with self.assertRaisesRegex(ValueError, "Bad unapproved page"):
            make_gallery(self.root, count=3)

if __name__ == "__main__":
    unittest.main()
