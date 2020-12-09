package leakcanary

import shark.HeapAnalysis

/**
 * A [HeapAnalysisJob] represents a single prepared request to analyze the heap. It cannot be
 * executed twice.
 */
interface HeapAnalysisJob {

  val startReason: String

  /**
   * true if [execute] has been called. It is an
   * error to call [execute] more than once.
   */
  val executed: Boolean

  /**
   * true of [cancel] has been called or if an [HeapAnalysisInterceptor] has returned
   * [Result.Canceled] from [HeapAnalysisInterceptor.intercept].
   */
  val canceled: Boolean

  /**
   * Starts the analysis job immediately, and blocks until a result is available.
   *
   * @return Either [Result.Done] if the analysis was attempted or [Result.Canceled]
   */
  fun execute(): Result

  /** Cancels the job, if possible. Jobs that are already complete cannot be canceled. */
  fun cancel(cancelReason: String)

  sealed class Result {
    abstract val startReason: String

    data class Done(
      override val startReason: String,
      val analysis: HeapAnalysis
    ) : Result()

    data class Canceled(
      override val startReason: String,
      val cancelReason: String
    ) : Result()
  }
}