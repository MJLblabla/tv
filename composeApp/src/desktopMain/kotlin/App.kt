import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dadb.Dadb
import ennity.ADBDevice
import ennity.ADBUIState
import ennity.DetectorType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tvboxassistant.composeapp.generated.resources.Res
import tvboxassistant.composeapp.generated.resources.ic_launcher

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        var adbUIState by remember {
            mutableStateOf<ADBUIState>(ADBUIState((mutableListOf<ADBDevice>()), isDetecting = false))
        }
        val uiScope = rememberCoroutineScope()
        var adbPathStr by remember { mutableStateOf("") }

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

                    AnimatedVisibility(adbUIState.detectorType == DetectorType.USB) {
                        Box(modifier = Modifier.clickable {
                            Util.openBrowserDownAdb()
                        }) {
                            Text("若未安装usb驱动,点击安装usb驱动", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Box(Modifier.width(80.dp)) {
                            Text("连接方式", color = Color.White)
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        DetectTypeDropdownMenu(adbUIState.detectorType) {
                            adbUIState = adbUIState.copy(detectorType = it)
                        }
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                    AnimatedVisibility(DetectorType.LAN == adbUIState.detectorType) {
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

                    Spacer(modifier = Modifier.padding(10.dp))
                    AnimatedVisibility(DetectorType.USB == adbUIState.detectorType) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(Modifier.width(80.dp)) {
                                Text("adb文件路径(可选)", color = Color.White)
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
                                        value = adbPathStr,
                                        onValueChange = { newText ->
                                            adbPathStr = newText
                                            ADBDetector.adbPath = newText
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.body1,
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                    AnimatedVisibility(DetectorType.IP == adbUIState.detectorType) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            RoundedCornerBorderBackground(modifier = Modifier.width(160.dp)) {
                                BasicTextField(
                                    value = adbUIState.ip,
                                    onValueChange = { newText ->
                                        adbUIState = adbUIState.copy(ip = newText)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.body1,
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            if (adbUIState.ip.isEmpty()) {
                                                Text(
                                                    text = "请输入盒子ip",
                                                    style = TextStyle(color = Color.Gray)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                            RoundedCornerBorderBackground(modifier = Modifier.width(160.dp)) {
                                BasicTextField(
                                    value = adbUIState.portStr,
                                    onValueChange = { newText ->
                                        adbUIState = adbUIState.copy(portStr = newText)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.body1,
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            if (adbUIState.portStr.isEmpty()) {
                                                Text(
                                                    text = "请输入端口号",
                                                    style = TextStyle(color = Color.Gray)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                    }


                    Box(modifier = Modifier.clickable {
                        if (adbUIState.isDetecting) {
                            return@clickable
                        }
                        adbUIState = adbUIState.copy(isDetecting = true)

                        if (adbUIState.detectorType == DetectorType.LAN || adbUIState.detectorType == DetectorType.IP) {
                            if (adbUIState.portStr.isEmpty()) {
                                uiScope.launch {
                                    snackbarHostState.showSnackbar("请输入端口号", duration = SnackbarDuration.Short)
                                }
                                return@clickable
                            }
                        }
                        if (adbUIState.detectorType == DetectorType.IP) {
                            if (adbUIState.ip.isEmpty()) {
                                uiScope.launch {
                                    snackbarHostState.showSnackbar("请输入盒子ip", duration = SnackbarDuration.Short)
                                }
                                return@clickable
                            }
                        }

                        uiScope.launch {
                            val ret = mutableListOf<ADBDevice>()

                            try {
                                val dadbs = when (adbUIState.detectorType) {
                                    DetectorType.USB -> ADBDetector.detectByUSB()
                                    DetectorType.IP -> ADBDetector.detectByIP(adbUIState.ip, adbUIState.portStr)
                                    else -> ADBDetector.detectByLan(adbUIState.portStr)
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


