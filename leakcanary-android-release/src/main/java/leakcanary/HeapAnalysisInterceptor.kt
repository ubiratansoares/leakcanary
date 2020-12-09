package leakcanary

fun interface HeapAnalysisInterceptor {

  fun intercept(chain: Chain): HeapAnalysisJob.Result

  interface Chain {
    val job: HeapAnalysisJob

    fun proceed(): HeapAnalysisJob.Result

    fun canceled(cancelReason: String): HeapAnalysisJob.Result.Canceled
  }
}