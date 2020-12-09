package leakcanary

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.Application
import android.content.Context
import android.os.Build
import leakcanary.HeapAnalysisInterceptor.Chain

class MinimumMemoryInterceptor(
  private val application: Application,
  private val minimumRequiredAvailableMemoryBytes: Long = 100_000_000,
) : HeapAnalysisInterceptor {


  private val memoryInfo = MemoryInfo()

  override fun intercept(chain: Chain): HeapAnalysisJob.Result {
    val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    if (Build.VERSION.SDK_INT >= 19 && activityManager.isLowRamDevice) {
      return chain.canceled("low ram device")
    }
    activityManager.getMemoryInfo(memoryInfo)

    if (memoryInfo.lowMemory || memoryInfo.availMem <= memoryInfo.threshold) {
      return chain.canceled("low memory")
    }
    val systemAvailableMemory = memoryInfo.availMem - memoryInfo.threshold

    val runtime = Runtime.getRuntime()
    val appUsedMemory = runtime.totalMemory() - runtime.freeMemory()
    val appAvailableMemory = runtime.maxMemory() - appUsedMemory

    val availableMemory = systemAvailableMemory.coerceAtMost(appAvailableMemory)
    return if (availableMemory >= minimumRequiredAvailableMemoryBytes) {
      chain.proceed()
    } else {
      chain.canceled(
        "not enough free memory: available $availableMemory < min $minimumRequiredAvailableMemoryBytes"
      )
    }
  }
}