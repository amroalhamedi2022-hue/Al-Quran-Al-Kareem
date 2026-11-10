# LIVE PROGRESS — القرآن الكريم

**Last updated:** 2026-11-10 | **Canonical repository branch:** main.
**Truth labels:** DOCUMENTED / IMPLEMENTED / TESTED / APPROVED / RELEASED are distinct.

## Status by workstream

| Stage | Status | Evidence / blocker |
|---|---|---|
| P0 Locked requirements and all-stage plan | ✅ DOCUMENTED | README, docs/MASTER_PLAN.md, QURAN_INTEGRITY.md, DECISIONS.md, SOURCE_PROVENANCE.md |
| P0 Automated source and offline checks | ✅ TESTED | GitHub Actions run 42790593497, **12 of 12 Python tests PASSED**; source scan passed |
| P1 Authentic official Hafs source archive | ✅ DOWNLOADED & BYTE VERIFIED | GitHub Actions run 42799214618: original archive SHA-256 matched publisher; exactly 604 AI artwork files staged |
| P1 Rendered 604 PNG images and 2 independent approvals | ✅ RENDERED + TECH CHECK; ⛔ HUMAN APPROVAL PENDING | Artifact 13756210989: 604 RGBA PNGs; 604 hashes matched, pixel visibility/borders checked; NO scholarly signoffs |
| P2 Android Kotlin/Compose foundation | ✅ IMPLEMENTED and ✅ BUILT | :app:assembleDebug BUILD SUCCESSFUL in CI run 42790593497 |
| P2 Offline permission check on produced APK | ✅ TESTED | Android aapt2 permissions inspection PASS (no declared network permissions) |
| P2 Actual complete Quran reading | ⛔ BLOCKED by P1 | Only codepath to render verified pages; no authentic page assets available, intentionally fails closed |
| P3 Single-ayah self-check visual reveal | ⏳ NOT IMPLEMENTED | Requires independently verified verse coordinate masks; no audio or scoring |
| P4 Realistic 3D page curl | ⏳ NOT IMPLEMENTED | After actual reviewed Quran reader |
| P5 Liquid Glass design system | ⏳ NOT IMPLEMENTED | After source and reader |
| P6 Index, bookmarks, search, manual wird/khatma | ⏳ NOT IMPLEMENTED | Requires approved metadata |
| P7 Signed official Quran APK, update/device tests | ⛔ NO RELEASE | Source/604 review, hardware tests, signing not completed |

## Verified CI evidence (2026-11-10)
- GitHub Actions run URL: https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42790593497
- source-guards job: **SUCCESS**, 12 unittest fixture checks passing.
- android-compile job: **SUCCESS**, `:app:assembleDebug` successful.
- aapt2 permission inspection: **PASS**, zero Android network permissions in the produced debug APK.
- The CI debug APK **does NOT contain approved Quran pages**; success is a build-safety result, NOT Quranic content approval, real hardware validation or product release.
- Pull request #7 (compatibility/offline verifier improvements) merged into main: https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/7 ; merge commit `c7a84f1ee0bce6ee93a46add7b07212387e54292`.

## Outstanding highest priority
1. Two independent human page-by-page comparisons of ALL 604 PNGs to original official artwork, documenting any glyph, harakah, waqf, color, margin, renderer-font or crop differences; do not self-certify from checksum tests. Use the 604-row PENDING ledger.
2. Repair conversion if any reviewed discrepancy is found, then re-run full render audit and visual signoff. Pin independent approved manifest only **after** all human reviews and explicit provenance acceptance.
3. Bundle independently approved PNG pages into Android and confirm offline reader on real phone/tablet; test all pages and failure/recovery states.
4. Only then complete and measure self-reveal, paper curl, Liquid Glass, bookmarks, metadata, final signed release.
5. Check fresh CI results after latest commits before claiming Android build/test success.

## Invariant scope (never broaden silently)
Application title **القرآن الكريم**. Quran text never edited, retyped, corrected by AI or OCR. Offline-only. No recitation audio, no smart memory review, no similar-verses module, no teacher portal. Word interpretation on tap deferred post-v1, NOT implemented.

## Session completion rule
Every subsequent work session updates this file with exact paths, commit/run URLs, PASS/FAIL and remaining blockers. No full-completion claim without actual G1–G7 acceptance.

