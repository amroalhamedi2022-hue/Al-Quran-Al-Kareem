# Acceptance & test matrix
| Gate | Mandatory test | Required evidence |
|---|---|---|
| G1 | 604 PNGs exactly numbered; SHA-256 matches independently reviewed file; documented rights | Verified source, reviewed manifest, 2 human signoffs |
| G1 | original vs exported artwork/diacritics vs final APK imagery | all 604 reviewed, issues closed |
| G2 | Android app has zero network permissions in source, merged manifest AND APK | CI logs + aapt2 + runtime airplane test |
| G2 | fresh app install shows every page offline; clear fail closed if no pack | device recording + 604 index traversal |
| G2 | portrait one page; landscape paired right-to-left layout | device screenshots with verified genuine pages |
| G3 | reveal one ayah at a time, correct bounds, multi-line and page crossing | reviewed coords + device tests |
| G4 | drag/reverse/mouse/pointer, RTL backside and paper shadows | hardware tests, frame time recording |
| G5 | glass hidden during reading, does not cover Quran; no unwanted halo | actual screenshots, accessibility review |
| G6 | bookmarks/last page/wird/Khatma, index and search | verified reference, lifecycle and update tests |
| G7 | no missing Quran asset in final signed APK, integrity & version update | apk inspection, 2 independent reviewers |
| G7 | no INTERNET/audio/network/analytics plugins or unauthorized source | dependency/manifest static audit + runtime |
All G1–G7 mandatory. Tests with synthetic fixtures **do not imply approved Quran assets**.
