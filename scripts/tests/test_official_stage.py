"""Strict synthetic ZIP fixtures only. No verses or Quran source art in tests."""
import hashlib
import io
import sys
import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from stage_official_hafs import StagingError, examine_archive, stage

def fixture_blob(omit=None, extra=None):
    buffer = io.BytesIO()
    with ZipFile(buffer, "w") as bundle:
        for n in range(1, 605):
            if n == omit:
                continue
            bundle.writestr(f"1441-AI-hafs/{n:03d}___Hafs39__DM.ai", f"UNIT-TEST-ONLY-{n}".encode())
        if extra:
            bundle.writestr(*extra)
    return buffer.getvalue()

class OfficialStageTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.archive = self.root / "fixture.zip"

    def tearDown(self):
        self.temp.cleanup()

    def inspect(self, data, expected=None):
        self.archive.write_bytes(data)
        return examine_archive(self.archive, expected or hashlib.sha256(data).hexdigest())

    def test_604_unique_ordered_pages(self):
        pages, digest = self.inspect(fixture_blob())
        self.assertEqual(len(pages), 604)
        self.assertIn(604, pages)
        self.assertEqual(len(digest), 64)

    def test_wrong_official_sha_rejected(self):
        with self.assertRaisesRegex(StagingError, "SHA256 mismatch"):
            self.inspect(fixture_blob(), "0" * 64)

    def test_missing_page_rejected(self):
        with self.assertRaisesRegex(StagingError, "Missing Quran artwork pages"):
            self.inspect(fixture_blob(omit=32))

    def test_duplicate_page_rejected(self):
        with self.assertRaisesRegex(StagingError, "Duplicate or invalid"):
            self.inspect(fixture_blob(extra=("duplicate/001___Hafs39__DM.ai", b"fake")))

    def test_path_traversal_rejected(self):
        with self.assertRaisesRegex(StagingError, "Unsafe ZIP path"):
            self.inspect(fixture_blob(extra=("../escape.txt", b"evil")))

    def test_wrong_edition_rejected(self):
        with self.assertRaisesRegex(StagingError, "Unknown AI artwork entry"):
            self.inspect(fixture_blob(extra=("extra/008___Warsh11__DM.ai", b"fake")))

    def test_stage_does_not_produce_approved_manifest(self):
        data = fixture_blob()
        self.archive.write_bytes(data)
        report = stage(self.archive, self.root / "out", hashlib.sha256(data).hexdigest())
        self.assertEqual(report["page_count"], 604)
        self.assertEqual(len(list((self.root / "out").glob("*.ai"))), 604)
        self.assertFalse((self.root / "out" / "approved_manifest.json").exists())
        self.assertTrue((self.root / "out" / "SOURCE_STAGE_ONLY.json").exists())

    def test_stage_nonempty_directory_rejected(self):
        data = fixture_blob()
        self.archive.write_bytes(data)
        dest = self.root / "out"
        dest.mkdir()
        (dest / "unrelated").write_text("safe")
        with self.assertRaisesRegex(StagingError, "must be empty"):
            stage(self.archive, dest, hashlib.sha256(data).hexdigest())

if __name__ == "__main__":
    unittest.main()
