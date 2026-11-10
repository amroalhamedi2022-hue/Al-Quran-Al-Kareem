# Official content verification without requiring the app owner to inspect 604 pages

**Date:** 2026-11-10 | **State:** Publisher verification pathway IDENTIFIED, not submitted or granted.

## 1. Already authenticated source
- Official publisher: King Fahd Glorious Quran Printing Complex (KFGQPC), Madinah.
- The original Hafs 1441H Illustrator archive contains 604 original AI page artworks.
- Official archive SHA-256: `280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef`.
- Source and permitted software use: https://dm.qurancomplex.gov.sa/ and https://dm.qurancomplex.gov.sa/rights/
- CI downloaded original bytes (SHA verified), rendered 604 pages and verified all PNG hashes. Separate CI runs rendered **identical PNG bytes** on all 604 pages: https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42830167631

**Technical equivalence to an original archive does NOT certify the Android display: even a deterministic renderer can consistently omit a glyph.**

## 2. Additional independent source for automated comparison
- The publisher itself also hosts a Hafs printed Mushaf PDF, `standard39-2.pdf`, at:
  https://qurancomplex.gov.sa/wp-content/uploads/isdarat/hafs/standard39-2.pdf
- **UNVERIFIED HERE:** PDF page mapping and whether its printing edition/ornamentation precisely correspond to the 1441H vector release. Do not assume 1:1 page numbers or approve mismatches automatically.
- Recommended separate audit: parse page count, register matching page artwork, compare independently rendered PDF pages to archived AI→PNG images using normalized registration and pixel/line-level difference detection. Report outliers, and compare any differences with the authoritative source. **OCR/text reconstruction must never be used to alter Quran.** Record software versions and exact source hashes. This audit has not yet been executed.
- Both sources ultimately come from the same publisher; cross-comparison is a powerful render-integrity screen, but not two independent scholarly approvals.

## 3. Publisher certification/evaluation is the preferred alternative to asking user to proofread 604 pages
The Complex has historically maintained a digital Quran research and review unit authorized to examine electronic Quran editions and issue digital validation certificates. Its **current intake procedure and availability must be requested**; neither a new certification nor a service-level promise is currently confirmed.

Supporting publisher/official references:
- Saudi Press Agency background: https://www.spa.gov.sa/1400799
- Official published technology guide: https://qurancomplex.gov.sa/wp-content/uploads/isdarat/booklets/complextechguide.pdf
- Official publisher contact: https://qurancomplex.gov.sa/contact/
- Contact address: `info@qurancomplex.gov.sa`

### Proposed requested evaluation:
1. Confirm that the exact 1441H Hafs original AI vector archive is suitable for offline app distribution and whether existing content approval can cover a bitwise-verified conversion.
2. Advise if the publisher supplies already validated 604 PNG/WebP page assets suitable for Android, avoiding additional conversion risks.
3. Request the method for the Complex's current electronic Mushaf app review/certification (if still available).
4. Submit the exact 604 PNG hashes, publisher source hash, reproducibility logs, proposed app APK and offline rendering tests once the reader app is testable.
5. Obtain the published written decision, conditions, edition details and approved asset hashes **before labeling the app approved**.

## 4. No-work-for-owner assurance
The app owner is NOT being asked to manually review 604 pages. Engineering can continue independently. A qualified external publisher review or other credible documented independent review is required before a misleading public claim of proofread perfection. The official publisher may accept source provenance instead of asking for another full review, but **we cannot assume this until confirmed in writing**.

## 5. Release locks unchanged
- `approved_manifest.json` not yet present; reviewer records / official certification not yet obtained.
- `QuranAssetVerifier.kt` independent manifest SHA remains blank.
- No production Quran APK is approved, signed, or released.
- Image/render integrity and publisher review are separate gates.
