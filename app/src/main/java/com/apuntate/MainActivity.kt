package com.apuntate

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.apuntate.auth.AuthRepository
import com.apuntate.cloud.CloudRepository
import com.apuntate.notif.Notificaciones
import com.apuntate.ui.RootApp
import com.apuntate.ui.theme.ApuntateTheme
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private val pedirPermisoNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* resultado ignorado */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa Firebase y el canal de notificaciones.
        FirebaseApp.initializeApp(this)
        Notificaciones.crearCanal(this)

        // Pide permiso de notificaciones en Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificaciones.tienePermiso(this)) {
            pedirPermisoNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val authRepo = AuthRepository()
        val cloudRepo = CloudRepository()

        setContent {
            ApuntateTheme {
                RootApp(authRepo = authRepo, cloudRepo = cloudRepo)
            }
        }
    }
}
