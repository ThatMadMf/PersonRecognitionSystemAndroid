package com.example.person_recognition_system

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.person_recognition_system.databinding.ActivityMainBinding
import com.example.person_recognition_system.services.SocketClientCallbacks
import com.example.person_recognition_system.services.WebsocketClient
import com.example.person_recognition_system.services.WebsocketClient.LocalBinder
import com.example.person_recognition_system.ui.dashboard.DashboardFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.URI


class MainActivity : AppCompatActivity(), SocketClientCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: NavController
    private lateinit var navView: BottomNavigationView
    private var bound = false

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, WebsocketClient::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            socketClient!!.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request camera permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS,
            )
        }

        navView = binding.navView

        controller = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.face_capture,
            )
        )

        setupActionBarWithNavController(controller, appBarConfiguration)
        navView.setupWithNavController(controller)

        val intent = Intent(this, WebsocketClient::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        )

        socketClient = WebsocketClient()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Callbacks for service binding, passed to bindService()  */
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // cast the IBinder and get MyService instance
            val binder = service as LocalBinder
            socketClient = binder.getService()
            bound = true
            socketClient!!.setCallbacks(this@MainActivity) // register
            socketClient!!.connect()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun authorizationSuccess() {
        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_dashboard)
        Toast.makeText(this@MainActivity, "Authorization successful", 3).show()
    }

    override fun authorizationFailure() {
        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_dashboard)
        Toast.makeText(this@MainActivity, "Authorization failed", 3).show()
    }

    override fun deviceAuthorizationSuccess() {
        Toast.makeText(this@MainActivity, "Device authorized", 3).show()
    }

    override fun deviceAuthorizationFailure() {
        Toast.makeText(this@MainActivity, "Device authorization failure", 3).show()
    }

    companion object {
        var socketClient: WebsocketClient? = null
        var token: String? = null
        val serverURI = URI("ws://192.168.0.195:5005/")
        var deviceId: String? = null
        val authToken = "808de751-ee7b-4006-bc93-24b45f82ea42"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}