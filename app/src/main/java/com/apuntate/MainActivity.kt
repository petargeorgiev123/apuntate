package com.apuntate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apuntate.auth.AuthRepository
import com.apuntate.cloud.CloudRepository
import com.apuntate.ui.RootApp
import com.apuntate.ui.theme.ApuntateTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa Firebase (usa google-services.json).
        FirebaseApp.initializeApp(this)

        val authRepo = AuthRepository()
        val cloudRepo = CloudRepository()

        setContent {
            ApuntateTheme {
                RootApp(authRepo = authRepo, cloudRepo = cloudRepo)
            }
        }
    }
}
