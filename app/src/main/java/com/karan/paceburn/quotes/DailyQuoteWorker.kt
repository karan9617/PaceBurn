package com.karan.paceburn.quotes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyQuoteWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        QuoteManager.sendQuoteNotification(applicationContext)
        // Reschedule for next day to ensure continuous daily quotes
        DailyQuoteScheduler.scheduleDailyQuoteWork(applicationContext)
        return Result.success()
    }
}
