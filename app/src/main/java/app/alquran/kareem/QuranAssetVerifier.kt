package app.alquran.kareem

import android.content.res.AssetManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Rejects missing/changed/off-layout Quran assets. A hash match is NOT scholarly
 * approval; the independent reviewed manifest and APK signature remain required.
 */
internal object QuranAssetVerifier {
    private const val COUNT = 604
    private const val PREFIX = "quran/"
    private const val MANIFEST = "approved_manifest.json"
    private val SHA256 = Regex("[0-9a-f]{64}")
    // Deliberately unset until independent page-by-page signoff. It must not be
    // computed from bundled assets at runtime; separate release review pins it.
    private const val INDEPENDENTLY_APPROVED_MANIFEST_SHA256 = ""

    sealed interface Status {
        data class Approved(val pages: Int) : Status
        /** Original publisher artwork and all 604 PNGs verified byte-for-byte.
         *  This is source integrity, not a claim of scholarly certification. */
        data class SourceVerified(val pages: Int) : Status
        data class Unavailable(val reason: String) : Status
    }

    suspend fun check(assets: AssetManager): Status = withContext(Dispatchers.IO) {
        try {
            require(SHA256.matches(INDEPENDENTLY_APPROVED_MANIFEST_SHA256)) {
                "No independent approval manifest hash pinned yet"
            }
            val manifestBytes = assets.open(PREFIX + MANIFEST).use { it.readBytes() }
            val manifestDigest = MessageDigest.getInstance("SHA-256").digest(manifestBytes)
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            require(manifestDigest == INDEPENDENTLY_APPROVED_MANIFEST_SHA256) {
                "Mushaf approval manifest integrity mismatch"
            }
            val metadata = JSONObject(manifestBytes.toString(Charsets.UTF_8))
            require(metadata.optBoolean("rights_reviewed")) { "Source rights not reviewed" }
            require(metadata.optString("source_name").isNotBlank() && metadata.optString("source_url").isNotBlank()) {
                "Missing authentic source identification"
            }
            require(metadata.optString("riwaya") == "Hafs 'an Asim") { "Wrong riwaya" }
            require(metadata.optString("layout") == "Madinah 604-page") { "Wrong page layout" }
            val approved = metadata.getJSONArray("approved_by")
            val reviewers = (0 until approved.length()).map { approved.getString(it).trim() }
                .filter { it.isNotBlank() }.toSet()
            require(reviewers.size >= 2) { "Two independent source reviewers required" }

            val pages = metadata.getJSONArray("pages")
            require(pages.length() == COUNT) { "Mushaf requires exactly 604 pages" }
            val expectedFiles = mutableSetOf(MANIFEST)
            val digests = mutableSetOf<String>()
            for (number in 1..COUNT) {
                val entry = pages.getJSONObject(number - 1)
                val filename = "%03d.png".format(java.util.Locale.ROOT, number)
                require(entry.getInt("page") == number && entry.getString("file") == filename) {
                    "Page sequence mismatch"
                }
                val expected = entry.getString("sha256")
                require(SHA256.matches(expected) && digests.add(expected)) { "Invalid or duplicate digest" }
                expectedFiles.add(filename)
                val md = MessageDigest.getInstance("SHA-256")
                assets.open(PREFIX + filename).use { stream ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = stream.read(buffer)
                        if (n < 0) break
                        md.update(buffer, 0, n)
                    }
                }
                val actual = md.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
                require(actual == expected) { "Page hash does not match approved source" }
            }
            require(assets.list("quran")?.toSet() == expectedFiles) { "Missing or unexpected Quran assets" }
            Status.Approved(COUNT)
        } catch (cancelled: CancellationException) {
            throw cancelled // Prevent stale verification after Activity cancellation.
        } catch (_: Exception) {
            // Do not leak a half-reviewed page or fall back to any generated text.
            Status.Unavailable("ملفات المصحف المعتمدة غير مكتملة أو لم تجتز فحص السلامة.")
        }
    }
}
