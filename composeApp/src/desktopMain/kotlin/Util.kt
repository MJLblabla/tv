import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.nio.file.Paths
import javax.swing.filechooser.FileSystemView
import kotlin.coroutines.EmptyCoroutineContext

object Util {

    fun openBrowserDownAdb() {
        val url: String = "https://sdk-release.qnsdk.com/platform-tools-latest-windows.zip"
        CoroutineScope(EmptyCoroutineContext).launch {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }

}