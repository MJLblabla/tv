package ennity

import androidx.compose.runtime.Immutable
import dadb.Dadb

data class ADBDevice(val devName: String, var isSelect: Boolean, var deviceID: String = "") {
    var adb: Dadb? = null
}

enum class DetectorType(val str: String) {
    LAN("局域网"),
    USB("usb"),
    IP("ip-端口")
}

data class ADBUIState(
    val adbDevices: MutableList<ADBDevice>,
    val detectorType: DetectorType = DetectorType.LAN,
    val isDetecting: Boolean,
    val portStr: String = "5555",
    val ip: String = "",
    val showDevList: Boolean = false,
    val version: Int = 0
) {
    fun resetDevices() {
        adbDevices.forEach {
            try {
                it.adb?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        adbDevices.clear()
    }
}