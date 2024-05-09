package ennity

import androidx.compose.runtime.Immutable
import dadb.Dadb

data class ADBDevice(val devName: String, var isSelect: Boolean, val deviceID: String) {
    var adb: Dadb? = null
}

@Immutable
data class ADBDeviceCollection(val adbDevices: MutableList<ADBDevice>)

data class ADBUIState(
    val adbCollection: ADBDeviceCollection,
    val isStarting: Boolean,
    val isUseUSBConnect: Boolean,
    val isDetecting: Boolean,
    val portStr: String = "5555",
    val showDevList: Boolean = false
) {
    fun resetDevices() {
        adbCollection.adbDevices.forEach {
            try {
                it.adb?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        adbCollection.adbDevices.clear()
    }
}