"""Reviewer consistency tests use fake SHA strings and no Quran content."""
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from verify_review_records import check_records, ReviewRecordError, PIN, REVIEW_STATUS

class ReviewRecordTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        root = Path(self.tmp.name)
        self.source = root / "unapproved_source.json"
        self.files = [root / "reviewer1.json", root / "reviewer2.json"]
        self.origin = {
            "status": "UNAPPROVED_RENDER_ONLY", "source_sha256": PIN, "page_count": 3,
            "pages": [{"page": n, "file": f"{n:03d}.png",
                       "sha256": ("%064x" % n), "review_status": "PENDING"}
                      for n in range(1, 4)]
        }
        self.reviews = []
        for name in ["Independent One", "Independent Two"]:
            self.reviews.append({
                "status": REVIEW_STATUS, "source_sha256": PIN, "expectedPages": 3,
                "reviewer": name, "exportedUtc": "2026-11-10T12:00:00.000Z",
                "records": [{"page": n, "sha256": ("%064x" % n), "checked": True,
                             "flagged": False, "lastUpdatedUtc": "2026-11-10T12:00:00.000Z"}
                            for n in range(1, 4)]
            })
        self.write()

    def tearDown(self):
        self.tmp.cleanup()

    def write(self):
        self.source.write_text(json.dumps(self.origin))
        for file, body in zip(self.files, self.reviews):
            file.write_text(json.dumps(body))

    def validate(self):
        return check_records(self.source, self.files, count=3)

    def test_two_complete_records_do_not_approve_release(self):
        result = self.validate()
        self.assertEqual(result["reviewed_page_count"], 3)
        self.assertFalse(result["automatic_quran_approval"])
        self.assertFalse(result["release_allowed"])

    def test_missing_check_is_blocking(self):
        self.reviews[0]["records"][2]["checked"] = False
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "not confirmed"):
            self.validate()

    def test_flagged_page_is_blocking(self):
        self.reviews[1]["records"][1]["flagged"] = True
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "still flagged"):
            self.validate()

    def test_duplicate_reviewer_is_blocking(self):
        self.reviews[1]["reviewer"] = "independent one"
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "Duplicate reviewer"):
            self.validate()

    def test_changed_page_sha_is_blocking(self):
        self.reviews[0]["records"][0]["sha256"] = "0"*64
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "hash/order mismatch"):
            self.validate()

    def test_missing_page_entry_is_blocking(self):
        self.reviews[0]["records"].pop()
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "Incomplete review"):
            self.validate()

    def test_false_approval_status_is_blocking(self):
        self.reviews[0]["status"] = "APPROVED"
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "Wrong review report"):
            self.validate()

    def test_missing_page_timestamp_is_blocking(self):
        del self.reviews[1]["records"][0]["lastUpdatedUtc"]
        self.write()
        with self.assertRaisesRegex(ReviewRecordError, "timestamp"):
            self.validate()

if __name__ == "__main__":
    unittest.main()
