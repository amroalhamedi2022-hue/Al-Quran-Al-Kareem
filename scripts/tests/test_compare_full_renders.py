"""Non-Quran synthetic fixture checks for reproducible render comparison."""
import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from compare_full_renders import compare_folders, RenderComparisonError, SOURCE_SHA

class RenderReproducibilityTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory()
        self.root=Path(self.temp.name)
        self.left=self.root/"run_a"; self.right=self.root/"run_b"
        self.make_pack(self.left); self.make_pack(self.right)
    def tearDown(self):
        self.temp.cleanup()
    def make_pack(self,folder):
        folder.mkdir()
        rows=[]
        for n in range(1,4):
            raw=("TEST-PIXELS-NOT-QURAN-%d"%n).encode()
            (folder/f"{n:03d}.png").write_bytes(raw)
            rows.append({"page":n,"file":f"{n:03d}.png","sha256":hashlib.sha256(raw).hexdigest(),
                         "width":1600,"height":2287,"review_status":"PENDING"})
        self.write_report(folder,rows)
    def write_report(self,folder,rows,source=SOURCE_SHA):
        (folder/"UNAPPROVED_RENDER_REPORT.json").write_text(json.dumps({
            "status":"UNAPPROVED_RENDER_ONLY","source_sha256":source,
            "page_count":3,"pages":rows}))
    def test_matching_outputs_only_confirm_reproducibility(self):
        result=compare_folders(self.left,self.right,count=3)
        self.assertEqual(result["pages_compared"],3)
        self.assertFalse(result["release_authorized"])
        self.assertFalse(result["independent_human_quran_review"])
    def test_modified_image_rejected_even_with_old_report(self):
        (self.right/"002.png").write_bytes(b"DIFFERENT")
        with self.assertRaisesRegex(RenderComparisonError,"Altered PNG"):
            compare_folders(self.left,self.right,count=3)
    def test_different_valid_page_bytes_detected(self):
        data=b"ANOTHER-TEST-ONLY"
        (self.right/"002.png").write_bytes(data)
        report=json.loads((self.right/"UNAPPROVED_RENDER_REPORT.json").read_text())
        report["pages"][1]["sha256"]=hashlib.sha256(data).hexdigest()
        self.write_report(self.right,report["pages"])
        with self.assertRaisesRegex(RenderComparisonError,"Non-reproducible"):
            compare_folders(self.left,self.right,count=3)
    def test_missing_page_rejected(self):
        (self.right/"003.png").unlink()
        with self.assertRaises(RenderComparisonError):
            compare_folders(self.left,self.right,count=3)
    def test_source_mismatch_rejected(self):
        report=json.loads((self.right/"UNAPPROVED_RENDER_REPORT.json").read_text())
        self.write_report(self.right,report["pages"],source="0"*64)
        with self.assertRaisesRegex(RenderComparisonError,"pinned"):
            compare_folders(self.left,self.right,count=3)
    def test_same_folder_is_not_independent(self):
        with self.assertRaisesRegex(RenderComparisonError,"independent"):
            compare_folders(self.left,self.left,count=3)
    def test_sidecars_are_allowed_but_unknown_files_not(self):
        (self.right/"REVIEW_OFFLINE_UNAPPROVED.html").write_text("<!doctype html>")
        self.assertEqual(compare_folders(self.left,self.right,count=3)["pages_compared"],3)
        (self.right/"unexpected.png").write_bytes(b"unapproved")
        with self.assertRaisesRegex(RenderComparisonError,"file set"):
            compare_folders(self.left,self.right,count=3)

if __name__=="__main__":
    unittest.main()
