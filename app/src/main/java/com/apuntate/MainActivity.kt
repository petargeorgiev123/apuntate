package com.apuntate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apuntate.data.ApuntateDatabase
import com.apuntate.data.ApuntateRepository
import com.apuntate.ui.ApuntateApp
import com.apuntate.ui.theme.ApuntateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = ApuntateRepository(
            ApuntateDatabase.getInstance(applicationContext).apuntateDao()
        )

        setContent {
            ApuntateTheme {
                ApuntateApp(repo)
            }
        }
    }
}
