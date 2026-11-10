import json
import sys
import tempfile
import unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from make_review_ledger import generate, ReviewLedgerError, EXPECTED_SOURCE

class LedgerTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.root=Path(self.tmp.name)
        self.source=self.root/'render.json'
        self.target=self.root/'review.csv'
        self.report={'status':'UNAPPROVED_RENDER_ONLY','source_sha256':EXPECTED_SOURCE,
                     'page_count':3,'pages':[{'page':n,'file':f'{n:03d}.png','sha256':str(n)*64,'review_status':'PENDING'} for n in range(1,4)]}
    def tearDown(self):self.tmp.cleanup()
    def write(self):self.source.write_text(json.dumps(self.report))
    def test_all_pending_and_no_overwrite(self):
        self.write()
        self.assertEqual(generate(self.source,self.target,3),3)
        content=self.target.read_text(encoding='utf-8-sig')
        self.assertEqual(content.count('PENDING'),6)
        with self.assertRaisesRegex(ReviewLedgerError,'overwrite'):
            generate(self.source,self.target,3)
    def test_reject_approval_claim(self):
        self.report['pages'][0]['review_status']='APPROVED';self.write()
        with self.assertRaises(ReviewLedgerError):generate(self.source,self.target,3)
    def test_reject_missing_page(self):
        self.report['pages'].pop();self.write()
        with self.assertRaisesRegex(ReviewLedgerError,'Incomplete'):
            generate(self.source,self.target,3)
    def test_reject_wrong_source(self):
        self.report['source_sha256']='0'*64;self.write()
        with self.assertRaisesRegex(ReviewLedgerError,'provenance'):
            generate(self.source,self.target,3)
if __name__=='__main__':unittest.main()
