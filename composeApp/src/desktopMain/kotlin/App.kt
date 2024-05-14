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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ennity.ADBDevice
import ennity.ADBUIState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tvboxassistant.composeapp.generated.resources.Res
import tvboxassistant.composeapp.generated.resources.ic_launcher
import tvboxassistant.composeapp.generated.resources.ic_xiala

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
                        Box(modifier = Modifier.clickable {
                            Util.openBrowserDownAdb()
                        }){
                            Text("若未安装usb驱动,点击安装usb驱动", color = Color.White, fontSize = 12.sp)
                        }
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
