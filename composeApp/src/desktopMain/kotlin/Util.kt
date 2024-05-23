import dadb.AdbKeyPair
import dadb.AdbKeyPair.Companion.generate
import dadb.AdbKeyPair.Companion.read
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
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

    fun getHomeDir(): File {
        val homeDir = System.getProperty("user.home")
        val dir = File(homeDir,".tvboxassistant")
        val ret = dir.mkdirs()
        return dir
    }
    fun readDefault(): AdbKeyPair {
        val homeDir = getHomeDir()
        val privateKeyFile = File(homeDir,"adbkey")
        val publicKeyFile = File(homeDir,"adbkey.pub")

        if (!privateKeyFile.exists()) {
            generate(privateKeyFile, publicKeyFile)
        }

        return read(privateKeyFile, publicKeyFile)
    }
}