### 2026-11-10 — PNG robustness increment
- Added `scripts/validate_png.py`: validates PNG signature, IHDR layout/dimensions, chunk CRC, IEND, animation rejection and trailing bytes; structural check only.
- Added `scripts/tests/test_png_validation.py`: six adversarial/synthetic tests. An equivalent local six-test suite passed; **GitHub CI / complete repository suite is not verified here**.
- Integrated PNG validation into `scripts/verify_quran_pack.py`, replaced previous signature-only check. Refreshed existing synthetic fixture generation and corrupt-PNG assertion in `scripts/tests/test_guards.py`.
- Commits: `3d39866`, `6a8fc57`, `d244e6f`, `298a761`, `975a6a4`.
- **Still blocked:** original approved 604-page source, independent human signoff, Gradle compile confirmation, final device checks and signed APK. No authentic Quran page was inserted.

## 2026-11-10 — Official Hafs source pipeline and immutable approval seal

| Item | Actual status | Evidence |
|---|---|---|
| Public source location and software use rights | ✅ LOCATED, not imported | Official KFGQPC vector Mushaf / rights (see `docs/OFFICIAL_QURAN_SOURCE.md`) |
| Official archive published SHA256 pinned | ✅ IN CODE | `scripts/stage_official_hafs.py` |
| Archive staging with SHA256 + ZIP CRC + exact 604 AI pages + traversal/duplicate checks | ✅ COMMITTED; official archive NOT YET RUN | `scripts/stage_official_hafs.py` |
| Synthetic staging tests | ✅ 8/8 PASS in local equivalent script; GitHub runner status not verified | `scripts/tests/test_official_stage.py` |
| GitHub Actions download and sample renderer | ✅ WORKFLOW COMMITTED; download/result not verified | `.github/workflows/official-source-staging.yml` |
| 604 official pages actually downloaded and converted | ⛔ NOT YET | this environment failed to download official zip; cannot claim files exist |
| Independent approved manifest SHA pin in Android | ✅ FAIL-CLOSED CODE COMMITTED | `QuranAssetVerifier.kt`; blank until human approval |
| Approval-seal CLI + four synthetic tests | ✅ COMMITTED; full CI status not yet verified | `scripts/verify_approval_seal.py`, `scripts/tests/test_approval_seal.py` |
| CI enforces seal for any bundled Quran pack | ✅ CONFIG COMMITTED | `.github/workflows/quality.yml` |
| Human review and signing APK | ⛔ NOT STARTED | gates must remain closed |

**Proof commits:** `c7917c8` (source gate), `a96242d` (8 tests), `0cc07e8` (runner), `93d7cde` (provenance), `2bffc37` (Android pin), `1406322` & `ddc716e` (seal tests), `b0a0ee4` (CI enforcement).

**Next immediately:** inspect GitHub Actions official-source-staging run. If the zip download/checksum succeeds, inspect rendering of sample pages 1,2,3,604, then render 604 and start independent human/page review. If the job fails, preserve error log and repair source endpoint; do not mark source approved. Gradle/Android build and device testing not verified in this session.

### 2026-11-10 — Extended source conversion stage
- Added `scripts/render_staged_hafs.py` (commit `24b4e27`): re-checks each staged AI SHA and requires a verified source-stage report; exports 604 ordered 1600px PNGs via Inkscape, checks PNG structure/dimensions, records render hashes as **PENDING review**, cleans partial output on failure. NEVER writes `approved_manifest.json`.
- Updated source-staging workflow (commit `1d417f0`): after official archive SHA, 604 source AI checks and sample render, it attempts **all 604 rendered pages** and uploads explicitly UNAPPROVED review artifact, 3-day retention.
- This is pipeline CODE only. **No confirmed official 604-file archive download, complete rasterization, independent human review or final APK.** Workflow run results from the new pipeline have not been verified.

### 2026-11-10 — Verified original archive mirror + foundational offline reader fixes

