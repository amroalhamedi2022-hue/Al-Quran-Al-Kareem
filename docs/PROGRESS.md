# LIVE PROGRESS — القرآن الكريم

**Updated 2026-11-10 — GitHub initialization + offline Android scaffold.**
Status notation: ✅ code committed; 🧪 automated tests run/pending; ⛔ mandatory gate not passed.

| Stage | State | Evidence / outstanding |
|---|---|---|
| P0 Scope and project plan | ✅ committed | README, docs/*.md; official Arabic name locked |
| P0 Python safety guard tooling | ✅ code committed, 🧪 CI pending | scripts/verify_quran_pack.py, verify_offline.py, 7 fixture tests |
| P1 Authentic source pack | ⛔ NOT APPROVED | Official KFGQPC page + posted archive SHA found; no original download, 604 page export, hashes, human signoff |
| P2 Android native offline scaffold | ✅ code committed, 🧪 build pending | app Gradle project, source checks, Compose fail-closed screen; **NOT an approved reader** |
| P2 Real 604-page reader | ⛔ blocked by P1 | UI code can show verified bitmaps; not tested with actual pages or device |
| P3 Self-revision verse reveal | ⛔ not started | awaiting reviewed coordinates |
| P4 Realistic page curl | ⛔ not started | after real reader |
| P5 Liquid Glass | ⛔ not started | after real reader |
| P6 Index, bookmarks etc. | ⛔ not started | requires reviewed metadata |
| P7 Signed APK release | ⛔ not started | no approved content, build, real device QA, or private signing |

## Evidence
- Repo initialization commit: 484a0f9ed341483e5171482abad9a954b6ce18ab.
- Plan documents commit: 068b6e3516948f0ba61c8614e635b6008d2084b4.
- Official source reference SHA (not yet locally verified): 280C5D71CA16AAEEB3A343BE1B92C76FA6DF71D0671E202C026FDE12402D9EEF.
- Test fixtures contain **no Quranic content**. Real Android build/CI still pending at the time of writing.

## Next sequence
1. Verify Android CI, fix all failures and document proof.
2. Acquire official source safely; validate original archive published SHA.
3. Export/review all 604 pages with independent approvals, then integrate approved files.
4. Verify portrait/landscape real pages offline; only then implement visual reveal, page curl and glass.

No screenshots from simulated/generated Quran pages will be called real-device screenshots.
