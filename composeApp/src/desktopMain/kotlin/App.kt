import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ennity.ADBDevice
import ennity.ADBUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tvboxassistant.composeapp.generated.resources.Res
import tvboxassistant.composeapp.generated.resources.ic_launcher
import tvboxassistant.composeapp.generated.resources.ic_xiala
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        var adbUIState by remember {
            mutableStateOf<ADBUIState>(
                ADBUIState(
                    (mutableListOf<ADBDevice>()),
                    isUseUSBConnect = false,
                    isDetecting = false
                )
            )
        }
        val uiScope = rememberCoroutineScope()

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3C7AF2), Color(0xFF6B9CFC)),
                    )
                ), contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painter = painterResource(Res.drawable.ic_launcher), "")
                    Spacer(modifier = Modifier.padding(10.dp))

                    AnimatedVisibility(adbUIState.isUseUSBConnect) {
                        Text("若未安装usb驱动,点击安装usb驱动", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Box(Modifier.width(80.dp)) {
                            Text("连接方式", color = Color.White)
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        DropdownMenuExample(adbUIState.isUseUSBConnect) {
                            adbUIState = adbUIState.copy(isUseUSBConnect = it)
                        }
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    AnimatedVisibility(!adbUIState.isUseUSBConnect) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(Modifier.width(80.dp)) {
                                Text("端口号", color = Color.White)
                            }
                            Spacer(modifier = Modifier.padding(10.dp))

                            RoundedCornerBorderBackground(modifier = Modifier.width(300.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        // 设置文本值和更新文本的回调
                                        value = adbUIState.portStr,
                                        onValueChange = { newText ->
                                            adbUIState = adbUIState.copy(portStr = newText)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.body1,
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.clickable {
                        if (adbUIState.isDetecting) {
                            return@clickable
                        }
                        adbUIState = adbUIState.copy(isDetecting = true)
                        uiScope.launch {
                            val ret = mutableListOf<ADBDevice>()

                            try {
                                val dadbs = if (adbUIState.isUseUSBConnect) {
                                    ADBDetector.detectByUSB()
                                } else {
                                    ADBDetector.detectByIp(adbUIState.portStr)
                                }

                                dadbs.forEach {
                                    ret.add(ADBDevice(it.toString(), false, "").apply {
                                        adb = it
                                    })
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()

                                snackbarHostState.showSnackbar(
                                    message = "探测成功 失败 - ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            } finally {
                                if (ret.isEmpty()) {
                                    snackbarHostState.showSnackbar(
                                        message = "没有探测的任何设备～",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                adbUIState = adbUIState.copy(
                                    isDetecting = false,
                                    adbDevices = (ret),
                                    showDevList = ret.isNotEmpty()
                                )
                            }
                        }
                    }, contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier.size(100.dp).background(Color(0x55FFFFFF), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {}
                        TwoArcLoading(modifier = Modifier.width(200.dp).height(200.dp), adbUIState.isDetecting)
                        Text("扫描", color = Color.Black)
                    }
                }

                if (adbUIState.showDevList) {
                    DeviceRetView(adbUIState.adbDevices, snackbarHostState, {
                        uiScope.launch {
                            snackbarHostState.showSnackbar("设备列表已经重制")
                        }
                        adbUIState.resetDevices()
                        adbUIState.adbDevices.clear()
                        adbUIState = adbUIState.copy(showDevList = false)
                    }, {
                        val newList = mutableListOf<ADBDevice>()
                        newList.addAll(adbUIState.adbDevices)
                        println("跟新列表")
                        adbUIState = adbUIState.copy(adbDevices = (newList), version = adbUIState.version + 1)
                    })
                }
            }
        }

    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun DropdownMenuExample(useUSBConnect: Boolean, onSelect: (Boolean) -> Unit) {
    val options = listOf("局域网连接", "usb连接")
    val expanded = remember { mutableStateOf(false) }
    val selectedIndex = if (useUSBConnect) {
        1
    } else {
        0
    }

    Column {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    onSelect(index == 1)
                    expanded.value = false
                }) {
                    Text(option)
                }
            }
        }

        RoundedCornerBorderBackground(modifier = Modifier.width(300.dp).clickable {
            expanded.value = true
        }) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(options[selectedIndex], color = Color.White)
                Image(painter = painterResource(Res.drawable.ic_xiala), "")

            }
        }

    }
}

@Composable
fun RoundedCornerBorderBackground(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.background(
            color = Color(0x55FFFFFF),
            shape = RoundedCornerShape(16.dp) // 设置圆角半径为 16dp
        )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp) // 设置边框圆角半径为 16dp，与背景相同
            ).padding(8.dp), content = content
    )
}

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
                                    val bs = Res.readBytes("files/app-noui.apk")
                                    val fos = FileOutputStream(file)
                                    fos.write(bs)
                                    fos.close()
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
                                            // it.adb?.install(file)

                                            dev.adb?.openShell().use { shellStream ->
                                                shellStream?.write("echo hello\n")

                                                val shellPacket = shellStream?.read()
                                                println("shell packet: $shellPacket")

                                                shellStream?.write("am start -n com.qiniu.upd.app/com.qiniu.upd.app.MainActivity\n")

                                                val shellResponse = shellStream?.read()
                                                println("shell packet: $shellResponse")

                                                delay(1000)
                                                shellStream?.write("cat  /data/user/0/com.qiniu.upd.app/files/nodeID/nodeID.txt \n")

                                                val nodeID1 = shellStream?.read()?.toString() ?: ""
                                                println("shell packet: $nodeID1")

                                                if (nodeID1.startsWith("STDOUT: ant")) {
                                                    dev.deviceID = nodeID1.replace("STDOUT: ", "")
                                                } else {
                                                    shellStream?.write("cat  /data/user_de/0/com.qiniu.upd.app/files/nodeID/nodeID.txt \n")
                                                    val nodeID2 = shellStream?.read()?.toString() ?: ""
                                                    println("shell packet: $nodeID2")
                                                    if (nodeID2.startsWith("STDOUT: ant")) {
                                                        dev.deviceID = nodeID2.replace("STDOUT: ", "")
                                                    }
                                                }

                                                shellStream?.write("exit\n")
                                                val shellResponse2 = shellStream?.readAll()
                                                println("shell packet: $shellResponse2")

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

@Composable
fun DeviceItem(device: ADBDevice) {
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
        if (device.deviceID.isNotEmpty()) {
            Text("节点id:" + device.deviceID)
        }
    }

}