**Source pipeline implementation (NOT source approval):**
- Retrieved public mirror provenance metadata from `quran-ws/kfgqpc-resources`: it records the same original Hafs (1441H) archive SHA-256 as the KFGQPC publisher, with **604 vector art files**, total compressed size **466,430,736 bytes**. The mirrored original ZIP is a fallback; the mirror is not an official publisher.
- Updated `.github/workflows/official-source-staging.yml` (commit `79da124`) to try three sources, and reject wrong **SHA-256 OR byte length** before moving to the next.
- Created issue [#8](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/8) for public, machine-verifiable run summaries; workflow update `4b5486e` reports download/staging/preview/all-604 step outcomes on future runs even when they fail. **No successful 604-page run confirmed as of this status update**.

**Actual Android functionality implemented, NOT YET re-verified by CI:**
- `ReaderPagination.kt`: consistent right-to-left spread calculations for all 604 pages and no phantom first/last spreads.
- `ReaderPositionStore.kt`: persist last-open page on device via private SharedPreferences with no account or Internet.
- `MainActivity.kt`: connects RTL pagination and local persistence to the reader; reader remains fail-closed until independently approved Quran pages exist.
- `ReaderPaginationTest.kt`: 8 JUnit cases committed; `app/build.gradle.kts` adds JUnit dependency and `quality.yml` executes `testDebugUnitTest` in CI.
- Representative commit links: `eea2f6b`, `9e8b37e`, `972e5cc`, `8cf7816`, `9a1c3e4`, `6166787`, `5537049`.
- **Test status:** tests are committed, GitHub Actions execution of *new* tests is NOT confirmed as passing. Earlier base build (run 42790593497) passed before these changes.

**Highest priority unchanged:** 604 actual source assets downloaded and verified; page rendering and independent human visual review; only then official PNG pack, manifest seal, integrated Android tests and signed release. No page art, text or offline policy was changed.

### 2026-11-10 — Independent local Kotlin pagination verification
- Reproduced the committed pure `ReaderPagination` logic in a clean local Kotlin compiler harness (`kotlinc-jvm 1.9.0`).
- **PASS:** eight RTL portrait/landscape scenarios plus exhaustive constraints for every one of the **604 page positions**: visible spread order, valid bounds, and forward/backward progression.
- **Scope limitation:** local pure-Kotlin logic only. GitHub JUnit CI, Compose integration, actual Android APK and on-device screenshots have **not** been confirmed in this session; do not equate these.
- GitHub Action source audit issue [#8](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/8) still had **no machine-posted result comment** when last checked. Do not mark official source as downloaded or approved.

### 2026-11-10 — Real 604-page technical audit + offline human review tooling
- **Confirmed GitHub Actions RUN SUCCESS:** [42799214618](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42799214618) downloaded the original Hafs source ZIP and matched published SHA-256, extracted 604 original AI pages, rendered 604 PNGs, and produced review artifacts (artifact ID `13756210989`, 604 PNGs, ~437 MB archive).
- **Downloaded and independently checked complete artifact locally:** ZIP CRC PASS, 604/604 original PNG file SHA-256 against render report PASS; all 604 fully decoded; all 604 RGBA dimensions 1600×2287; all 604 nontransparent content bounding boxes nonempty and strictly inside image edges. No suspiciously small page bbox. Verified actual image preview on a white viewer for 001, 003 and 604 (background was view-only; PNG bytes untouched).
- **Added `scripts/audit_rendered_pages.py` + `scripts/tests/test_raster_audit.py`** (8 synthetic tests passed locally), with full-decode technical audit and CI integration; separate `scripts/audit_render_archive.py` from concurrent work validates archive structure & hashes. Corrected workflow to avoid including extra technical JSON in strict ZIP (`8bf8052`).
- **Added OFFLINE 604-page manual review gallery** `scripts/make_review_gallery.py`, `scripts/tests/test_review_gallery.py`, workflow inclusion and instructions `docs/HUMAN_QURAN_REVIEW_PROTOCOL.md`. Gallery supports full-sized original PNG viewing, previous/next, bookmarks of review progress in browser local storage, per-page flagged notes and standalone JSON export/import. Does NOT automatically approve any page.
- Evidence: [public review issue #8](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/8), commits `58997ddd`, `4017253b`, `d630cdf`, `8bf8052`, `ec27d0ff`, `7d9a31db`, `4db4ff0c`, `bc2a946d`.
- **New CI run for recent audit/gallery changes has not yet been confirmed PASS.** No independent human review signoffs, no pinned approved manifest, and no approved signed app or live-device test.
- **Next:** independent reviewer workflow on all 604 pages; fix any rendering defects found, then cryptographically pin independent approval and package content into offline Android app. Continue technical P2 improvements safely without bypassing reader gate.

### 2026-11-10 — Source render fully downloaded, technical audit, reader fix and review workflow
- **Source pipeline VERIFIED SUCCESS:** GitHub Actions [run 42799214618](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42799214618), job `verify-and-preview` success. Official source zip from archive.org mirrored byte-for-byte against pinned publisher SHA256. All 604 original .ai files extracted and rendered to 604 PNG files.
- **Download artifact inspected:** `UNAPPROVED-Hafs-604-PNG-for-human-review` ID 13756210989 (ZIP 437,374,353 bytes; **artifact ZIP SHA-256** `27ebef16721cad7fa826220c4d2a4f72e0032ce6a620e414fdcd1d5bb3950432`). ZIP CRC PASS; all 604 PNG SHA-256 hashes MATCH `UNAPPROVED_RENDER_REPORT.json`; no byte-identical duplicate PNGs; all images 1600x2287, RGBA8. In addition, 25 distributed pages had non-empty alpha content with no bounding boxes at edges. No Quranic text correctness claimed.
- **Public audit evidence:** [Issue #8](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/8), including source-run comment and full-hash technical review comment.
- **Actual new code:** `scripts/audit_render_archive.py` with **7 synthetic adversarial local tests passing**, commits `1d72340`, `1bd40b7`; CI workflow audit + independent downloadable audit report, commit `ba40282`.
- **Actual QA process code:** `scripts/make_review_ledger.py` creates **604 PENDING rows** with page hashes; refuses to overwrite annotations or invent approvals. **4 new local unit tests passing**, commits `2a842bc`, `9ad2211`. Workflow creates ledger and publishes with the report, commit `74388c0`. These 11 local unit tests PASS; GitHub CI on latest commits not yet verified.
- **Release-blocker fixed:** `scripts/verify_quran_pack.py` previously rejected its own co-located `approved_manifest.json` in final APK asset layout. Now permits exactly that one metadata file while rejecting other unknown files; two new regression tests, commits `6f0769d`, `8882c83`.
- **Reader code fix:** `MainActivity.kt` keys loaded bitmap state by page number, reports decode errors rather than loading forever, and preserves white paper underneath the transparent original artwork; commit `93c4563`. Android build after this modification NOT YET independently verified.
- **Source review still PENDING:** No signed approvals from two independent reviewers; no approved manifest hash pinned in Android; no authorized release APK. Neither technical audit nor local review ledger counts as religious/scriptural approval.

### 2026-11-10 — Confirmed recovered 604-source CI & fail-closed review process
- **Successful new full pipeline:** [GitHub Actions run 42811461469](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42811461469) independently reports SUCCESS for original-source SHA verification, 604 vector originals, 604 PNG renders, whole-archive audit, and all-pixel technical audit. Earlier run 42811340258 failed due extra technical JSON in strict image ZIP (fixed by `8bf8052`). Run 42811950453 skipped download after test failure caused by escaped synthetic PNG signature and missing Pillow; fixed by commits `319648d` and `da125bc`. These are honest failure histories, not hidden.
- `scripts/make_review_gallery.py`, `scripts/tests/test_review_gallery.py` and workflow gallery step committed: artifact will contain local-only `REVIEW_OFFLINE_UNAPPROVED.html` showing 604 raw PNGs with navigation, note/flag per page and manual JSON export/import. **The new gallery pipeline run has not yet been confirmed PASS**.
- `scripts/verify_review_records.py` and `scripts/tests/test_review_records.py` committed: require two different names plus 604 matching page digests, checked pages, no flagged/missing pages, and timestamps. Does NOT verify identity, actual source comparison or authorize publication. Eight consistency test cases added, not yet verified in GitHub Actions.
- `docs/HUMAN_QURAN_REVIEW_PROTOCOL.md` records the actual two-person manual acceptance protocol. Human reviewers have not yet signed off; `approved_manifest.json` remains intentionally absent.
- `MainActivity.kt` now fails closed on undecodable/wrong-dimension Quran page and offers retry without generating any alternate Quran content (commit `d6e1d4d`). **Android build after this change not yet independently confirmed**.
- CI workflow now keeps both independent image audits and includes the per-page review ledger in the technical artifact (`140931c`).
- **Priority:** obtain two qualified independent 604-page comparison reviews and solve any issues; then pin approved manifest/ship offline Android reader; paper curl and glass remain future development steps after authenticity.

### 2026-11-10 — Pre-release FAIL-CLOSED workflow (no APK published)
- Added `.github/workflows/pre-release-quran-gate.yml` (commit `60fbd81`), **manual invocation only**. Before even compiling an unsigned release it requires a physically present approved 604-page pack, verifies its independent SHA pin and every file, and enforces offline-only source restrictions.
- If and only if the authentic pack exists and passes checks, workflow runs Android unit tests + `assembleRelease`, then verifies the **actual unsigned APK's embedded 604 PNG bytes** against the independent approved manifest and inspects merged APK permissions via aapt2. It does not sign, upload, or publish any APK.
- **As of this entry:** gate is committed but **expected to FAIL** (no human-reviewed pack or pinned approved-manifest hash). The workflow has NOT been claimed passing or run; this protects the user from a misleading scaffold-release success.
- A separate human signoff and final signed-APK release remains REQUIRED after this technical preflight.
