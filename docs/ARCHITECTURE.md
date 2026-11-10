# Architecture and permanent boundaries

1. `source/quran/` OFF-REPOSITORY source-of-truth archive, provenance plus checksums.
2. `app/src/main/assets/quran/` read-only audited page pack and its approved_manifest.json, **empty until approved**.
3. `QuranAssetVerifier` checks all 604 pages against signed-off manifest before reader may render.
4. `Reader` draws only original page image(s); never programmatically rewrites Quran.
5. `VerseOverlay` future independently reviewed mask coordinates, never edits source image.
6. `PaperCurl` future geometric page mesh, shadows separate from verified pixels.
7. `GlassControls` future UI edges only. No network/services by architecture.
8. `LocalReadingState` future local-only progress data; no sync, backup needs privacy review.

### Technical baseline
Kotlin + Compose, JDK17, AGP 8.13.x / Gradle 8.13 / Kotlin 2.3.21 (verified from upstream sources on 2026-11-10). Compose BOM 2026.09.00. Versions pinned. AGP compileSdk <=36.1.

### Dependency rule
No remote fetches at app runtime (Gradle downloading during **development/build** is distinct from an installed Android application's operation). CI may access dependency repositories to build.
