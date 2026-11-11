"""Static UI contract tests, complementary to Android JVM tests.

We cannot prove physical device system-bar rendering in unit CI. These checks
lock the safe-inset placement so future edits cannot accidentally return the
reader buttons or Quran image to navigation-bar-covered regions.
"""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
ACTIVITY=ROOT/"app/src/main/java/app/alquran/kareem/MainActivity.kt"

class ReaderSafeLayoutContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.code=ACTIVITY.read_text(encoding="utf-8")

    def test_reader_uses_safe_system_window_insets(self):
        self.assertIn("import androidx.compose.foundation.layout.safeDrawingPadding",self.code)
        self.assertIn(".background(MaterialTheme.colorScheme.background)\n                .safeDrawingPadding()",self.code)

    def test_theme_does_not_repaint_printed_quran_page(self):
        self.assertIn("Box(modifier.padding(3.dp).background(Color.White)",self.code)
        self.assertIn("contentScale = ContentScale.Fit",self.code)

    def test_reader_still_shows_controls_outside_printed_page(self):
        self.assertIn("AnimatedVisibility(visible = showControls)",self.code)
        self.assertIn("ReaderControls(",self.code)

if __name__ == "__main__":
    unittest.main()
