import dadb.Dadb
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


object ADBDetector {

    fun getLocalNetworkIpAddress(): String {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        var localIp: String? = null

        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val interfaceAddresses = networkInterface.inetAddresses

            while (interfaceAddresses.hasMoreElements()) {
                val address = interfaceAddresses.nextElement()

                if (!address.isLoopbackAddress && address.hostAddress.contains(":").not()) {
                    localIp = address.hostAddress
                }
            }
        }

        return localIp ?: ""
    }


    private fun ping(host: String, port: Int): Boolean {
        val socket = Socket()
        var ret = false
        try {
            val address = InetAddress.getByName(host)
            val socketAddress = InetSocketAddress(address, port)
            socket.connect(socketAddress, 500) // 设置连接超时时间为1秒
            System.out.println("Host: $host is reachable on port $port")
            ret = true
        } catch (e: IOException) {
            // 端口不可达
            // e.printStackTrace()
        } finally {
            socket.close()
        }
        return ret
    }

    suspend fun detectByIp(port: String) = suspendCoroutine<List<Dadb>> { continuation ->

        Thread {
            val startTime = System.currentTimeMillis()
            val retList = mutableListOf<Dadb>()
            val threadPool = Executors.newFixedThreadPool(16)
            try {

                val hostStr = getLocalNetworkIpAddress()
                println("本机地址 ${hostStr}")
                val firstThreeNumbers = hostStr.split(".").take(3).joinToString(".")
                if (hostStr.isEmpty()) {
                    throw Exception("获取本机ip地址失败～")
                }

                threadPool.execute {
                    println(" 开始 目标ip 100.100.108.113  ")
                    val ret = ping("100.100.108.18", port.toInt())
                    println(" 目标ip 100.100.108.113  是否可达 $ret")
                    if (ret) {
                        val adb = Dadb.create("100.100.108.18", port.toInt())
                        try {
                            adb.openShell("pwd")
                            retList.add(adb)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                for (i in 0..255) {
                    val targetIP = "$firstThreeNumbers.$i"
                    threadPool.execute {
                        println(" 开始 目标ip $targetIP  ")
                        val ret = ping(targetIP, port.toInt())
                        println(" 目标ip $targetIP  是否可达 $ret")
                        if (ret) {
                            val adb = Dadb.create(targetIP, port.toInt())
                            try {
                                adb.openShell("pwd")
                                retList.add(adb)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resumeWithException(e)
            }

            try {
                threadPool.shutdown()
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val now = System.currentTimeMillis()
            println("adb 探测结果 ${retList.size} cost ${now - startTime} ms")
            continuation.resume(retList)
        }.start()

    }

    suspend fun detectByUSB() = suspendCoroutine<List<Dadb>> { continuation ->
        Thread {
            try {
                val dadbs = Dadb.list()
                println("adb 探测结果 ${dadbs.size} ")
                continuation.resume(dadbs)
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resumeWithException(e)
            }
        }.start()

    }


}