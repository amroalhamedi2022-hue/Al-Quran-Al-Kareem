# Signing and versioning policy — one Android app, in-place updates

**Production application ID (immutable):** `app.alquran.kareem`.

**First production version:** `versionCode=100`, `versionName=1.0.0`.
Every later production update MUST retain the same `applicationId` and signing certificate and increase `versionCode` strictly (101, 102, ...). Never distribute temporary debug-signed APKs as upgrade candidates.

## Why the previous separate preview cannot be upgraded in place

The previous GitHub-runner debug preview used `app.alquran.kareem.preview`, and runner debug signatures may change. Android cannot turn it into `app.alquran.kareem` in place because package IDs and signing certificates differ. This is a **one-time transition**. Future official application builds will all use the permanent production ID and same release key. Local reading progress in the old preview is isolated to the old package; we cannot silently transfer it under Android sandboxing rules.

## Secure one-time signing setup

There is **no permanently held Quran production keystore in this repository**. Never commit one (even if this repo is public); loss or exposure of this key would break trusted future updates.

The repository owner must supply an original, durable private signing keystore through GitHub repository **Settings → Secrets and variables → Actions → New repository secret**, with these five exact names:

- `QURAN_RELEASE_KEYSTORE_B64`: base64 of the PKCS#12/JKS binary keystore file (single-line).
- `QURAN_RELEASE_STORE_PASSWORD`: keystore password.
- `QURAN_RELEASE_KEY_ALIAS`: original signing alias.
- `QURAN_RELEASE_KEY_PASSWORD`: signing key password.
- `QURAN_RELEASE_CERT_SHA256`: 64-character SHA-256 certificate fingerprint (this fingerprint is public; the private keystore is not).

Create the keystore locally with Android Studio **Build → Generate Signed Bundle / APK**, using a strong keystore password and long-lived validity. Store the original key file and passwords securely outside the public repository, with at least one encrypted private copy. Do not reuse an unrelated application's key unless you deliberately intend to share a signing identity.

To obtain the certificate fingerprint from the keystore, you can run locally:

```sh
keytool -list -v -keystore quran-release.jks -alias quran-release
```

Use the SHA256 fingerprint without colons, normalized to lowercase, for `QURAN_RELEASE_CERT_SHA256`.

On a local machine with base64, encode the keystore **without line breaks**:

```sh
base64 -w 0 quran-release.jks
```

Keep the base64 text private; it encodes the entire secret key.

## Building one signed update

Open GitHub Actions → **Quran — stable signed Android update (official-source bytes)** → Run workflow.
The source run ID defaults to a previously successful publisher-archive SHA-verified 604-page render. If its short-lived artifact expires, rerun the official source-staging workflow first and use the resulting successful run ID.

The signed-update workflow fails closed unless ALL of the following pass:

1. The five required private signing secrets exist and the pinned certificate fingerprint is valid.
2. Source archive metadata matches publisher-published SHA-256 and all 604 original page PNG hashes/dimensions are verified before building.
3. Android JVM tests and `assembleRelease` succeed.
4. APK is aligned, signed and `apksigner verify --print-certs` confirms the same pinned signing certificate.
5. `aapt2 dump badging` verifies production ID `app.alquran.kareem` and exact versionCode 100.
6. The finished signed APK's **own** 604 image bytes match the verified source, and no network permissions are declared.
7. Only then does the job publish `Al-Quran-Al-Kareem-v1.0.0-SIGNED-OFFLINE` artifact.

This produces a regular updateable Android APK rather than another trial application. It does not assert institutional religious certification; the Quran page source is attributed to King Fahd Glorious Quran Printing Complex, Madinah, Hafs 1441H.

**Current truth:** no production signing secret has been verified as connected in this conversation, so a signed release cannot yet be claimed. Never substitute temporary CI/debug keys or upload private signing credentials to a public GitHub repository.
