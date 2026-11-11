"""Synthetic debug APK image-byte audit tests (no Quran text/images)."""
import hashlib, json, sys, tempfile, unittest
from pathlib import Path
from zipfile import ZipFile
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from verify_debug_source_preview_apk import audit_preview_apk, PreviewApkError

class DebugPreviewApkTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.root=Path(self.tmp.name)
        self.source=self.root/"source";self.source.mkdir()
        self.apk=self.root/"example.apk"
        self.data={}
        for n in range(1,4):
            name=f"{n:03d}.png"
            raw=f"FAKE_PICTURE_{n}".encode()
            self.data[name]=raw
            (self.source/name).write_bytes(raw)
        self.report={"status":"UNAPPROVED_RENDER_ONLY","page_count":3,
                     "pages":[{"page":n,"file":f"{n:03d}.png",
                               "sha256":hashlib.sha256(self.data[f"{n:03d}.png"]).hexdigest(),
                               "review_status":"PENDING"} for n in range(1,4)]}
        self.write()
    def tearDown(self):self.tmp.cleanup()
    def write(self,extra=None,modified=None):
        manifest=json.dumps(self.report).encode()
        (self.source/"source_preview_manifest.json").write_bytes(manifest)
        with ZipFile(self.apk,"w") as z:
            z.writestr("assets/quran/source_preview_manifest.json",manifest)
            for name,payload in self.data.items():
                z.writestr("assets/quran/"+name,modified if modified is not None and name=="002.png" else payload)
            if extra: z.writestr("assets/quran/extra.txt",b"not allowed")
    def audit(self):return audit_preview_apk(self.apk,self.source,count=3)
    def test_same_byte_originals_pass_only_as_preview(self):
        result=self.audit()
        self.assertEqual(result["pages_checked"],3)
        self.assertFalse(result["release_allowed"])
        self.assertFalse(result["human_quran_approval"])
    def test_tampered_packaged_image_refused(self):
        self.write(modified=b"tampered")
        with self.assertRaisesRegex(PreviewApkError,"APK pixel bytes"):
            self.audit()
    def test_extra_embedded_content_refused(self):
        self.write(extra=True)
        with self.assertRaisesRegex(PreviewApkError,"Missing/extra"):
            self.audit()
    def test_missing_embedded_page_refused(self):
        with ZipFile(self.apk,"w") as z:
            z.writestr("assets/quran/source_preview_manifest.json",(self.source/"source_preview_manifest.json").read_bytes())
            z.writestr("assets/quran/001.png",self.data["001.png"])
        with self.assertRaisesRegex(PreviewApkError,"Missing/extra"):
            self.audit()
    def test_approval_claim_refused(self):
        self.report["status"]="APPROVED"
        self.write()
        with self.assertRaisesRegex(PreviewApkError,"falsely approved"):
            self.audit()
    def test_mutated_staged_image_refused(self):
        (self.source/"001.png").write_bytes(b"not original")
        with self.assertRaisesRegex(PreviewApkError,"Unexpected staged"):
            self.audit()
if __name__=="__main__":unittest.main()
