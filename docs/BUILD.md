# Development build & limitations
Prerequisites: JDK17; Gradle 8.13; Android SDK 36; Android Studio with Compose support.
Command: `gradle :app:assembleDebug`. There is intentionally no checked-in Gradle wrapper JAR in this first scaffold; CI uses setup-gradle to supply pinned Gradle 8.13.

IMPORTANT: A debug APK built **before** the 604-page independently approved manifest is not a Quran reader and must not be published. It displays a fail-closed notice; no Quran text is hard-coded.

The official source pack is pending (see docs/SOURCE_PROVENANCE.md). Final signed release also requires inspecting merged manifest and `aapt2 dump permissions`, verifying every APK page, and offline device tests.

Never commit a signing key or insert Internet permission. Toolchain dependency downloads by developers/CI are *not* app runtime internet usage.
