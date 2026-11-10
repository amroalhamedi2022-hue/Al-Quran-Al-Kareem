"""All fixtures contain invented arbitrary pixels, NEVER Quranic verses."""
import hashlib
import json
import struct
import sys
import tempfile
import unittest
import zlib
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from audit_render_archive import ArtifactAuditError, OFFICIAL_SOURCE_SHA256, audit


def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)


def png(n):
    ihdr = struct.pack(">IIBBBBB", 100, 100, 8, 6, 0, 0, 0)
    pixels = bytes([0] + [n % 256, 0, 0, 255] * 100) * 100
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(pixels)) + chunk(b"IEND", b"")


class RenderArtifactTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.path = Path(self.tmp.name) / "fixture.zip"

    def tearDown(self):
        self.tmp.cleanup()

    def fixture(self, bad_sha=False, extra=False, status="UNAPPROVED_RENDER_ONLY", swapped=False):
        rows = []
        with zipfile.ZipFile(self.path, "w") as z:
            for i in range(1, 4):
                raw = png(i)
                z.writestr(f"{i:03}.png", raw)
                rows.append({"page": i, "file": f"{i:03}.png", "width": 100, "height": 100,
                             "sha256": hashlib.sha256(raw).hexdigest(), "review_status": "PENDING"})
            if bad_sha:
                rows[1]["sha256"] = "0" * 64
            if swapped:
                rows[0]["page"] = 2
            if extra:
                z.writestr("extra.png", b"garbage")
            z.writestr("UNAPPROVED_RENDER_REPORT.json", json.dumps({"status": status,
                "source_sha256": OFFICIAL_SOURCE_SHA256, "page_count": 3, "pages": rows}))

    def test_valid_complete_synthetic_artifact(self):
        self.fixture()
        report = audit(self.path, expected_count=3)
        self.assertEqual(report["status"], "TECHNICAL_PASS_UNAPPROVED")
        self.assertTrue(report["review_required"])

    def test_hash_mismatch_rejected(self):
        self.fixture(bad_sha=True)
        with self.assertRaisesRegex(ArtifactAuditError, "SHA-256 mismatch"):
            audit(self.path, 3)

    def test_additional_files_rejected(self):
        self.fixture(extra=True)
        with self.assertRaisesRegex(ArtifactAuditError, "numbered PNGs"):
            audit(self.path, 3)

    def test_wrong_order_rejected(self):
        self.fixture(swapped=True)
        with self.assertRaisesRegex(ArtifactAuditError, "ordering"):
            audit(self.path, 3)

    def test_false_approval_rejected(self):
        self.fixture(status="APPROVED")
        with self.assertRaisesRegex(ArtifactAuditError, "Unreviewed status"):
            audit(self.path, 3)

    def test_truncated_zip_rejected(self):
        self.fixture()
        self.path.write_bytes(self.path.read_bytes()[:-40])
        with self.assertRaises(ArtifactAuditError):
            audit(self.path, 3)

    def test_missing_page_rejected(self):
        self.fixture()
        with zipfile.ZipFile(self.path, "a") as z:
            z.writestr("999.png", b"extra")
        with self.assertRaises(ArtifactAuditError):
            audit(self.path, 3)

if __name__ == "__main__":
    unittest.main()
