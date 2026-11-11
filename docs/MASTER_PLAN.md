# القرآن الكريم — MASTER EXECUTION PLAN v1.1

**Updated:** 2026-11-11. **Status:** Core reader and official-source assets implemented; stable signed release pending original key and hardware QA.
**Mission:** أصالة المصحف، فخامة أدوات زجاجية تختفي، وسكينة القراءة دون تشويش. لا تضحية بسلامة النص من أجل المظهر.

## Mandatory sequence & release gates
**P0: Contracts & policy** → **P1: Authentic source & 604 immutable pages** → **P2: Offline native reader** → **P3: Self-test verse reveal** → **P4: physical page curl** → **P5: liquid glass** → **P6: basic reading utilities** → **P7: QA, review and signed release**.

Order is by dependency, not visual appeal. Any blocker in textual authenticity stops all release claims, but safe independent engineering may continue.

### P0 — Project foundation and governance
- Lock name **القرآن الكريم**, application identity and offline-only product scope.
- Record product exclusions: all audio, reciters, microphone, smart memorization, teacher, similar-verses system, online services.
- Track work in docs/PROGRESS.md with verified commit/test evidence.
- Establish source integrity and offline CI gates. External institutional certification is paused at the owner's request; never label the app certified.
**Exit G0:** documented contracts, tracked repo, automated guard tests. **Not evidence** of Quran source verification.

### P1 — Authentic Mushaf source (highest priority)
1. Obtain official Madinah Mushaf, Hafs 'an Asim, original 604-page artwork from King Fahd Complex, or a separately verified licensed source.
2. Preserve unmodified original source; record precise edition, origin URL, download date and upstream published SHA if any.
3. Confirm redistribution and derivative-export permissions; review exact license for actual file format and distribution.
4. If source is vector artwork, export losslessly to numbered pages 001.png–604.png using a documented non-editorial process. No text regeneration, AI, OCR, Unicode normalization or handwritten retyping.
5. **Completed technical checks:** source published SHA, all 604 PNG cryptographic hashes/CRC/dimensions/margins, and two independent deterministic render runs. External scholarly certification is explicitly paused by owner; do not claim it.
6. Bundle original publisher-byte-verified page PNGs from pinned upstream SHA and immutable per-page render digest report; independently re-hash all 604 pages **inside signed APK**. No retyping, OCR or generated letters. Optional formal certification can add further trust later but is not misrepresented as complete.
7. **No more separate preview applications.** Future distributable APKs use only the permanent package ID `app.alquran.kareem`, original stable signing identity and increasing versionCode.
**Exit G1:** publisher archive matches published SHA; exactly 604 original page images validated and byte-audited inside APK. Structural matching is not scholarly certification and cannot promise zero rendering defects; no such claim shall be made.

### P2 — Basic Android reader, entirely offline
- Native Kotlin/Compose project and AndroidManifest with no INTERNET / ACCESS_NETWORK_STATE and no embedded tracking/network clients.
- Authenticated publisher-source assets bundled in signed base APK; fresh installs must work without Wi-Fi, mobile data, accounts or downloads. ✅ Startup reports progress while validating each of all 604 original image SHA-256 hashes and NEVER opens an incomplete Quran page pack.
- Portrait: single page. Landscape tablet: paired pages with correct RTL order and first/last-page handling. Display actual verified original page graphics without rewriting text.
- ✅ Source-safe 100%–300% image zoom **clamped to the actual fitted 1600×2287 image rectangle**, even on wide landscape tablets; double-tap magnifies the exact sheet touched in a two-page spread and returns at 100%. Leaving zoom navigates to the correct NEXT/PREVIOUS full spread; no page flipping while drag-panning. Stable neighboring-page bitmap prefetch. Physical-device touch/mouse/zoom testing still pending.
- Local-only last-page persistence (with migration tests); initial source-not-approved screen is **not** Quran content.
**Exit G2:** 604 locally accessible pages, checked layouts, consistent navigation, airplane-mode operation on devices.

