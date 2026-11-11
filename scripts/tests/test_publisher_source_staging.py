"""Debug preview gate tests use tiny generated colored pixels, NEVER Quran text."""
import hashlib
import json
import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from stage_publisher_source import stage_publisher_source, PublisherSourceError, PIN, REPORT, SOURCE_MANIFEST

def chunk(kind,payload):
    return struct.pack(">I",len(payload))+kind+payload+struct.pack(">I",zlib.crc32(kind+payload)&0xffffffff)

def test_image(n):
    return (b"\x89PNG\r\n\x1a\n" +
        chunk(b"IHDR", struct.pack(">IIBBBBB",1,1,8,2,0,0,0)) +
        chunk(b"IDAT",zlib.compress(bytes([0,n,n,n])))+
        chunk(b"IEND",b""))

class PublisherSourceStagingTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory()
        self.base=Path(self.temp.name)
        self.src=self.base/"original"; self.src.mkdir()
        self.dest=self.base/"build"/"quran"
        self.rows=[]
        for n in range(1,4):
            name=f"{n:03d}.png"
            raw=test_image(n)
            (self.src/name).write_bytes(raw)
            self.rows.append({"page":n,"file":name,"sha256":hashlib.sha256(raw).hexdigest(),
                              "width":1,"height":1,"review_status":"PENDING"})
        self.report={"status":"UNAPPROVED_RENDER_ONLY","source_sha256":PIN,
                     "page_count":3,"pages":self.rows}
        self.write_report()
    def tearDown(self): self.temp.cleanup()
    def write_report(self):
        (self.src/REPORT).write_text(json.dumps(self.report))
    def execute(self):
        return stage_publisher_source(self.src,self.dest,count=3,dimensions=(1,1))

    def test_valid_source_only_builds_unapproved_preview(self):
        result=self.execute()
        self.assertTrue(result["source_integrity_verified"])
        self.assertFalse(result["institutional_certification"])
        self.assertFalse(result["bytes_modified"])
        self.assertEqual(len(list(self.dest.glob("*.png"))),3)
        self.assertTrue((self.dest/SOURCE_MANIFEST).exists())
        self.assertFalse((self.dest/"approved_manifest.json").exists())

    def test_no_changed_pixels(self):
        expected={p.name:p.read_bytes() for p in self.src.glob("*.png")}
        self.execute()
        self.assertEqual(expected,{p.name:p.read_bytes() for p in self.dest.glob("*.png")})

    def test_tampered_image_is_rejected_before_copy(self):
        (self.src/"002.png").write_bytes(test_image(7))
        with self.assertRaisesRegex(PublisherSourceError,"Source image mismatch"):
            self.execute()
        self.assertFalse(self.dest.exists())

    def test_no_missing_page(self):
        (self.src/"003.png").unlink()
        with self.assertRaisesRegex(PublisherSourceError,"Unknown, missing"):
            self.execute()

    def test_no_wrong_source_provenance(self):
        self.report["source_sha256"]="0"*64; self.write_report()
        with self.assertRaisesRegex(PublisherSourceError,"pinned"):
            self.execute()

    def test_no_claim_of_approval_accepted(self):
        self.report["status"]="APPROVED"; self.write_report()
        with self.assertRaisesRegex(PublisherSourceError,"pinned"):
            self.execute()

    def test_no_auto_approved_page(self):
        self.rows[0]["review_status"]="APPROVED"; self.write_report()
        with self.assertRaisesRegex(PublisherSourceError,"metadata"):
            self.execute()

    def test_no_extra_unexpected_files(self):
        (self.src/"unauthorized.bin").write_bytes(b"unexpected")
        with self.assertRaisesRegex(PublisherSourceError,"extra"):
            self.execute()

    def test_no_replacement_of_existing_android_assets(self):
        self.dest.mkdir(parents=True)
        (self.dest/"approved_manifest.json").write_text("must not overwrite")
        with self.assertRaisesRegex(PublisherSourceError,"refuse overwrite"):
            self.execute()

if __name__=="__main__":
    unittest.main()
