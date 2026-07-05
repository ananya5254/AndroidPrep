package com.example.androidprep

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager

    private lateinit var etDuration: EditText
    private lateinit var cbRequireCharging: CheckBox
    private lateinit var cbRequireNetwork: CheckBox
    private lateinit var btnStartOneTime: Button
    private lateinit var tvOneTimeStatus: TextView
    private lateinit var pbOneTime: ProgressBar
    private lateinit var tvOneTimeResult: TextView

    private lateinit var btnStartPeriodic: Button
    private lateinit var tvPeriodicStatus: TextView

    private lateinit var btnCancelAll: Button
    private lateinit var tvLogs: TextView
    private lateinit var svLogs: ScrollView

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize WorkManager
        workManager = WorkManager.getInstance(applicationContext)

        // Bind views
        etDuration = findViewById(R.id.etDuration)
        cbRequireCharging = findViewById(R.id.cbRequireCharging)
        cbRequireNetwork = findViewById(R.id.cbRequireNetwork)
        btnStartOneTime = findViewById(R.id.btnStartOneTime)
        tvOneTimeStatus = findViewById(R.id.tvOneTimeStatus)
        pbOneTime = findViewById(R.id.pbOneTime)
        tvOneTimeResult = findViewById(R.id.tvOneTimeResult)

        btnStartPeriodic = findViewById(R.id.btnStartPeriodic)
        tvPeriodicStatus = findViewById(R.id.tvPeriodicStatus)

        btnCancelAll = findViewById(R.id.btnCancelAll)
        tvLogs = findViewById(R.id.tvLogs)
        svLogs = findViewById(R.id.svLogs)

        logToConsole("App initialized. WorkManager is ready.")

        setupClickListeners()
        observeWork()
    }

    private fun setupClickListeners() {
        btnStartOneTime.setOnClickListener {
            val duration = etDuration.text.toString().toIntOrNull() ?: 10
            val requiresCharging = cbRequireCharging.isChecked
            val requiresNetwork = cbRequireNetwork.isChecked

            logToConsole("Starting One-Time Work (Duration: ${duration}s, Charging: $requiresCharging, Network: $requiresNetwork)")

            // Build constraints
            val constraintsBuilder = Constraints.Builder()
            if (requiresCharging) {
                constraintsBuilder.setRequiresCharging(true)
            }
            if (requiresNetwork) {
                constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
            }

            // Build request
            val workRequest = OneTimeWorkRequestBuilder<SimpleWorker>()
                .setInputData(workDataOf(SimpleWorker.KEY_INPUT_DURATION to duration))
                .setConstraints(constraintsBuilder.build())
                .addTag(SimpleWorker.TAG)
                .build()

            // Enqueue unique work to avoid overlapping multiple demo runs
            workManager.enqueueUniqueWork(
                "one_time_work_demo",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            logToConsole("One-Time Work enqueued. ID: ${workRequest.id}")
        }

        btnStartPeriodic.setOnClickListener {
            logToConsole("Starting Periodic Work (Interval: 15 minutes)")

            // Build request (minimum interval is 15 minutes)
            val workRequest = PeriodicWorkRequestBuilder<SimpleWorker>(15, TimeUnit.MINUTES)
                .addTag("periodic_work_demo_tag")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "periodic_work_demo",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            logToConsole("Periodic Work enqueued. ID: ${workRequest.id}")
        }

        btnCancelAll.setOnClickListener {
            logToConsole("Cancelling all work...")
            workManager.cancelAllWork()
            logToConsole("All work cancellation requests sent.")
        }
    }

    private fun observeWork() {
        // Observe One-Time Work by tag
        workManager.getWorkInfosForUniqueWorkLiveData("one_time_work_demo")
            .observe(this, Observer { workInfos ->
                if (workInfos.isNullOrEmpty()) return@Observer

                val workInfo = workInfos[0]
                val state = workInfo.state
                tvOneTimeStatus.text = "Status: $state"
                logToConsole("One-Time Work state: $state")

                // Handle progress update
                val progress = workInfo.progress.getInt(SimpleWorker.KEY_PROGRESS, -1)
                if (progress != -1) {
                    pbOneTime.progress = progress
                    logToConsole("One-Time Work progress: $progress%")
                }

                when (state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val result = workInfo.outputData.getString(SimpleWorker.KEY_OUTPUT_RESULT)
                        tvOneTimeResult.text = "Result: $result"
                        logToConsole("One-Time Work Success Output: $result")
                    }
                    WorkInfo.State.FAILED -> {
                        val result = workInfo.outputData.getString(SimpleWorker.KEY_OUTPUT_RESULT)
                            ?: "Failed without details"
                        tvOneTimeResult.text = "Result: $result"
                        logToConsole("One-Time Work Failure Output: $result")
                    }
                    WorkInfo.State.CANCELLED -> {
                        tvOneTimeResult.text = "Result: Worker Cancelled"
                        logToConsole("One-Time Work Cancelled.")
                        pbOneTime.progress = 0
                    }
                    else -> {
                        // Work is still enqueued, running, or blocked
                        if (state != WorkInfo.State.RUNNING) {
                            tvOneTimeResult.text = "Result: N/A"
                        }
                    }
                }
            })

        // Observe Periodic Work by unique name
        workManager.getWorkInfosForUniqueWorkLiveData("periodic_work_demo")
            .observe(this, Observer { workInfos ->
                if (workInfos.isNullOrEmpty()) return@Observer

                val workInfo = workInfos[0]
                val state = workInfo.state
                tvPeriodicStatus.text = "Status: $state"
                logToConsole("Periodic Work state: $state")
            })
    }

    private fun logToConsole(message: String) {
        val timeString = dateFormat.format(Date())
        val formattedLog = "[$timeString] $message\n"
        tvLogs.append(formattedLog)
        
        // Auto scroll to bottom
        svLogs.post {
            svLogs.fullScroll(ScrollView.FOCUS_DOWN)
        }
        Log.d("WorkManagerDemo", message)
    }
}