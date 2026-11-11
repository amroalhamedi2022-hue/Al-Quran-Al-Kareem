"""Offline navigation metadata integrity; no Quran verse bytes embedded."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SURAH = ROOT / "app/src/main/assets/metadata/surahs.json"
SECTIONS = ROOT / "app/src/main/assets/metadata/sections.json"

class OfflineQuranIndexTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.surahs = json.loads(SURAH.read_text(encoding="utf-8"))
        cls.sections = json.loads(SECTIONS.read_text(encoding="utf-8"))

    def test_all_114_surah_names_and_604_page_boundaries(self):
        m = self.surahs
        assert m["page_count"] == 604
        assert m["chapter_count"] == 114
        assert len(m["chapters"]) == 114
        self.assertEqual([x["id"] for x in m["chapters"]],list(range(1,115)))
        starts = [x["page"] for x in m["chapters"]]
        self.assertEqual(starts,sorted(starts))
        self.assertTrue(all(1<=page<=604 for page in starts))
        self.assertEqual(starts[:3],[1,2,50])
        self.assertEqual(starts[-3:],[604,604,604])

    def test_names_are_nonempty_and_verses_total(self):
        self.assertTrue(all(isinstance(x["name"],str) and x["name"].strip() for x in self.surahs["chapters"]))
        self.assertEqual(sum(x["verses"] for x in self.surahs["chapters"]),6236)

    def test_metadata_records_three_independent_source_files(self):
        s=self.surahs["sources"]
        self.assertEqual(len(s),3)
        self.assertEqual(len({x["repository"] for x in s}),3)
        self.assertTrue(all(len(x["github_blob_sha"])==40 for x in s))

    def test_30_juz_starts_in_madinah_604_pages(self):
        entries=self.sections["juz"]
        self.assertEqual(len(entries),30)
        self.assertEqual([x["number"] for x in entries],list(range(1,31)))
        self.assertEqual(entries[0]["page"],1)
        self.assertEqual(entries[1]["page"],22)
        self.assertEqual(entries[-1]["page"],582)

    def test_60_hizbs_are_monotonically_ordered(self):
        entries=self.sections["hizb"]
        self.assertEqual(len(entries),60)
        self.assertEqual([x["number"] for x in entries],list(range(1,61)))
        pages=[x["page"] for x in entries]
        self.assertEqual(pages,sorted(pages))
        self.assertTrue(all(1<=p<=604 for p in pages))

    def test_all_240_rub_el_hizb_quarters_are_present(self):
        quarters = self.sections["quarters"]
        self.assertEqual(len(quarters), 240)
        self.assertEqual([q["number"] for q in quarters], list(range(1, 241)))
        self.assertEqual(quarters[0]["page"], 1)
        self.assertEqual(quarters[-1]["page"], 599)
        pages = [q["page"] for q in quarters]
        self.assertEqual(pages, sorted(pages))

    def test_each_hizb_equals_first_verse_of_its_four_quarters(self):
        quarters = self.sections["quarters"]
        hizbs = self.sections["hizb"]
        for i, hizb in enumerate(hizbs):
            quarter = quarters[i * 4]
            for key in ("page", "surah", "ayah"):
                self.assertEqual(quarter[key], hizb[key])

    def test_source_quarter_metadata_pins_both_authoritative_inputs(self):
        source = self.sections["source"]
        self.assertEqual(len(source["quarters_xml_blob_sha"]), 40)
        self.assertEqual(len(source["quarters_page_blob_sha"]), 40)

    def test_every_section_verse_is_positive_and_bounded(self):
        for section in ("juz","hizb","quarters"):
            rows=self.sections[section]
            for item in rows:
                self.assertTrue(1<=item["surah"]<=114)
                self.assertTrue(item["ayah"]>=1)
                self.assertTrue(1<=item["page"]<=604)

if __name__=="__main__": unittest.main()
