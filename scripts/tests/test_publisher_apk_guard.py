"""Adversarial package-integrity tests, synthetic bytes only: never Quran text."""
import hashlib
import json
import sys
import tempfile
import unittest
import warnings
from pathlib import Path
from zipfile import ZipFile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from verify_publisher_release_apk import (
    OFFICIAL_HAFS_ARCHIVE_SHA256, PublisherApkError, audit_publisher_apk
)

class PublisherSignedApkIntegrityTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        root = Path(self.tmp.name)
        self.source = root / "source"
        self.source.mkdir()
        self.apk = root / "test.apk"
        self.records = []
        self.images = {}
        for n in range(1, 4):
            name = f"{n:03d}.png"
            contents = b"TEST-PNG-FIXTURE-NOT-QURAN-" + bytes([n])
            (self.source / name).write_bytes(contents)
            self.images[f"assets/quran/{name}"] = contents
            self.records.append({"page": n, "file": name,
                                 "sha256": hashlib.sha256(contents).hexdigest(),
                                 "review_status": "PENDING", "width": 1600, "height": 2287})
        self.report = {"status": "UNAPPROVED_RENDER_ONLY",
                       "source_sha256": OFFICIAL_HAFS_ARCHIVE_SHA256,
                       "page_count": 3, "pages": self.records}
        self.write_report()

    def write_report(self):
        self.report_path = self.source / "source_provenance_manifest.json"
        self.report_path.write_text(json.dumps(self.report), encoding="utf-8")

    def write_apk(self, *, extra=None, omit=(), manifest_bytes=None):
        entries = dict(self.images)
        for name in omit:
            entries.pop(name)
        if extra:
            entries.update(extra)
        entries["assets/quran/source_provenance_manifest.json"] = (
            self.report_path.read_bytes() if manifest_bytes is None else manifest_bytes
        )
        with ZipFile(self.apk, "w") as z:
            for name, data in entries.items():
                z.writestr(name, data)

    def verify(self):
        return audit_publisher_apk(self.apk, self.source, count=3)

    def test_unchanged_source_and_apk_succeed_without_claim_of_certification(self):
        self.write_apk()
        result = self.verify()
        self.assertEqual(result["pages_checked"], 3)
        self.assertTrue(result["source_integrity_verified"])
        self.assertFalse(result["institutional_certification"])

    def test_changed_apk_page_is_rejected(self):
        self.write_apk(extra={"assets/quran/002.png": b"tampered"})
        with self.assertRaisesRegex(PublisherApkError, "pixel bytes"):
            self.verify()

    def test_missing_page_is_rejected(self):
        self.write_apk(omit={"assets/quran/003.png"})
        with self.assertRaisesRegex(PublisherApkError, "Missing/extra"):
            self.verify()

    def test_extra_page_is_rejected(self):
        self.write_apk(extra={"assets/quran/004.png": b"fake"})
        with self.assertRaisesRegex(PublisherApkError, "Missing/extra"):
            self.verify()

    def test_mismatched_embedded_report_is_rejected(self):
        self.write_apk(manifest_bytes=b"{}")
        with self.assertRaisesRegex(PublisherApkError, "manifest bytes"):
            self.verify()

    def test_duplicate_zip_entry_is_rejected(self):
        self.write_apk()
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with ZipFile(self.apk, "a") as z:
                z.writestr("assets/quran/001.png", self.images["assets/quran/001.png"])
        with self.assertRaisesRegex(PublisherApkError, "duplicated"):
            self.verify()

    def test_source_page_changed_after_build_is_rejected(self):
        self.write_apk()
        (self.source / "002.png").write_bytes(b"changed-source")
        with self.assertRaisesRegex(PublisherApkError, "staged page"):
            self.verify()

    def test_unpinned_origin_sha_is_rejected(self):
        self.report["source_sha256"] = "0" * 64
        self.write_report()
        self.write_apk()
        with self.assertRaisesRegex(PublisherApkError, "unpinned"):
            self.verify()

    def test_unapproved_status_must_not_be_laundered_as_approved(self):
        self.report["status"] = "APPROVED"
        self.write_report()
        self.write_apk()
        with self.assertRaisesRegex(PublisherApkError, "falsely approved"):
            self.verify()

    def test_reordered_source_page_mapping_is_rejected(self):
        self.records[0]["file"] = "002.png"
        self.write_report()
        self.write_apk()
        with self.assertRaisesRegex(PublisherApkError, "mapping"):
            self.verify()

    def test_malformed_source_sha_is_rejected(self):
        self.records[1]["sha256"] = "not-a-hash"
        self.write_report()
        self.write_apk()
        with self.assertRaisesRegex(PublisherApkError, "digest"):
            self.verify()

    def test_wrong_page_dimensions_are_rejected(self):
        self.records[1]["height"] = 2278
        self.write_report()
        self.write_apk()
        with self.assertRaisesRegex(PublisherApkError, "dimensions"):
            self.verify()

    def test_symlinked_staged_page_is_rejected(self):
        self.write_apk()
        original = self.source / "002.png"
        copy = self.source / "outside.png"
        copy.write_bytes(original.read_bytes())
        original.unlink()
        original.symlink_to(copy)
        with self.assertRaisesRegex(PublisherApkError, "symlinked"):
            self.verify()

if __name__ == "__main__":
    unittest.main()
