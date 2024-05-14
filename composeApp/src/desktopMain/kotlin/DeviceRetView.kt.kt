import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.unit.dp
import ennity.ADBDevice
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import tvboxassistant.composeapp.generated.resources.Res
import tvboxassistant.composeapp.generated.resources.ic_qrcode
import tvboxassistant.composeapp.generated.resources.ic_xiala
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DeviceRetView(
    adbDevices: List<ADBDevice>,
    snackbarHostState: SnackbarHostState,
    dismissCallback: () -> Unit,
    updaterCallback: () -> Unit
) {
    println("DeviceRetView")

    val isStarting = remember { mutableStateOf(false) }
    val uiScope = rememberCoroutineScope()
    Box(
        modifier = Modifier.background(color = Color(0x55000000)).fillMaxSize().clickable {
            if (isStarting.value) {
                return@clickable
            }
            dismissCallback.invoke()
        },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(100.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(8.dp), elevation = 30.dp, modifier = Modifier.clickable { }) {
                Column(
                    Modifier.padding(24.dp), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    adbDevices.forEach {
                        DeviceItem(it)
                    }
                    Button(onClick = {

                        if (isStarting.value) {
                            return@Button
                        }
                        var hastSelect = false
                        adbDevices.forEach {
                            if (it.isSelect) {
                                hastSelect = true
                            }
                        }
                        if (!hastSelect) {
                            uiScope.launch {
                                snackbarHostState.showSnackbar("没有设备选中", duration = SnackbarDuration.Short)
                            }
                            return@Button
                        }

                        val file = File("files/app-noui.apk")
                        println("  " + file.exists() + "  " + file.absolutePath)

                        uiScope.launch {
                            isStarting.value = true
                            try {
                                if (!file.exists()) {
                                    val dir = File("files/")
                                    val ret = dir.mkdirs()
                                    println("创建文件结果 $ret")
//                                    val bs = Res.readBytes("files/app-noui.apk")
//                                    val fos = FileOutputStream(file)
//                                    fos.write(bs)
//                                    fos.close()


                                    val downFile = File("files/app-noui-temp.apk")
                                    DownloadUtil.downloadFileWithProgress(
                                        "https://download.niulinkcloud.com/NiuLinkNodeApps/minibox/app-noui-dagongzai-release-jg.apk",
                                        downFile
                                    ) { bytesDownloaded: Long, totalBytes: Long ->
                                        println("下载进度  $bytesDownloaded  $totalBytes")
                                    }
                                    println("下载进度 完成")
                                    downFile.renameTo(file)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                snackbarHostState.showSnackbar("文件读取失败")
                            }
                            var hasInstall = false
                            val ret = async(Dispatchers.IO) {
                                adbDevices.forEach { dev ->
                                    if (dev.isSelect) {
                                        try {
                                            println("安装文件 " + dev.adb + "  " + file.absolutePath)
                                            dev.adb?.push(file, "/sdcard/${file.name}")

                                            dev.adb?.openShell("su \n").use { shellStream ->
                                                //  shellStream?.write("")
                                                //  val shellRootPacket = shellStream?.read()
                                                //  println("shell shellRootPacket: $shellRootPacket")
                                                delay(1000)
                                                //  shellStream?.write("pm uninstall -k --user 0 com.qiniu.upd.app")
                                                shellStream?.write("pm install -r -d  /sdcard/${file.name}\n")

                                                val shellPacket = shellStream?.read()

                                                println("shell install packet: $shellPacket")

                                                shellStream?.write("am start -n com.qiniu.upd.app/com.qiniu.upd.app.MainActivity\n")

                                                shellStream?.write("exit\n")

                                            }

                                            dev.adb?.openShell("su \n").use { shellStream ->
                                                shellStream?.write("cat  /data/user/0/com.qiniu.upd.app/files/nodeID/nodeID.txt \n")

                                                val nodeID1 = shellStream?.read()?.toString() ?: ""
                                                println("shell packet1: $nodeID1")

                                                if (nodeID1.startsWith("STDOUT: ant")) {
                                                    dev.deviceID = nodeID1.replace("STDOUT: ", "")
                                                }
                                                shellStream?.write("exit\n")
                                            }
                                            if (dev.deviceID.isEmpty()) {
                                                dev.adb?.openShell("su \n").use { shellStream ->
                                                    shellStream?.write("cat  /data/user_de/0/com.qiniu.upd.app/files/nodeID/nodeID.txt \n")

                                                    val nodeID = shellStream?.read()?.toString() ?: ""
                                                    println("shell packet1: $nodeID")

                                                    if (nodeID.startsWith("STDOUT: ant")) {
                                                        dev.deviceID = nodeID.replace("STDOUT: ", "")
                                                    }
                                                    shellStream?.write("exit\n")
                                                }
                                            }

                                            hasInstall = true
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                            ret.await()
                            isStarting.value = false
                            if (hasInstall) {
                                updaterCallback.invoke()
                            }
                        }

                    }) {
                        val text = if (isStarting.value) {
                            "进行中"
                        } else {
                            "开始"
                        }
                        Text(text)
                    }
                }
            }
        }

    }

}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DeviceItem(device: ADBDevice) {

    var showDialog by remember { mutableStateOf(false) }
    println("DeviceItem ${device.deviceID}")
    val isSelect = remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("设备:" + device.devName)
            Spacer(modifier = Modifier.padding(10.dp))
            Checkbox(
                checked = isSelect.value,
                onCheckedChange = { newChecked ->
                    device.isSelect = newChecked
                    isSelect.value = newChecked
                }
            )
        }
        Spacer(modifier = Modifier.padding(10.dp))
        if (device.deviceID.isNotEmpty()) {
            Text("节点id:")
            Spacer(modifier = Modifier.padding(10.dp))
            Image(
                painter = painterResource(Res.drawable.ic_qrcode),
                contentDescription = "",
                modifier = Modifier.clickable {
                    showDialog = true
                })
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("绑定节点") },
            text = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberQrCodePainter("https://www.niulinkcloud.com/antbind?device_id=${device.deviceID}"),
                        contentDescription = "QR code"
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

}
