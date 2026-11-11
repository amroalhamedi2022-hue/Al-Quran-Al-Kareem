"""Verify offline Quran page-reference map against structured section metadata."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
DATA=ROOT/"app/src/main/assets/metadata"

class OfflineVerseMapTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.page = json.loads((DATA/"verse_page_index.json").read_text("utf-8"))
        cls.surahs=json.loads((DATA/"surahs.json").read_text("utf-8"))
        cls.sections=json.loads((DATA/"sections.json").read_text("utf-8"))

    def test_all_604_page_first_verses_are_sorted(self):
        first=self.page["first_verse_on_pages"]
        self.assertEqual(len(first),604)
        self.assertEqual([x["page"] for x in first],list(range(1,605)))
        pairs=[(x["surah"],x["ayah"]) for x in first]
        self.assertEqual(pairs,sorted(pairs))
        self.assertEqual(pairs[:2],[(1,1),(2,1)])
        self.assertEqual(pairs[-1],(112,1))

    def test_two_independent_page_map_sources_are_documented(self):
        self.assertEqual(self.page["page_count"],604)
        refs=self.page["source"]
        self.assertEqual(len(refs["first_blob_sha"]),40)
        self.assertEqual(len(refs["second_blob_sha"]),40)
        self.assertNotEqual(refs["first"],refs["second"])

    def test_page_anchors_reference_real_suras_and_ayah_counts(self):
        counts=[x["verses"] for x in self.surahs["chapters"]]
        for item in self.page["first_verse_on_pages"]:
            self.assertTrue(1 <= item["surah"] <= 114)
            self.assertTrue(1 <= item["ayah"] <= counts[item["surah"]-1])

    def test_juz_and_hizb_first_verse_maps_to_its_pinned_page(self):
        pages=self.page["first_verse_on_pages"]
        for key in ("juz","hizb"):
            for section in self.sections[key]:
                target=(section["surah"],section["ayah"])
                prior=[x["page"] for x in pages if (x["surah"],x["ayah"])<=target]
                self.assertTrue(prior)
                self.assertEqual(max(prior),section["page"])
if __name__=="__main__": unittest.main()
