# Source provenance & acquisition (not approved yet)

**Status 2026-11-10:** Official upstream located; assets NOT downloaded/reviewed/converted/committed in this repo. Publishing is BLOCKED until G1.

## Official priority source
- King Fahd Glorious Quran Printing Complex — digitized 604-page Madinah Mushaf, Hafs, AI vector assets: https://dm.qurancomplex.gov.sa/
- Official rights: https://dm.qurancomplex.gov.sa/rights/ (permits use in software; preserve and attribute; re-evaluate actual edition/format).
- Official published SHA-256 for the **Hafs vector download** displayed on source page as of 2026-11-10:
  `280C5D71CA16AAEEB3A343BE1B92C76FA6DF71D0671E202C026FDE12402D9EEF`
  Note: NOT a checksum for individual page PNGs. Independently verify downloadable archive before acceptance.
- Alternative official textual publishing program: https://nashr.qurancomplex.gov.sa/ — **not** automatically page source.

## Acquisition procedure
1. Record source direct download, edition and date. Download original without modification, verify official archive SHA.
2. List individual originals and confirm pages 604. Document native artboard dimensions/color profile.
3. Establish repeatable export without typography reflow or AI/OCR. Export in consistent dimensions/high quality.
4. Compare page raster with original; inspect all pages and register human approval outside the same generator pipeline.
5. Pin audited SHA-256 list. Store original private/archived for chain-of-custody; publicly publish only approved redistributed outputs.
6. If illustration or text cannot be trusted after export, source remains **unapproved**; no release.
7. Any separate index/surah/verse coordinates need their own independent provenance and review.

## No premature assertions
Website's download hash is source-advertised; NOT locally downloaded or verified.
`assets/quran/approved_manifest.json` does not exist until full approval. The application must fail closed without it.
