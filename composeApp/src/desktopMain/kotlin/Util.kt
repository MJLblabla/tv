import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.EmptyCoroutineContext

object Util {

    fun openBrowserDownAdb() {
        val url: String = "https://developer.android.com/tools/adb?hl=zh-cn"
        CoroutineScope(EmptyCoroutineContext).launch {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }
}