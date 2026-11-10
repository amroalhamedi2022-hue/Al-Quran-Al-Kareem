"""Only fake 1x1 pixel test images, never Quranic content."""
import hashlib
import json
import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from stage_verified_release_assets import AssetAssemblyError, assemble

def chunk(name, data):
    return struct.pack(">I", len(data)) + name + data + struct.pack(">I", zlib.crc32(name + data) & 0xffffffff)

def fake_png(number: int):
    header = struct.pack(">IIBBBBB", 1, 1, 8, 2, 0, 0, 0)
    image = zlib.compress(bytes([0, number, number, number]))
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", image) + chunk(b"IEND", b"")

class VerifiedAssemblyTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.src = self.root / "artifact"; self.src.mkdir()
        self.dst = self.root / "quran"; self.dst.mkdir()
        self.meta = {
            "rights_reviewed": True, "riwaya": "Hafs 'an Asim",
            "layout": "Madinah 604-page", "approved_by": ["Reviewer A", "Reviewer B"],
            "pages": []
        }
        for number in range(1,4):
            name=f"{number:03d}.png"
            payload = fake_png(number)
            (self.src/name).write_bytes(payload)
            self.meta["pages"].append({"page":number,"file":name,
                                         "sha256":hashlib.sha256(payload).hexdigest()})
        self.manifest=self.dst/"approved_manifest.json"
        self.write_manifest()

    def tearDown(self):
        self.temp.cleanup()

    def write_manifest(self):
        self.manifest.write_text(json.dumps(self.meta),encoding="utf-8")

    def run_assemble(self):
        return assemble(self.src,self.dst,self.manifest,count=3)

    def test_valid_fake_pictures_copy_exact_bytes(self):
        expected=[(self.src/f"{n:03d}.png").read_bytes() for n in range(1,4)]
        self.assertEqual(self.run_assemble()["pages"],3)
        self.assertEqual(expected,[(self.dst/f"{n:03d}.png").read_bytes() for n in range(1,4)])

    def test_missing_approval_manifest_never_stages(self):
        self.manifest.unlink()
        with self.assertRaisesRegex(AssetAssemblyError,"NO RELEASE"):
            self.run_assemble()
        self.assertFalse((self.dst/"001.png").exists())

    def test_changed_png_rejected_before_any_copy(self):
        (self.src/"002.png").write_bytes(fake_png(9))
        with self.assertRaisesRegex(AssetAssemblyError,"SHA-256 differs"):
            self.run_assemble()
        self.assertFalse((self.dst/"001.png").exists())

    def test_missing_page_rejected_before_copy(self):
        (self.src/"002.png").unlink()
        with self.assertRaisesRegex(AssetAssemblyError,"missing"):
            self.run_assemble()

    def test_unknown_artifact_file_rejected(self):
        (self.src/"unexpected.bin").write_bytes(b"unsafe")
        with self.assertRaisesRegex(AssetAssemblyError,"unexpected"):
            self.run_assemble()

    def test_only_documented_unapproved_sidecars_allowed(self):
        (self.src/"REVIEW_OFFLINE_UNAPPROVED.html").write_text("TEST")
        (self.src/"UNAPPROVED_RENDER_REPORT.json").write_text("{}")
        self.assertEqual(self.run_assemble()["pages"],3)
        self.assertFalse((self.dst/"REVIEW_OFFLINE_UNAPPROVED.html").exists())

    def test_untrusted_reviewers_rejected(self):
        self.meta["approved_by"]=["fake","fake"]
        self.write_manifest()
        with self.assertRaisesRegex(AssetAssemblyError,"independent reviewers"):
            self.run_assemble()

    def test_wrong_edition_rejected(self):
        self.meta["riwaya"]="other"
        self.write_manifest()
        with self.assertRaisesRegex(AssetAssemblyError,"edition"):
            self.run_assemble()

    def test_existing_page_prevents_unsafe_overwrite(self):
        (self.dst/"001.png").write_bytes(b"old")
        with self.assertRaisesRegex(AssetAssemblyError,"only"):
            self.run_assemble()

if __name__=="__main__":
    unittest.main()
