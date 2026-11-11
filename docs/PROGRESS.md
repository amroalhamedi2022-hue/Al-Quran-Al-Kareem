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

### 2026-11-10 — Verified offline reader navigation foundation (PR #9 merged)

**Real code merged into main:**
- `ReaderNavigationRules.kt`: strict page input validation in Western, Arabic-Indic and Persian digits, bounds 1..604, pure bookmark set logic. New `ReaderNavigationRulesTest.kt`: nine JUnit test methods.
- `ReaderBookmarkStore.kt`: Android private local-only bookmarks, no network/backup/account. `ReaderControls.kt`: go-to-page dialog, save/remove bookmark and browse saved pages.
- `MainActivity.kt`: integrates local bookmarks, navigation controls and white paper behind transparent Quran artwork. Existing `ReaderPositionStore` retains last page locally.
- `ReaderViewportRules.kt` + `ReaderViewportRulesTest.kt` merged from [PR #9](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/9), squash commit `18746612979d63a398163e75e8ef595c4b5601f7`. Double-page view limited to landscape screens >=700dp; portrait/narrow screens show one page.
- Local `kotlinc-jvm 1.9.0` standalone eight-assertion check of digit parsing and bookmarks: PASS.

**Actual CI evidence:** [PR #9 workflow run 42828072808](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42828072808):
- `source-guards`: SUCCESS; **63 Python unit tests passed**, offline source scan checked 12 source files, no banned permission/code found.
- `android-compile`: SUCCESS, `assembleDebug` BUILD SUCCESSFUL and `testDebugUnitTest` BUILD SUCCESSFUL (includes new reader Kotlin tests).
- `aapt2` inspection of debug APK: PASS, **no declared Android network permissions**.
- This is a debug scaffold **without approved Quran pages**, not a readable Quran APK nor a final signed release.
- **Still blocked:** human certification of 604 page conversion by two qualified independent reviewers, separately pinned approved manifest hash, final APK audit, device tests and premium page curl / Liquid Glass.

**Next:** continue no-text-mutation controls, manual review and reader input improvements. Do not bypass Quran validation gate.

### 2026-11-10 — Fully reproduced 604-page source output from two different CI runs
- **Actual CI PASS:** [run 42830167631](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42830167631); `compare-independent-renders` job completed successfully. Both independent source-run artifacts 42799214618 and 42811461469 downloaded by GitHub runner.
- `scripts/compare_full_renders.py` computed and compared actual SHA-256 of every PNG against its own separate source render report and across runs: **604/604 exact byte matches** including width/height. Source archive SHA is the pinned official SHA. NO Quran characters were edited/generated by this operation.
- Seven synthetic adversarial tests executed in this workflow and passed. Reproducibility proof is artifact `TECHNICAL-604-render-reproducibility-NOT-APPROVED` (ID 13770652780).
- Full offline manual-review gallery + 604 original pages: [run 42828626440](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42828626440), job SUCCESS, review artifact ID `13770557808`, retention until **2026-12-10**. Includes `REVIEW_OFFLINE_UNAPPROVED.html`. No human approvals fabricated or recorded.
- Changed costly source workflow so complete rerender happens only through `workflow_dispatch` or an intentional `[render-604]` commit tag; routine source guard tests still run on every push in `quality.yml`. Extended human review artifact retention from 3 to 30 days.
- The archive is reproducible and technically consistent, **NOT YET independently content-approved**. Two qualified independent human page-by-page reviews (604/604) and final APK verification remain the highest-priority release blockers. Do not ship Quran until certified.

### 2026-11-10 — Public independent Quran review recruitment
- Created [GitHub issue #10](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/10) as a concrete open task for two suitably qualified, independent reviewers to inspect all 604 page renders against authorized source artwork.
- Includes direct unapproved 604-page review artifact, fully offline gallery and strict per-page criteria. Does NOT invent reviewers or grant approval.
- The 604/604 reproducibility result is technical source-verification only. **No reviewer signoffs yet, no approved manifest, no signed or released Quran APK.**

### 2026-11-10 — Owner-free official verification route + safer Android pages + release staging

**No owner review burden:** The user will not be asked to inspect 604 Quran pages. Publisher-level independent review route is documented in `docs/OFFICIAL_VERIFICATION_WITHOUT_USER_REVIEW.md` and the official enquiry draft in `docs/KFGQPC_CONTACT_DRAFT_AR.md`. The KFGQPC digital review centre has historically evaluated electronic Quran apps and offered digital certification. We do **not** know the current 2026 intake process; enquiry is UNSENT, no approval certificate exists. Contact: info@qurancomplex.gov.sa. Independent publisher reference PDF identified at `https://qurancomplex.gov.sa/wp-content/uploads/isdarat/hafs/standard39-2.pdf`; independent graphic comparison has NOT been run and exact edition/page offsets are NOT yet verified. Do not equate hash checks with proofreading.

**Android reader:** [PR #11](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/11) merged as `f51500b4577488b1ecd5af0af8e1abd4836c6913`. Adds `QuranPageBitmapRepository.kt`, bounded decoded bitmap cache, safe unloading without recycling still-displayed images, exact bitmap dimension policy and `key(page)` around each Compose page to prevent previous-page content flickering while the next page loads. Tests in `QuranPageCachePolicyTest.kt`. [CI 42834063591](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42834063591) SUCCESS for Android and source guard jobs; no actual Quran page APK published.

**Release architecture:** [PR #12](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/12) merged as `134f6c259b4ebdf44aa55a18f6be2ea8748024bf`. New `scripts/stage_verified_release_assets.py` verifies every original PNG against the externally pinned approved manifest BEFORE and AFTER copying, refuses missing/reordered/changed images, denies unexpected files, never transcodes the Quran. Nine synthetic adversarial tests in `scripts/tests/test_stage_verified_release_assets.py`. The manual `pre-release-quran-gate.yml` now downloads an untrusted source artifact by `source_run_id` only AFTER legitimate independent manifest seal has passed, then packages and revalidates all PNGs. [CI 42835158820](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42835158820) source-guards SUCCESS (**79 Python tests**), Android build + reader JVM tests SUCCESS; APK remains only a blocked scaffold.

**Touch/mouse RTL navigation** currently in [PR #13](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/13), with `ReaderGestureRules.kt`, six boundary cases and Compose horizontal drag; CI source-guards passed, Android-compile was still in progress when this entry was written. Do not mark merged/compiled until confirmed.

**Release hard stop unchanged:** NO official certificate, no two-person accepted independent review, no pinned approved manifest or finalized Quran APK. Only original publisher files and approved authentic content can be embedded. No claims of mathematically guaranteed zero errors. Remaining: publisher evaluation, independent view comparison/flagging, then signed release and device review; after foundation, page-curl, Liquid Glass, self-test masking with VERIFIED coordinate dataset.

### 2026-11-10 — RTL touch navigation CI PASSED + publisher certification request tracked
- [PR #13](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/pull/13) merged as `e6472e6d5f66216a29c6664baa03ecf70ce6eb46`: page-area deliberate right/left drag to navigate one page/spread, minimum 64dp, clamped to 1..604; new `ReaderGestureRules.kt` pure classifier with 6 JUnit tests. No glyphs/pixels rewritten. [CI run 42836128888](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42836128888): source-guards SUCCESS, Android `assembleDebug` BUILD SUCCESSFUL and `testDebugUnitTest` BUILD SUCCESSFUL; APK network permission audit PASS.
- Opened [GitHub issue #14](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/14) to request **official KFGQPC digital publisher assessment** instead of user doing manual 604-page proofreading. Official contact address independently confirmed: `info@qurancomplex.gov.sa`. A request draft is written but explicitly **UNSENT**; current availability of formal digital app certification needs confirmation. No certificate or acceptance has been obtained.
- User is NOT required to manually review 604 pages. Alternate source-to-source comparison (official original AI vs independently published PDF) is proposed but has NOT yet been executed, nor has exact page/edition match been checked. Even successful comparison is technical evidence, not absolute guarantee of zero mistakes.
- Current shipping status: NO approved Quran pack, NO finalized signed release. Development proceeds only on safe app foundation while formal external verification is pursued.

### 2026-11-11 — Actual 604-page offline Android preview and manual Khatma foundation

**No formal certification outreach is being pursued per project owner request.** Issue #14 closed as not planned. This is not a claim of scholarly text approval; preserve original publisher artwork and fail-closed production release gate.

**PR #15 merged** as `4d6d27753310bde4ef9f3d2ba0c0633acc38479c`: immersive reader controls with center-tap toggle, left/right-edge single-page RTL navigation, physical keyboard arrow/Page Up/Page Down, animated control visibility and touch-zone tests. [CI 42983303439](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42983303439) SUCCESS (source guards, Android debug compile/unit tests, offline permission audit).

**PR #16 merged** as `3e6a6b0aec6d6c120b743511a3eb938add21600b`: `QuranSourcePreviewVerifier.kt` allows **DEBUG-ONLY** source-hash-verified display of authentic 604 PNG pages when approved manifest is intentionally absent; runtime SHA-verifies all 604 PNGs before display and shows explicit non-certified preview banner. Separate debug `applicationId=app.alquran.kareem.preview`. `scripts/stage_debug_source_preview.py` copies original bytes only, validates source report and all PNG hashes/dimensions/CRC, no generated text. `scripts/verify_debug_source_preview_apk.py` inspects every byte inside the **actual APK**; source validation tests added, 95 Python tests PASS in pre-merge CI.
- **Actual FULL APK preview build SUCCESS:** [GitHub Actions run 42984559974](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42984559974), all steps SUCCESS: 604 source PNG stage, Android unit tests and `assembleDebug`, exact 604 packaged PNG SHA audit, no network permission, artifact upload. Artifact ID `13838553615`, name `Quran-604-source-verified-DEBUG-PREVIEW-not-for-publication`, size `454833039` bytes. Download direct via Actions page. Distinct debug id protects future signed app. **This preview does not claim official certification or production readiness.**
- Build proof tracked in [issue #17](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/17).
- **PR #18 merged** as `c6a8b23847eec0bbdbfaaf6146d61c0c092cff7e`: user-controlled manual Khatma pages stored offline; 604-page progress indicator and explicit reset confirmation. Navigating to a page never automatically counts as read. 10 Kotlin unit tests added. [CI 42984895392](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42984895392) SUCCESS (source guards + Android compile/tests). **Important:** current previously packaged preview from run 42984559974 was built before PR #18 and therefore does not yet contain khatma UI until rebuilt.
- Open priorities: update preview package with PR #18, verify physical Android phone/tablet behavior and input, improve realistic page curl + liquid glass behind controls without touching Quran ink, add reliable verified surah/juz indices and optional self-test line masking only if authoritative coordinates available. Formal independent certification remains paused, and NO production signed release claim.

### 2026-11-11 — Updated actual 604-page DEBUG APK including Khatma feature

- [GitHub Actions run 42985428323](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/actions/runs/42985428323) **SUCCESS** for full source stage of original 604 pages, Android unit tests and assembleDebug, independent check of all 604 original page bytes inside APK, offline APK permissions, and build artifact upload.
- Updated downloadable artifact ID **13838958787**, `Quran-604-source-verified-DEBUG-PREVIEW-not-for-publication`, ZIP artifact size **454,849,423 bytes**. It now includes merged **PR #18 manual Khatma progress** (as well as PR #15 reading controls, navigation, offline bookmarks).
- This is a separate debug-only install `app.alquran.kareem.preview`, source-hash-verified by test. No production signature or external scholarly certification; debug-keystore signatures may differ between runner builds, so an in-place update is NOT guaranteed.
- Reviewable QA evidence: [issue #17](https://github.com/amroalhamedi2022-hue/Al-Quran-Al-Kareem/issues/17), [installation guide](docs/ANDROID_PREVIEW_INSTALL.md).
- Remaining implementation focus: actual-device touch/mouse and memory testing, premium paper-curl, subtle Liquid Glass, on-device accessibility, search/index only from publisher-verified mapping. No rendered or text source pixel modification is permitted.
