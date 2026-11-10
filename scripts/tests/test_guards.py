import hashlib
import struct
import zlib
import json
import sys
import tempfile
import unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from verify_offline import check_manifest, check_sources, OfflineError
from verify_quran_pack import verify_pages, IntegrityError

class Guards(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.root=Path(self.tmp.name)
    def tearDown(self):
        self.tmp.cleanup()
    def test_missing_quran_is_blocked(self):
        with self.assertRaises(IntegrityError):
            verify_pages(self.root/"missing",self.root/"manifest.json")
    def _pack(self):
        pages=self.root/"pages";pages.mkdir()
        rows=[]
        for n in range(1,4):
            file=f"{n:03d}.png"
            def chunk(kind, payload):
                return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", zlib.crc32(kind + payload) & 0xffffffff)
            ihdr = struct.pack(">IIBBBBB", 1, 1, 8, 2, 0, 0, 0)
            pixels = zlib.compress(bytes([0, n, n, n]))
            data = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", pixels) + chunk(b"IEND", b"")
            (pages/file).write_bytes(data)
            rows.append({"page":n,"file":file,"sha256":hashlib.sha256(data).hexdigest()})
        manifest=self.root/"manifest.json"
        metadata={"source_name":"TEST FIXTURE ONLY", "source_url":"https://example.invalid",
                  "rights_reviewed":True,"riwaya":"Hafs 'an Asim","layout":"Madinah 604-page",
                  "approved_by":["A","B"],"pages":rows}
        manifest.write_text(json.dumps(metadata))
        return pages,manifest,metadata
    def test_page_pack_hash_integrity(self):
        pages,manifest,_=self._pack()
        self.assertIn("3 files verified",verify_pages(pages,manifest,count=3))
        (pages/"002.png").write_bytes(b"\x89PNG\r\n\x1a\ntampered")
        with self.assertRaisesRegex(IntegrityError,"Invalid PNG structure"):
            verify_pages(pages,manifest,count=3)
    def test_co_located_approved_manifest_is_allowed(self):
        pages, manifest, _ = self._pack()
        colocated = pages / "approved_manifest.json"
        colocated.write_bytes(manifest.read_bytes())
        self.assertIn("3 files verified", verify_pages(pages, colocated, count=3))

    def test_co_located_other_metadata_remains_forbidden(self):
        pages, manifest, _ = self._pack()
        colocated = pages / "approved_manifest.json"
        colocated.write_bytes(manifest.read_bytes())
        (pages / "unexpected.txt").write_text("not Quran")
        with self.assertRaisesRegex(IntegrityError, "Unexpected"):
            verify_pages(pages, colocated, count=3)

    def test_page_pack_two_independent_approvers(self):
        pages,manifest,metadata=self._pack()
        metadata["approved_by"]=["same","same"]
        manifest.write_text(json.dumps(metadata))
        with self.assertRaisesRegex(IntegrityError,"Two independent"):
            verify_pages(pages,manifest,count=3)
    def test_page_pack_order_mismatch(self):
        pages,manifest,metadata=self._pack()
        metadata["pages"][0]["file"]="002.png"
        manifest.write_text(json.dumps(metadata))
        with self.assertRaisesRegex(IntegrityError,"ordering"):
            verify_pages(pages,manifest,count=3)
    def test_page_pack_extra_file_rejected(self):
        pages,manifest,_=self._pack()
        (pages/"extra.png").write_bytes(b"x")
        with self.assertRaisesRegex(IntegrityError,"Unexpected"):
            verify_pages(pages,manifest,count=3)
    def test_offline_permission_rejection(self):
        manifest=self.root/"AndroidManifest.xml"
        manifest.write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application /></manifest>')
        self.assertTrue(check_manifest(manifest))
        manifest.write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android"><uses-permission android:name="android.permission.INTERNET"/></manifest>')
        with self.assertRaisesRegex(OfflineError,"Forbidden permission"):
            check_manifest(manifest)
    def test_offline_source_rejection(self):
        src=self.root/"src";src.mkdir()
        (src/"App.kt").write_text("package app\nclass App {}")
        self.assertEqual(check_sources(src),1)
        (src/"App.kt").write_text("val t = OkHttpClient()")
        with self.assertRaisesRegex(OfflineError,"Forbidden network"):
            check_sources(src)

if __name__=="__main__":unittest.main()
