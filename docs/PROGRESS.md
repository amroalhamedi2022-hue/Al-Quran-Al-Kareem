# LIVE PROGRESS — القرآن الكريم

**Last updated:** 2026-11-10 | **Canonical repository branch:** main.
**Truth labels:** DOCUMENTED / IMPLEMENTED / TESTED / APPROVED / RELEASED are distinct.

## Status by workstream

| Stage | Status | Evidence / blocker |
|---|---|---|
| P0 Locked requirements and all-stage plan | ✅ DOCUMENTED | README, docs/MASTER_PLAN.md, QURAN_INTEGRITY.md, DECISIONS.md, SOURCE_PROVENANCE.md |
| P0 Automated source and offline checks | ✅ TESTED | GitHub Actions run 42790593497, **12 of 12 Python tests PASSED**; source scan passed |
| P1 Authentic official Hafs source archive | ⛔ NOT ACQUIRED / NOT VERIFIED | KFGQPC website & upstream published SHA identified; source download not completed; no content approval |
| P1 All 604 original page images and 2 independent approvals | ⛔ NOT APPROVED | No page pack, independently signed manifest, rendered page review |
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
1. Obtain authoritative Hafs/Madinah original vector archive from KFGQPC, preserve original untouched and run scripts/verify_upstream_archive.py against published upstream SHA.
2. Produce pages 001.png–604.png using a non-editorial, repeatable process; review every exported page plus source layout, and record independent approvals & manifest.
3. Bundle audited pages; perform true offline 604-page reader tests on Android and tablet.
4. Only after that: verified ayah-reveal map, paper curling, liquid glass, reader utilities and signed release.

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
