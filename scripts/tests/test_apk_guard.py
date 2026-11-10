import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from verify_release_apk import verify_apk_assets, ApkIntegrityError
from verify_upstream_archive import verify_official_hafs_archive, UpstreamError

class ApkTests(unittest.TestCase):
    def setUp(self):
        self.t=tempfile.TemporaryDirectory()
        self.root=Path(self.t.name)
        self.files={}
        rows=[]
        for n in (1,2,3):
            fname=f"{n:03d}.png"
            raw=b"\x89PNG\r\n\x1a\nunit-fixture-"+bytes([n])
            self.files[f"assets/quran/{fname}"]=raw
            rows.append({"page":n,"file":fname,"sha256":hashlib.sha256(raw).hexdigest()})
        self.manifest=self.root/"approved_manifest.json"
        data={"rights_reviewed":True,"source_name":"TEST ONLY","source_url":"https://example.invalid",
              "riwaya":"Hafs 'an Asim","layout":"Madinah 604-page",
              "approved_by":["test A","test B"],"pages":rows}
        self.manifest.write_text(json.dumps(data))
        self.files["assets/quran/approved_manifest.json"]=self.manifest.read_bytes()
        self.apk=self.root/"test.apk"
    def tearDown(self):self.t.cleanup()
    def _write(self,files):
        with ZipFile(self.apk,"w") as z:
            for k,v in files.items():z.writestr(k,v)
    def test_apk_matches_external_independent_manifest(self):
        self._write(self.files)
        self.assertIn("Verified 3",verify_apk_assets(self.apk,self.manifest,count=3))
    def test_apk_rejects_changed_page(self):
        changed=dict(self.files)
        changed["assets/quran/002.png"]=b"tampered"
        self._write(changed)
        with self.assertRaisesRegex(ApkIntegrityError,"page mismatch"):
            verify_apk_assets(self.apk,self.manifest,count=3)
    def test_apk_rejects_modified_manifest(self):
        changed=dict(self.files)
        changed["assets/quran/approved_manifest.json"]=b'{"unapproved":true}'
        self._write(changed)
        with self.assertRaisesRegex(ApkIntegrityError,"Embedded manifest"):
            verify_apk_assets(self.apk,self.manifest,count=3)
    def test_apk_rejects_missing_page(self):
        changed=dict(self.files)
        del changed["assets/quran/003.png"]
        self._write(changed)
        with self.assertRaisesRegex(ApkIntegrityError,"missing"):
            verify_apk_assets(self.apk,self.manifest,count=3)
    def test_official_upstream_rejects_test_fixture(self):
        fake=self.root/"fake-official.zip"
        fake.write_bytes(b"definitely not the original archive")
        with self.assertRaisesRegex(UpstreamError,"Untrusted source"):
            verify_official_hafs_archive(fake)

if __name__=="__main__":unittest.main()
