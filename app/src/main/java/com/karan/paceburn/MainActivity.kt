package com.karan.paceburn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.karan.paceburn.quotes.DailyQuoteScheduler
import com.karan.paceburn.ui.PaceBurnApp
import com.karan.paceburn.ui.theme.PaceBurnTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Schedule daily motivational quote notifications
        DailyQuoteScheduler.scheduleDailyQuoteWork(this)

        // Configure clean User-Agent without com.example to comply with open map tile server policies
        Configuration.getInstance().userAgentValue = "Mozilla/5.0 (Android 14; Mobile Fitness) PaceBurn/1.0"
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))

        enableEdgeToEdge()
        setContent {
            PaceBurnTheme {
                PaceBurnApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaceBurnPreview() {
    PaceBurnTheme {
        PaceBurnApp()
    }
}
