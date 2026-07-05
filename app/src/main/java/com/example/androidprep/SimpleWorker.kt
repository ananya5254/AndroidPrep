package com.example.androidprep

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class SimpleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "SimpleWorker"
        const val KEY_INPUT_DURATION = "input_duration"
        const val KEY_OUTPUT_RESULT = "output_result"
        const val KEY_PROGRESS = "progress"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "SimpleWorker started execution")

        // Retrieve input data
        val durationSeconds = inputData.getInt(KEY_INPUT_DURATION, 5)
        Log.d(TAG, "Input duration: $durationSeconds seconds")

        // Initial progress
        setProgress(workDataOf(KEY_PROGRESS to 0))

        try {
            for (i in 1..durationSeconds) {
                // Check if worker was cancelled / stopped
                if (isStopped) {
                    Log.d(TAG, "SimpleWorker stopped/cancelled")
                    return Result.failure(
                        workDataOf(KEY_OUTPUT_RESULT to "Worker was stopped/cancelled at step $i")
                    )
                }

                // Simulate progress
                delay(1000)
                val progressPercent = (i * 100) / durationSeconds
                Log.d(TAG, "Progress updated: $progressPercent%")
                setProgress(workDataOf(KEY_PROGRESS to progressPercent))
            }

            Log.d(TAG, "SimpleWorker completed successfully")
            // Create output data
            val outputData = workDataOf(
                KEY_OUTPUT_RESULT to "Completed successfully after $durationSeconds seconds!"
            )
            return Result.success(outputData)

        } catch (e: Exception) {
            Log.e(TAG, "Error in SimpleWorker", e)
            return Result.failure(
                workDataOf(KEY_OUTPUT_RESULT to "Failed due to: ${e.localizedMessage}")
            )
        }
    }
}
