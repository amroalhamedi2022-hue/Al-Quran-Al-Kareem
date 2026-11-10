# Official Hafs source — provenance and release gate

**Recorded:** 2026-11-10. **Status:** Official publication located; original archive acquisition, integrity check and page-by-page review still PENDING.

## Publisher
King Fahd Glorious Quran Printing Complex (KFGQPC), Al-Madinah Al-Munawwarah.

- Official vector-Mushaf site: https://dm.qurancomplex.gov.sa/
- Source archive (Hafs, Madinah 604-page edition, AI vector format): https://dm.qurancomplex.gov.sa/Download/1441-AI-hafs.zip
- Alternate location of identical source archive (ONLY if SHA-256 matches): https://archive.org/download/MushafMadinaHafsGreen1441/1441-AI-hafs.zip
- **Publisher's official SHA-256:** `280C5D71CA16AAEEB3A343BE1B92C76FA6DF71D0671E202C026FDE12402D9EEF`.
- Publisher's usage terms: https://dm.qurancomplex.gov.sa/rights/ — free use includes software and digital publishing, including outside Saudi Arabia, with additional commercial print restrictions.

## Verifiable acquisition path
The GitHub Action `.github/workflows/official-source-staging.yml` fetches the original archive (or archival mirror), validates the publisher-published SHA-256, checks ZIP contents and CRC, and expects exactly **604 unique `NNN___HafsXX__DM.ai` artwork files**.

The staging script `scripts/stage_official_hafs.py` extracts *original AI bytes* as `NNN.ai`, writes per-file SHA-256 and a provenance report `SOURCE_STAGE_ONLY.json`. **It never emits `approved_manifest.json` or puts unreviewed assets into the application.**

Conversion preview uses Inkscape for sample pages 001, 002, 003 and 604, with structural PNG validation. These previews are **not approved content**. Compare all output pages visually with their original source before adopting the conversion workflow.

## Exact mandatory checkpoints
- [ ] Download archive and independently verify **exact published SHA-256** and ZIP CRC.
- [ ] Confirm 604 unique files in correct order, no replacement from an unverified mirror.
- [ ] Validate that AI pages render correctly; inspect 1, 2, 3, 604 first, and any decorative page variants.
- [ ] Convert **all** 604 pages losslessly with reproducible documented settings; verify image dimensions and no cropping.
- [ ] Conduct page-by-page two-person independent human comparison with official pages: glyphs, harakat, waqf signs, ayah ornaments, page sequence, margins, decoration. Record names, date, findings and individual page hashes.
- [ ] Freeze `approved_manifest.json` with two independent signoffs and independently pin its SHA-256 for build/release gates.
- [ ] Verify actual APK's packaged pages against pinned approved manifest.
- [ ] Run disconnected Android phone/tablet tests before publishing any signed APK.

## Never confuse these terms
- Official archive SHA match = source bytes are identical to a published checksum; **not** that conversion is correct.
- PNG SHA match = bytes match an approved image; **not** that content is scholarly verified.
- Technical preview = no approval to release.
- Two author strings in JSON are not independent human certification unless backed by review records.
- The app may show `المصحف غير متاح` pending approval; never display fabricated verses or partial Qur'an.

## Distribution / credit
Attribution: `مصحف المدينة النبوية، برواية حفص عن عاصم — مجمع الملك فهد لطباعة المصحف الشريف.`
Do not mislabel the app as an official KFGQPC publication. Keep original publication and license references in the app's credits and in distribution notes.

**Current blocker:** the runtime here could not download the archive. GitHub Action is configured to try on its runner. Its result must be independently inspected; no claim of 604 imported pages or completed APK at this stage.
