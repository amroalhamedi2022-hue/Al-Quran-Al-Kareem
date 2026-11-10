import hashlib
import io
import json
import sys
import tempfile
import unittest
from pathlib import Path
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from audit_rendered_pages import audit_source, RasterAuditError, PIN

class RasterAuditTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.rows = []
        for n in range(1, 4):
            image = Image.new("RGBA", (12, 18), (255, 255, 255, 0))
            draw = ImageDraw.Draw(image)
            draw.rectangle((2, 2, 9, 15), fill=(0, 0, 0, 255))
            if n == 2:
                draw.point((3, 3), fill=(255, 0, 0, 255))
            if n == 3:
                draw.point((4, 4), fill=(0, 0, 255, 255))
            output = io.BytesIO()
            image.save(output, format="PNG")
            payload = output.getvalue()
            (self.root / f"{n:03d}.png").write_bytes(payload)
            self.rows.append({"page": n, "file": f"{n:03d}.png",
                              "sha256": hashlib.sha256(payload).hexdigest(), "review_status": "PENDING"})
        self.meta = {"status": "UNAPPROVED_RENDER_ONLY", "source_sha256": PIN,
                     "page_count": 3, "pages": self.rows}
        self.write_meta()

    def tearDown(self):
        self.temp.cleanup()

    def write_meta(self):
        (self.root / "UNAPPROVED_RENDER_REPORT.json").write_text(json.dumps(self.meta))

    def audit(self):
        return audit_source(self.root, count=3, expected_dimensions=(12, 18))

    def test_valid_synthetic_pages(self):
        self.assertEqual(self.audit()["pages_examined"], 3)

    def test_modified_png_rejected(self):
        (self.root / "002.png").write_bytes((self.root / "001.png").read_bytes())
        with self.assertRaisesRegex(RasterAuditError, "hash mismatch"):
            self.audit()

    def test_missing_page_rejected(self):
        (self.root / "001.png").unlink()
        with self.assertRaisesRegex(RasterAuditError, "missing"):
            self.audit()

    def test_extra_file_rejected(self):
        (self.root / "extra.txt").write_text("a")
        with self.assertRaisesRegex(RasterAuditError, "extra"):
            self.audit()

    def test_false_approval_status_rejected(self):
        self.meta["status"] = "APPROVED"
        self.write_meta()
        with self.assertRaisesRegex(RasterAuditError, "provenance"):
            self.audit()

    def test_wrong_source_hash_rejected(self):
        self.meta["source_sha256"] = "a" * 64
        self.write_meta()
        with self.assertRaisesRegex(RasterAuditError, "provenance"):
            self.audit()

    def test_full_canvas_cropping_rejected(self):
        image = Image.new("RGBA", (12, 18), (0, 0, 0, 255))
        image.save(self.root / "002.png")
        self.meta["pages"][1]["sha256"] = hashlib.sha256((self.root / "002.png").read_bytes()).hexdigest()
        self.write_meta()
        with self.assertRaisesRegex(RasterAuditError, "border"):
            self.audit()

    def test_wrong_dimensions_rejected(self):
        image = Image.new("RGBA", (13, 18), (0, 0, 0, 255))
        image.save(self.root / "002.png")
        self.meta["pages"][1]["sha256"] = hashlib.sha256((self.root / "002.png").read_bytes()).hexdigest()
        self.write_meta()
        with self.assertRaisesRegex(RasterAuditError, "dimensions"):
            self.audit()
