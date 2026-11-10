"""Synthetic metadata tests, never real Quranic content."""
import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from verify_approval_seal import ApprovalError, verify_seal

class ApprovalSealTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory()
        root=Path(self.temp.name)
        self.manifest=root/"approved_manifest.json"
        self.kotlin=root/"QuranAssetVerifier.kt"
        self.meta={"approved_by":["reviewer one","reviewer two"],"rights_reviewed":True,
                   "pages":[{"page":n} for n in range(1,605)]}
        self.manifest.write_text(json.dumps(self.meta))
        self.good_hash=hashlib.sha256(self.manifest.read_bytes()).hexdigest()
    def tearDown(self):
        self.temp.cleanup()
    def pin(self,digest):
        self.kotlin.write_text('private const val INDEPENDENTLY_APPROVED_MANIFEST_SHA256 = "'+digest+'"')
    def test_valid_synthetic_seal(self):
        self.pin(self.good_hash)
        self.assertEqual(verify_seal(self.kotlin,self.manifest),self.good_hash)
    def test_no_pin_fails_closed(self):
        self.pin("")
        with self.assertRaisesRegex(ApprovalError,"not set"):
            verify_seal(self.kotlin,self.manifest)
    def test_mismatch_fails_closed(self):
        self.pin("0"*64)
        with self.assertRaisesRegex(ApprovalError,"not identical"):
            verify_seal(self.kotlin,self.manifest)
    def test_fake_single_reviewer_fails(self):
        self.meta["approved_by"]=["same","same"]
        self.manifest.write_text(json.dumps(self.meta))
        self.pin(hashlib.sha256(self.manifest.read_bytes()).hexdigest())
        with self.assertRaisesRegex(ApprovalError,"two independent"):
            verify_seal(self.kotlin,self.manifest)

if __name__=="__main__":
    unittest.main()
