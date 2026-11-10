# القرآن الكريم — MASTER EXECUTION PLAN v1.0

**Date:** 2026-11-10. **Status:** Approved scope / implementation in progress.
**Mission:** أصالة المصحف، فخامة أدوات زجاجية تختفي، وسكينة القراءة دون تشويش. لا تضحية بسلامة النص من أجل المظهر.

## Mandatory sequence & release gates
**P0: Contracts & policy** → **P1: Authentic source & 604 immutable pages** → **P2: Offline native reader** → **P3: Self-test verse reveal** → **P4: physical page curl** → **P5: liquid glass** → **P6: basic reading utilities** → **P7: QA, review and signed release**.

Order is by dependency, not visual appeal. Any blocker in textual authenticity stops all release claims, but safe independent engineering may continue.

### P0 — Project foundation and governance
- Lock name **القرآن الكريم**, application identity and offline-only product scope.
- Record product exclusions: all audio, reciters, microphone, smart memorization, teacher, similar-verses system, online services.
- Track work in docs/PROGRESS.md with verified commit/test evidence.
- Establish source integrity and offline CI gates, plus independent review and approval rules.
**Exit G0:** documented contracts, tracked repo, automated guard tests. **Not evidence** of Quran source verification.

### P1 — Authentic Mushaf source (highest priority)
1. Obtain official Madinah Mushaf, Hafs 'an Asim, original 604-page artwork from King Fahd Complex, or a separately verified licensed source.
2. Preserve unmodified original source; record precise edition, origin URL, download date and upstream published SHA if any.
3. Confirm redistribution and derivative-export permissions; review exact license for actual file format and distribution.
4. If source is vector artwork, export losslessly to numbered pages 001.png–604.png using a documented non-editorial process. No text regeneration, AI, OCR, Unicode normalization or handwritten retyping.
5. Compare every exported page visually with original; record independent scholarly/human review including signs, vowel marks, page order, ornamentation and cropping.
6. Freeze reviewed pack manifest with SHA-256 for all pages, precise source and two independent approval records. Verify packaged pages against *independently pinned* manifest before release.
7. Never publish a preview APK with fabricated verses, even as temporary placeholders.
**Exit G1:** 604/604 independently checked pages, licensed and immutable, plus repeatable validation with evidence. If absent: FAIL CLOSED.

### P2 — Basic Android reader, entirely offline
- Native Kotlin/Compose project and AndroidManifest with no INTERNET / ACCESS_NETWORK_STATE and no embedded tracking/network clients.
- Approved assets bundled in base APK; fresh installs must work without Wi-Fi, mobile data, accounts or downloads.
- Portrait: single page. Landscape tablet: paired pages with correct RTL order and first/last-page handling. Display actual verified original page graphics without rewriting text.
- Responsive image scaling without cutting diacritics. Stable navigation; adjacent-page prefetch and low-memory behavior.
- Local-only last-page persistence (with migration tests); initial source-not-approved screen is **not** Quran content.
**Exit G2:** 604 locally accessible pages, checked layouts, consistent navigation, airplane-mode operation on devices.

### P3 — Single-ayah self-revision mode (only approved memorization function)
- Overlay conceals verse areas visually without changing original immutable bitmap.
- Reveals one verified ayah at a time; can re-conceal; support verses wrapping multiple lines and pages.
- Requires an independently approved coordinate map per page and ayah; never infer glyph positions from OCR.
- Disable mode on pages without approved coordinates. No audio, scoring, scheduling or analysis.
**Exit G3:** each ayah mapping reviewed, zero exposure of wrong adjacent words; no content modifications.

### P4 — Realistic 3D paper fold
- Candidate evaluation: Compose PageCurl vs custom GPU mesh and curvature.
- Interactive touch/stylus/mouse tracking, release/snap-back, paper thickness, lighting and shadows, front/back page surfaces with correct readable RTL.
- Support reversals, rapid gestures, orientation, cancellation, motion-reduction preferences.
- Preserve exact stationary approved page; never shader-transform ink in idle reading.
- Measure jank/frame times on Xiaomi Pad 5 and representative phones; 120Hz is a test target, not a guarantee.
**Exit G4:** real hardware footage, RTL verification, page-sequence testing, performance measurements and no glyph loss.

### P5 — Liquid Glass and immersive book
- Tool-only glass with subtle reflection/refraction, no colored bubble halo or rectangular ripple.
- Hide all controls when reading; never overlay text; faithful paper texture with safe margins.
- Optionally Royal Dark Gold, Champagne Gold, Pearl Glass control themes; default page artwork untouched.
- Landscape book with two pages; portrait with one; accessibility and reduce-motion.
**Exit G5:** verified screenshots from running app and accessible contrast without altering Quranic glyphs.

### P6 — Essential reading functions
- Verified surah/juz/hizb/rub/page directory; offline search only from independent approved index matching original.
- Local bookmarks, last position, manual daily wird / khatma, minimal settings, mouse and keyboard support.
- No network sync, cloud storage, accounts, ads or analytics.
**Exit G6:** correctness on reference edition, app lifecycle tests and migration tests.

### P7 — Audit and launch
- Compile, source guards and test coverage.
- Validate merged manifest, final APK permissions, packaged assets, malware/network SDK absence; first-use airplane mode.
- Compare *APK-extracted* 604 pages with independently approved hashes; human visual review on final build.
- Real device: orientation, memory, fast flipping, pointer and swipe, page boundaries and self-reveal.
- Private, stable signing key outside GitHub; update-over-installed preserves data; release only when all gates pass.
**Exit G7:** signed verified APK + recorded evidence. Any missing Quran source, wrong page, corruption, overlap or offline violation = **NO RELEASE**.

## Deferred post-v1 idea (NOT IN CURRENT BACKLOG)
Interpretation of tapped word or ayah, potentially with approved offline reference and separately reviewed mapping. Do not implement until v1 completed and explicitly authorized.

## Reporting format (each work session)
Date | stage | actual files | commit URL | tests PASS/FAIL/SKIPPED | risk or blocker | next highest-priority task.
Do not label documentation or scaffold as a finished Quran reader.
