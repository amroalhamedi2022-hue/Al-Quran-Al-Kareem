"""Synthetic-only adversarial tests for promotion of reviewed Quran artwork."""
import hashlib
import json
import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from promote_approved_quran_pack import (
    PINNED_PUBLISHER_ARCHIVE, PromotionError, promote_approved_pack,
    approved_pin_from_kotlin
)

def tiny_png(n: int) -> bytes:
    def chunk(kind, payload):
        return (struct.pack(">I", len(payload)) + kind + payload
                + struct.pack(">I", zlib.crc32(kind + payload) & 0xffffffff))
    ihdr = struct.pack(">IIBBBBB", 1, 1, 8, 2, 0, 0, 0)
    pixels = zlib.compress(bytes([0, n, n, n]))
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", pixels) + chunk(b"IEND", b"")

class ApprovedQuranPromotionTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        root = Path(self.tmp.name)
        self.staged = root / "staged"
        self.staged.mkdir()
        self.approved_file = root / "independent-manifest.json"
        self.kotlin = root / "QuranAssetVerifier.kt"
        pages = []
        for n in range(1,4):
            name = f"{n:03d}.png"
            raw = tiny_png(n)
            (self.staged / name).write_bytes(raw)
            pages.append({"page": n, "file": name, "sha256": hashlib.sha256(raw).hexdigest(),
                          "review_status": "PENDING", "width": 1, "height": 1})
        self.original_report = {"status": "UNAPPROVED_RENDER_ONLY",
                                "source_sha256": PINNED_PUBLISHER_ARCHIVE,
                                "page_count": 3, "pages": pages}
        self.report_path = self.staged / "source_provenance_manifest.json"
        self.write_report()
        self.approved = {
            "source_sha256": PINNED_PUBLISHER_ARCHIVE,
            "source_report_sha256": hashlib.sha256(self.report_path.read_bytes()).hexdigest(),
            "source_name": "SYNTHETIC TEST FIXTURE", "source_url": "https://example.invalid",
            "riwaya": "Hafs 'an Asim", "layout": "Madinah 604-page",
            "rights_reviewed": True, "approved_by": ["Reviewer A", "Reviewer B"],
            "pages": [{"page": p["page"], "file": p["file"], "sha256": p["sha256"]}
                      for p in pages]
        }
        self.write_approved()

    def write_report(self):
        self.report_path.write_text(json.dumps(self.original_report), encoding="utf-8")

    def write_approved(self):
        self.approved_file.write_text(json.dumps(self.approved), encoding="utf-8")
        digest = hashlib.sha256(self.approved_file.read_bytes()).hexdigest()
        self.kotlin.write_text(
            'private const val INDEPENDENTLY_APPROVED_MANIFEST_SHA256 = "'+digest+'"' ,
            encoding="utf-8"
        )
        return digest

    def execute(self, pin=None):
        return promote_approved_pack(
            self.staged, self.approved_file,
            pin if pin is not None else approved_pin_from_kotlin(self.kotlin),
            count=3, dimensions=(1, 1)
        )

    def assert_still_unapproved(self):
        self.assertTrue(self.report_path.exists())
        self.assertFalse((self.staged / "approved_manifest.json").exists())
        self.assertFalse((self.staged / ".approved_manifest.json.pending").exists())

    def test_promote_exact_page_bytes_and_only_swap_manifest(self):
        old = [(self.staged / f"{n:03d}.png").read_bytes() for n in range(1,4)]
        result = self.execute()
        self.assertEqual(result["pages"], 3)
        self.assertFalse(result["approval_generated_by_script"])
        self.assertFalse(result["independent_human_review_verified_by_script"])
        self.assertFalse(self.report_path.exists())
        self.assertEqual((self.staged / "approved_manifest.json").read_bytes(),
                         self.approved_file.read_bytes())
        self.assertEqual(old, [(self.staged / f"{n:03d}.png").read_bytes() for n in range(1,4)])
        self.assertEqual(len(list(self.staged.iterdir())), 4)

    def test_refuse_second_promotion(self):
        self.execute()
        with self.assertRaises(PromotionError):
            self.execute()

    def test_unpinned_approval_rejected_without_mutation(self):
        with self.assertRaisesRegex(PromotionError, "pinned"):
            self.execute(pin="0"*64)
        self.assert_still_unapproved()

    def test_blank_kotlin_pin_rejected(self):
        self.kotlin.write_text('private const val INDEPENDENTLY_APPROVED_MANIFEST_SHA256 = ""')
        with self.assertRaisesRegex(PromotionError, "independently pinned"):
            approved_pin_from_kotlin(self.kotlin)
        self.assert_still_unapproved()

    def test_wrong_publisher_archive_rejected(self):
        self.original_report["source_sha256"] = "f"*64
        self.write_report()
        with self.assertRaisesRegex(PromotionError, "publisher"):
            self.execute()
        self.assert_still_unapproved()

    def test_report_changed_after_review_rejected(self):
        self.original_report["pages"][0]["width"] = 12
        self.write_report()
        with self.assertRaisesRegex(PromotionError, "conversion report"):
            self.execute()
        self.assert_still_unapproved()

    def test_approved_page_hash_not_matching_source_rejected(self):
        self.approved["pages"][1]["sha256"] = "0"*64
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "differs"):
            self.execute()
        self.assert_still_unapproved()

    def test_mutated_png_rejected(self):
        (self.staged / "002.png").write_bytes(b"modified")
        with self.assertRaisesRegex(PromotionError, "Corrupt"):
            self.execute()
        self.assert_still_unapproved()

    def test_missing_page_rejected(self):
        (self.staged / "003.png").unlink()
        with self.assertRaisesRegex(PromotionError, "Missing/extra"):
            self.execute()
        self.assert_still_unapproved()

    def test_extra_page_rejected(self):
        (self.staged / "004.png").write_bytes(b"not Quran")
        with self.assertRaisesRegex(PromotionError, "Missing/extra"):
            self.execute()
        self.assert_still_unapproved()

    def test_wrong_page_order_rejected(self):
        self.approved["pages"][0]["file"] = "002.png"
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "differs"):
            self.execute()
        self.assert_still_unapproved()

    def test_bad_original_page_dimensions_rejected(self):
        self.original_report["pages"][0]["width"] = 222
        self.write_report()
        self.approved["source_report_sha256"] = hashlib.sha256(self.report_path.read_bytes()).hexdigest()
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "publisher page record"):
            self.execute()
        self.assert_still_unapproved()

    def test_false_original_approved_status_rejected(self):
        self.original_report["status"] = "APPROVED"
        self.write_report()
        self.approved["source_report_sha256"] = hashlib.sha256(self.report_path.read_bytes()).hexdigest()
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "publisher"):
            self.execute()
        self.assert_still_unapproved()

    def test_unreviewed_usage_rights_rejected(self):
        self.approved["rights_reviewed"] = False
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "usage rights"):
            self.execute()
        self.assert_still_unapproved()

    def test_duplicate_human_approval_names_rejected(self):
        self.approved["approved_by"] = ["Reviewer A", "reviewer a"]
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "Two distinct"):
            self.execute()
        self.assert_still_unapproved()

    def test_missing_publisher_binding_rejected(self):
        del self.approved["source_report_sha256"]
        self.write_approved()
        with self.assertRaisesRegex(PromotionError, "bind"):
            self.execute()
        self.assert_still_unapproved()

    def test_source_page_symlink_rejected(self):
        data = (self.staged / "001.png").read_bytes()
        original = self.staged / "001.png"
        original.unlink()
        outside = self.staged.parent / "outside.png"
        outside.write_bytes(data)
        original.symlink_to(outside)
        with self.assertRaisesRegex(PromotionError, "symlinked"):
            self.execute()
        self.assert_still_unapproved()

    def test_pending_approval_artifact_rejected(self):
        (self.staged / ".approved_manifest.json.pending").write_bytes(b"stale")
        with self.assertRaisesRegex(PromotionError, "Missing/extra"):
            self.execute()
        self.assertTrue(self.report_path.exists())
        self.assertFalse((self.staged / "approved_manifest.json").exists())
        self.assertEqual((self.staged / ".approved_manifest.json.pending").read_bytes(), b"stale")

if __name__ == "__main__":
    unittest.main()
