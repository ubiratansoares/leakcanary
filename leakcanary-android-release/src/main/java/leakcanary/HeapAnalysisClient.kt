package leakcanary

import android.app.Application
import leakcanary.internal.RealHeapAnalysisJob
import leakcanary.internal.RealHeapAnalysisJob.Companion.HPROF_PREFIX
import leakcanary.internal.RealHeapAnalysisJob.Companion.HPROF_SUFFIX
import java.io.File

class HeapAnalysisClient(
  private val heapDumpDirectory: File,
  private val config: HeapAnalysisConfig,
  private val interceptors: List<HeapAnalysisInterceptor>
) {
  fun newJob(startReason: String): HeapAnalysisJob {
    return RealHeapAnalysisJob(startReason, heapDumpDirectory, config, interceptors)
  }

  fun deleteHeapDumpFiles() {
    val heapDumpFiles = heapDumpDirectory.listFiles { _, name ->
      name.startsWith(HPROF_PREFIX) && name.endsWith(HPROF_SUFFIX)
    }
    heapDumpFiles?.forEach { it.delete() }
  }

  companion object {
    // TODO Where should we move this?
    fun defaultInterceptors(application: Application): List<HeapAnalysisInterceptor> {
      return listOf(
        GoodAndroidVersionInterceptor(),
        MinimumDiskSpaceInterceptor(application),
        MinimumMemoryInterceptor(application),
        MinimumElapsedSinceStartInterceptor(),
        OncePerPeriodInterceptor(application),
        SaveResourceIdsInterceptor(application.resources)
      )
    }
  }
}
