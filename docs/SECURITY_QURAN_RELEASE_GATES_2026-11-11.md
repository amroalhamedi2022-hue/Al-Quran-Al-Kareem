# Quran source integrity / signed-release gates — 2026-11-11

## Implemented in this change

- Strengthened the **actual signed APK** byte auditor in `scripts/verify_publisher_release_apk.py` with a pinned source-archive SHA-256 (Hafs, 1441H), exact PNG metadata/dimensions (1600 × 2287), valid page digests, and rejection of symlinked staged assets. The original 604-page byte-by-byte comparison against the source artifact remains intact.
- Added **13 adversarial unit tests** in `scripts/tests/test_publisher_apk_guard.py`, using non-Quran synthetic data only. Tests cover a valid comparison, mutated/missing/extra/duplicated APK assets, modified embedded report, changed source file, wrong upstream hash, falsely approved report, shuffled pages, malformed hashes, changed dimensions and symlinked files.
- Existing `.github/workflows/quality.yml` runs `python -m unittest discover -s scripts/tests -v` on PR and push; this automatically includes the new tests. CI results must be checked for the actual commit.
- **Critical safeguard:** the manual signed-release workflow now stops **before signing** if there is no independently approved 604-page manifest pinned in `QuranAssetVerifier.kt`; it checks the independent approval seal and page pack. Added a regression contract test ensuring this gate precedes the signing step.

## Verified vs NOT verified

- Verified locally in an isolated Python 3 synthetic fixture harness: **13/13 new tests passed**. This does **not** prove the downloaded 604 PNG files, Android, a signed APK or the full GitHub CI run have passed.
- GitHub CI on this PR: **pending independent verification**.
- Independent letter/diacritic/waqf/page-by-page scholarly review: **NOT COMPLETED**.
- Publisher source byte-matching alone is not a scholarly review or release approval; there is **no codepoint-by-codepoint Qur'an text verification** because current reading content is publisher-sourced PNG page artwork, not a verified text database.
- Signed APK delivery: **BLOCKED** until real 604-page content review and separate approved-manifest packaging are complete. Current source-staging still produces `source_provenance_manifest.json` marked `UNAPPROVED_RENDER_ONLY`. The approved release packaging path still needs to be wired up after signoff (the guard intentionally prevents bypass).
- Hardware visual testing and exact verse-level self-recitation coordinates: **PENDING**.

## Requirements to unblock a final signed release

1. Two qualified independent reviewers compare all 604 converted pages with the identified publisher original, covering all letters, harakat, stop marks, page boundaries and clipping. Log their independent records and address every discrepancy. Do not auto-approve through CI.
2. Generate a genuinely approved `approved_manifest.json` from the reviewed **unchanged** PNG hashes and record independent approval, source provenance and permissions; pin its exact SHA-256 in the Android approval verifier after human signoff.
3. Update the release packaging pipeline to install only the approved content pack (instead of an UNAPPROVED report), and keep the byte, archive, APK, signature, permissions and 604-page checks.
4. Execute full GitHub CI, install/test on phone and two-page tablet, and only then publish a stable signed package under `app.alquran.kareem`.

No Quran text, bitmap or original page asset is modified by this security change. No audio, microphone, AI Quran rewriting, internet access or forbidden memorization modules are introduced.
