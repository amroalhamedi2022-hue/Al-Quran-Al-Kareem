# Quran Safe Continuous Page Curl — architecture contract (not yet active)

The Storylens project experiments with WebGL2 + html2canvas and an inert DOM overlay. This Quran project is Kotlin/Jetpack Compose and works with 604 verified original Madinah Mushaf PNG pages. **Do not import html2canvas, WebView DOM snapshots, story text, or a JavaScript Quran renderer into the Quran app.**

## Native integration boundary

1. Keep the existing all-604-page integrity gate as the absolute prerequisite. The current QuranAssetVerifier checks original PNG bytes and order against the approved, trusted manifest. Failing a check blocks the reader and GPU.
2. In idle state, present the ORIGINAL untouched approved PNG pages; the GPU never replaces the source of truth and must not colorize, sharpen, OCR, redraw, reflow or rewrite any Quran text.
3. Future QuranPageTextureProvider returns immutable decoded bitmaps only from bytes that were just authenticated with SHA-256 against the trusted manifest. Compare the actual bytes, not an asset-supplied hash string.
4. Future NativeCurlRenderer (OpenGL ES texture mesh or equivalent) receives verified front/back textures, physical direction, normalized 0..1 drag progress and grab Y. A continuous single-mesh curl with depth, normals and antialiased paper edges is transient visual transformation only.
5. Enforce right-to-left Mushaf order, true reverse-page orientation, first/last page, portrait single-page and landscape two-page spread. A reverse-face must never render mirrored Arabic writing.
6. On gesture cancellation, window resize, unexpected Android Back, app backgrounding, GPU context loss or manifest mismatch: release transient textures, restore original verified page view, do not change current page or saved reading state.
7. No audio/recording, internet, telemetry or additional app permissions. Any new rendering code needs unit tests, screenshot comparisons and physical Xiaomi Pad 5 inspection before production; do not assert 120fps without frame traces.

## Reference implementation and limits

The inactive JavaScript prototype resides in Storylens at app/src/main/assets/quran-safe-curl-adapter.js; it recomputes SHA-256 from original encoded bytes before upload, but it is **not** a native Compose adapter and is **not** linked into this Quran project.

This document modifies no Quran verses, page PNGs, asset manifests, signing keys, package identity or live reader functions. Implementing and testing the Kotlin renderer is a separate future milestone, after the existing Quran integrity gates remain green.
