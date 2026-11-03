package com.example.paceburn.quotes

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.paceburn.data.PreferencesManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyQuoteScheduler {

    const val WORK_TAG = "paceburn_daily_quote_work"

    fun scheduleDailyQuoteWork(context: Context) {
        val prefs = PreferencesManager(context)
        if (!prefs.isDailyQuotesEnabled()) {
            cancelDailyQuoteWork(context)
            return
        }

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }

    fun cancelDailyQuoteWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
    }
}
