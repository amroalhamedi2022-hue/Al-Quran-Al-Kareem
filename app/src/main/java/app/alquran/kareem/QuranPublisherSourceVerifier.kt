package app.alquran.kareem

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale

/**
 * A release-capable reader for 604 byte-verified publisher-source PNGs.
 *
 * The source is byte-verified but the app does not claim institutional certification. No word is retyped, generated,
 * inferred with OCR or modified. The source report and all PNG byte hashes are
 * checked before even one page can appear on screen.
 */
internal object QuranPublisherSourceVerifier {
    private const val SOURCE_SHA256 =
        "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
    private const val SOURCE_MANIFEST = "source_preview_manifest.json"
    private val SHA256 = Regex("[0-9a-f]{64}")

    suspend fun check(assets: AssetManager): QuranAssetVerifier.Status.SourceVerified? =
        withContext(Dispatchers.IO) {
            try {
                val manifest = assets.open("quran/$SOURCE_MANIFEST").use { it.readBytes() }
                val metadata = JSONObject(manifest.toString(Charsets.UTF_8))
                require(metadata.optString("status") == "UNAPPROVED_RENDER_ONLY")
                require(metadata.optString("source_sha256") == SOURCE_SHA256)
                require(metadata.getInt("page_count") == 604)
                val pages = metadata.getJSONArray("pages")
                require(pages.length() == 604)
                val wantedFiles = mutableSetOf(PREVIEW_MANIFEST)
                val foundDigests = mutableSetOf<String>()
                for (number in 1..604) {
                    val page = pages.getJSONObject(number - 1)
                    val name = "%03d.png".format(Locale.ROOT, number)
                    val expectedHash = page.getString("sha256")
                    require(page.getInt("page") == number && page.getString("file") == name)
                    require(page.getString("review_status") == "PENDING")
                    require(page.getInt("width") == 1600 && page.getInt("height") == 2287)
                    require(SHA256.matches(expectedHash) && foundDigests.add(expectedHash))
                    wantedFiles.add(name)
                    val md = MessageDigest.getInstance("SHA-256")
                    assets.open("quran/$name").use { stream ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val n = stream.read(buffer)
                            if (n < 0) break
                            md.update(buffer, 0, n)
                        }
                    }
                    val actual = md.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
                    require(actual == expectedHash)
                }
                require(assets.list("quran")?.toSet() == wantedFiles)
                QuranAssetVerifier.Status.SourceVerified(604)
            } catch (_: Exception) {
                // No fallback to partially loaded pages or synthetic Quran text.
                null
            }
        }
}
