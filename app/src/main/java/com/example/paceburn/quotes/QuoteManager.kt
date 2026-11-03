package com.example.paceburn.quotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.paceburn.MainActivity
import com.example.paceburn.R
import com.example.paceburn.data.PreferencesManager
import java.util.Calendar

data class MotivationalQuote(
    val quote: String,
    val author: String
)

object QuoteManager {

    const val QUOTE_CHANNEL_ID = "paceburn_quotes_channel"
    private const val NOTIFICATION_ID = 3001

    private val quotes = listOf(
        MotivationalQuote("The journey of a thousand miles begins with a single step.", "Lao Tzu"),
        MotivationalQuote("Small daily improvements over time lead to stunning results.", "Robin Sharma"),
        MotivationalQuote("Every step you take is a vote for the person you want to become.", "James Clear"),
        MotivationalQuote("Action is the foundational key to all success.", "Pablo Picasso"),
        MotivationalQuote("Your body can stand almost anything. It's your mind that you have to convince.", "Unknown"),
        MotivationalQuote("Success starts with self-discipline.", "Dwayne Johnson"),
        MotivationalQuote("Step by step, day by day, you are building a stronger version of yourself.", "PaceBurn"),
        MotivationalQuote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
        MotivationalQuote("Don't count the days, make the days count.", "Muhammad Ali"),
        MotivationalQuote("A 30-minute walk is only 2% of your day. Make it count!", "PaceBurn"),
        MotivationalQuote("The secret of getting ahead is getting started.", "Mark Twain"),
        MotivationalQuote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        MotivationalQuote("Consistency is the key that unlocks all fitness achievements.", "PaceBurn"),
        MotivationalQuote("Wake up with determination. Go to bed with satisfaction.", "Unknown"),
        MotivationalQuote("Walk with heart. Run with passion. Burn with purpose.", "PaceBurn"),
        MotivationalQuote("It always seems impossible until it's done.", "Nelson Mandela"),
        MotivationalQuote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar"),
        MotivationalQuote("Keep moving forward. Every step counts toward your daily burn!", "PaceBurn")
    )

    fun getTodayQuote(): MotivationalQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear % quotes.size).coerceIn(0, quotes.size - 1)
        return quotes[index]
    }

    fun getRandomQuote(): MotivationalQuote {
        return quotes.random()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                QUOTE_CHANNEL_ID,
                "Daily Motivational Quotes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily inspiring quotes to encourage walking and fitness"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendQuoteNotification(context: Context, quote: MotivationalQuote = getTodayQuote()) {
        val prefs = PreferencesManager(context)
        if (!prefs.isDailyQuotesEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationContent = "\"${quote.quote}\"\n— ${quote.author}"

        val notification = NotificationCompat.Builder(context, QUOTE_CHANNEL_ID)
            .setContentTitle("💡 Daily Motivation")
            .setContentText("\"${quote.quote}\" — ${quote.author}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationContent)
                    .setBigContentTitle("💡 Daily Motivation")
                    .setSummaryText("PaceBurn Fitness")
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
