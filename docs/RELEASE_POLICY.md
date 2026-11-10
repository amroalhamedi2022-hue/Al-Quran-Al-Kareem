# Release policy — fail closed
- No unsigned proof-of-concept APK is called a Quran app.
- No release until full reviewed source pack and all gates G1–G7 pass.
- Do not commit signing keys, passwords, source archives, reviewers' private information or unlicensed assets.
- Record commit, source version, hash list, artifact hash, Android permissions and installed device tests.
- Verify APK packaged images independently vs approved manifest and complete 604 asset names.
- Key stays off GitHub; maintain same key/applicationId for future upgrades and test data retention.
- Never claim a CI guard scan proves scholarly Quran correctness.
