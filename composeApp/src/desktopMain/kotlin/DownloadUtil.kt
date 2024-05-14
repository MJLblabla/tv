import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

object DownloadUtil {
    private val client = HttpClient(CIO)

    @OptIn(InternalAPI::class)
    suspend fun downloadFileWithProgress(
        url: String, file: File, onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ) {
        val response: HttpResponse = client.get(url)

        val totalBytes = response.contentLength() ?: 1 // Default to 1 if content length is unknown

        var bytesDownloaded = 0L

        withContext(Dispatchers.IO) {
            file.outputStream().use { outputStream ->
                response.content.readAvailable { buffer ->
                    val size = buffer.remaining().toLong()
                    bytesDownloaded += size

                    outputStream.write(buffer.array(), buffer.position(), buffer.remaining())

                    onProgress(bytesDownloaded, totalBytes)
                }
            }
        }

    }
}