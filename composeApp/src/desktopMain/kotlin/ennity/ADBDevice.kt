package ennity

import androidx.compose.runtime.Immutable
import dadb.Dadb

data class ADBDevice(val devName: String, var isSelect: Boolean, var deviceID: String = "") {
    var adb: Dadb? = null
}

data class ADBUIState(
    val adbDevices: MutableList<ADBDevice>,
    val isUseUSBConnect: Boolean,
    val isDetecting: Boolean,
    val portStr: String = "5555",
    val showDevList: Boolean = false,
    val version:Int = 0
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