### P3 — Single-ayah self-revision mode (only approved memorization function)
- Overlay conceals verse areas visually without changing original immutable bitmap.
- Reveals one verified ayah at a time; can re-conceal; support verses wrapping multiple lines and pages.
- Exact single-ayah mode still requires independently verified verse coordinates; do NOT invent verse boundaries from OCR. A 15-step manual reveal mask is the first implemented visual self-test fallback (not ayah-accurate).
- Disable mode on pages without approved coordinates. No audio, scoring, scheduling or analysis.
**Exit G3:** 15-step visual mask built and tested; true ayah-by-ayah reveal remains PENDING real coordinate map review.

### P4 — Realistic 3D paper fold
- Candidate evaluation: Compose PageCurl vs custom GPU mesh and curvature.
- Interactive touch/stylus/mouse tracking, release/snap-back, paper thickness, lighting and shadows, front/back page surfaces with correct readable RTL.
- ✅ Hinge-level touch and mouse page turn, snapping and reverse gestures; persisted reduce-motion preference skips page-turn animation. Full curved paper mesh/backside and real device performance certification remain pending.
- Preserve exact stationary approved page; never shader-transform ink in idle reading.
- Measure jank/frame times on Xiaomi Pad 5 and representative phones; 120Hz is a test target, not a guarantee.
**Exit G4:** real hardware footage, RTL verification, page-sequence testing, performance measurements and no glyph loss.

### P5 — Liquid Glass and immersive book
- 🟡 Tool-only translucent glass-style footer implemented below Quran image viewport (gradient, border and highlight). Full optical refraction and hardware visual QA remain pending; never alter or blur Quran glyphs.
- Hide all controls when reading; never overlay text; faithful paper texture with safe margins.
- Optionally Royal Dark Gold, Champagne Gold, Pearl Glass control themes; default page artwork untouched.
- Landscape book with two pages; portrait with one; ✅ local reading settings (keep-screen-awake disabled by default; reduce page-turn motion) and 1×–3× zoom; accessibility and hardware QA pending.
**Exit G5:** verified screenshots from running app and accessible contrast without altering Quranic glyphs.

### P6 — Essential reading functions
- ✅ Verified offline 114-surah, 30-juz, 60-hizb, **240 quarter-Hizb** and 604-page first-verse indexes, cross-checked against independent metadata. Full-word verse text search remains outstanding and must use licensed independently verified text.
- ✅ Local bookmarks, last position, manual khatma, **manual daily Wird target (1–30 pages) with next unread pages and estimated completion**, screen-awake/reduce-motion settings, double-tap zoom, mouse and keyboard support. Real Android phone/tablet UI QA is still pending.
- No network sync, cloud storage, accounts, ads or analytics.
**Exit G6:** correctness on reference edition, app lifecycle tests and migration tests.

### P7 — Audit and launch
- Compile, source guards and test coverage.
- Validate merged manifest, final APK permissions, packaged assets, malware/network SDK absence; first-use airplane mode.
- Compare *signed-APK-extracted* 604 pages with the pinned original publisher-source page hashes. Real-device visual QA remains mandatory before claiming tested production quality.
- Real device: orientation, memory, fast flipping, pointer and swipe, page boundaries and self-reveal.
- Permanent private release signing key in GitHub Actions secrets, pinned certificate SHA-256; updates retain package ID `app.alquran.kareem` and strictly increasing versionCode. No new preview package; no random CI debug signer for a release.
**Exit G7:** correctly signed source-verified 604-page Android APK + offline/per-page-hash proof + physical-device test evidence. Missing signing secrets or corrupt source = NO APK. Institutional scholarly certification is not asserted.

## Deferred post-v1 idea (NOT IN CURRENT BACKLOG)
Interpretation of tapped word or ayah, potentially with approved offline reference and separately reviewed mapping. Do not implement until v1 completed and explicitly authorized.

## Reporting format (each work session)
Date | stage | actual files | commit URL | tests PASS/FAIL/SKIPPED | risk or blocker | next highest-priority task.
Do not label documentation or scaffold as a finished Quran reader.
