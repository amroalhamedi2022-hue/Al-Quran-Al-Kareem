"""Regression tests for the immutable signed-update identity and release guard."""
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app/build.gradle.kts"
ACTIVITY = ROOT / "app/src/main/java/app/alquran/kareem/MainActivity.kt"
VERIFIER = ROOT / "app/src/main/java/app/alquran/kareem/QuranPublisherSourceVerifier.kt"
WORKFLOW = ROOT / ".github/workflows/build-stable-signed-update.yml"

class StableReleaseContractTests(unittest.TestCase):
    def test_only_one_production_application_id(self):
        gradle = APP.read_text()
        self.assertIn('applicationId = "app.alquran.kareem"',gradle)
        self.assertNotIn("applicationIdSuffix",gradle)
        self.assertIn("versionCode = 100",gradle)
        self.assertIn('versionName = "1.0.0"',gradle)

    def test_source_verification_is_release_capable_not_debug_only(self):
        app = ACTIVITY.read_text()
        verifier = VERIFIER.read_text()
        self.assertIn("QuranPublisherSourceVerifier.check(assets)",app)
        self.assertIn("QuranAssetVerifier.Status.SourceVerified",app)
        self.assertNotIn("BuildConfig.DEBUG",verifier)
        self.assertIn("source_provenance_manifest.json",verifier)
        self.assertIn("604",verifier)

    def test_release_key_must_be_private_and_constant(self):
        workflow = WORKFLOW.read_text()
        for name in ("QURAN_RELEASE_KEYSTORE_B64","QURAN_RELEASE_STORE_PASSWORD",
                     "QURAN_RELEASE_KEY_ALIAS","QURAN_RELEASE_KEY_PASSWORD",
                     "QURAN_RELEASE_CERT_SHA256"):
            self.assertIn("secrets."+name,workflow)
        self.assertIn("apksigner",workflow)
        self.assertIn("certificate changed",workflow)
        self.assertNotIn("keytool -genkey",workflow)

    def test_signed_release_checks_real_quran_content_and_no_network(self):
        workflow = WORKFLOW.read_text()
        self.assertIn("scripts/audit_rendered_pages.py",workflow)
        self.assertIn("scripts/stage_publisher_source.py",workflow)
        self.assertIn("scripts/verify_publisher_release_apk.py",workflow)
        self.assertIn("assembleRelease",workflow)
        self.assertIn("aapt2",workflow)
        self.assertIn("android.permission.(INTERNET",workflow)
        self.assertIn("app.alquran.kareem' versionCode='100'",workflow)

    def test_old_preview_artifact_generator_is_retired(self):
        self.assertFalse((ROOT/".github/workflows/build-debug-source-preview.yml").exists())

    def test_release_blocks_unreviewed_quran_before_signing(self):
        workflow = WORKFLOW.read_text()
        self.assertIn("No independently approved 604-page manifest", workflow)
        self.assertIn("scripts/verify_approval_seal.py", workflow)
        self.assertIn("scripts/verify_quran_pack.py", workflow)
        approval_gate = workflow.index("Block signed Quran release until independent 604-page content approval")
        signing_step = workflow.index("Sign with unchanged lifetime key")
        self.assertLess(approval_gate, signing_step)
        self.assertIn("exit 1", workflow[approval_gate:signing_step])

if __name__ == "__main__":
    unittest.main()
