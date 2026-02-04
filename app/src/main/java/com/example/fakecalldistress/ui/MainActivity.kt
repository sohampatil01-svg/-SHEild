package com.example.fakecalldistress.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fakecalldistress.R
import com.example.fakecalldistress.data.Contact
import com.example.fakecalldistress.data.ContactRepository
import com.example.fakecalldistress.databinding.ActivityMainBinding
import com.example.fakecalldistress.logic.ShakeDetector
import com.example.fakecalldistress.logic.SosManager
import com.example.fakecalldistress.service.JourneyService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactRepository: ContactRepository
    private lateinit var sosManager: SosManager
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private var isShakeEnabled = false

    // Audio Recording
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    // Tracking UI Update Loop
    private val handler = Handler(Looper.getMainLooper())
    private val trackingRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateTrackingLog()
                handler.postDelayed(this, 5000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactRepository = ContactRepository(this)
        sosManager = SosManager(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setupShakeDetector()
        setupUI()
        setupBottomNav()
        
        checkPermissions()
    }

    private fun setupShakeDetector() {
        shakeDetector = ShakeDetector {
            if (isShakeEnabled) {
                runOnUiThread {
                    startFakeCall()
                }
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showView(binding.viewHome)
                    true
                }
                R.id.nav_history -> {
                    showView(binding.viewCommunity)
                    loadIncidents()
                    true
                }
                R.id.nav_settings -> {
                    showView(binding.viewSettings)
                    true
                }
                R.id.nav_help -> {
                    showView(binding.viewInfo)
                    true
                }
                else -> false
            }
        }
    }

    private fun showView(view: View) {
        binding.viewHome.visibility = View.GONE
        binding.viewCommunity.visibility = View.GONE
        binding.viewSettings.visibility = View.GONE
        binding.viewInfo.visibility = View.GONE
        view.visibility = View.VISIBLE
    }

    private fun setupUI() {
        // --- Dashboard Logic ---
        
        // Load Settings
        val contacts = contactRepository.getContacts()
        if (contacts.isNotEmpty()) {
            val contact = contacts[0]
            binding.etContactName.setText(contact.name)
            binding.etContactNumber.setText(contact.phoneNumber)
            binding.tvSavedContact.text = "Saved: ${contact.name}"
        }

        val callerInfo = contactRepository.getCallerInfo()
        binding.etFakeName.setText(callerInfo.name)

        // Save Settings
        binding.btnSaveContact.setOnClickListener {
            val name = binding.etContactName.text.toString()
            val number = binding.etContactNumber.text.toString()
            if (name.isNotEmpty() && number.isNotEmpty()) {
                contactRepository.saveContacts(listOf(Contact(name, number)))
                binding.tvSavedContact.text = "Saved: $name"
                Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveCaller.setOnClickListener {
            val name = binding.etFakeName.text.toString()
            if (name.isNotEmpty()) {
                contactRepository.saveCallerInfo(com.example.fakecalldistress.data.CallerInfo(name, "Mobile"))
                Toast.makeText(this, "Caller ID Updated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchShake.setOnCheckedChangeListener { _, isChecked ->
            isShakeEnabled = isChecked
            if (isChecked) {
                startShakeDetection()
                binding.tvShakeStatus.text = "Shake: ON"
            } else {
                stopShakeDetection()
                binding.tvShakeStatus.text = "Shake: OFF"
            }
        }

        // Actions
        binding.cardFakeCall.setOnClickListener { startFakeCall() }

        // SOS Button
        binding.cardSos.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Prevent ScrollView from stealing the touch event
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    
                    if (checkPermissions()) {
                        startAudioRecording()
                    } else {
                        Toast.makeText(this, "Permissions required!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Allow scrolling again
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    stopAudioRecording()
                    triggerSos()
                    true
                }
                else -> true // Consume other events (like MOVE) so we keep control
            }
        }

        // Journey Tracking
        binding.cardJourney.setOnClickListener {
            toggleJourneyTracking()
        }

        // --- Community Logic ---
        binding.btnPostIncident.setOnClickListener {
            val text = binding.etIncident.text.toString()
            if (text.isNotEmpty()) {
                saveIncident(text)
                binding.etIncident.text?.clear()
                loadIncidents()
                Toast.makeText(this, "Log Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Journey Logic ---
    private var isTracking = false
    private fun toggleJourneyTracking() {
        if (!isTracking) {
            val intent = Intent(this, JourneyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isTracking = true
            binding.tvJourneyStatus.text = "Stop"
            binding.ivJourney.setColorFilter(getColor(R.color.sheild_blue_primary))
            handler.post(trackingRunnable)
            Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show()
        } else {
            stopService(Intent(this, JourneyService::class.java))
            isTracking = false
            binding.tvJourneyStatus.text = "Start"
            binding.ivJourney.setColorFilter(getColor(R.color.text_grey))
            binding.tvTrackingLog.text = "Tracking Stopped."
            handler.removeCallbacks(trackingRunnable)
            Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTrackingLog() {
        if (JourneyService.lastLocations.isEmpty()) {
            binding.tvTrackingLog.text = "Waiting for GPS..."
        } else {
            val recent = JourneyService.lastLocations.takeLast(3).joinToString("\n")
            binding.tvTrackingLog.text = "Live Tracking:\n$recent"
        }
    }

    // --- Audio Logic ---
    private fun startAudioRecording() {
        try {
            audioFile = File(externalCacheDir, "sos_audio_${System.currentTimeMillis()}.3gp")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudioRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
    }

    // --- SOS Logic ---
    private fun triggerSos() {
        val savedContacts = contactRepository.getContacts()
        if (savedContacts.isEmpty()) {
            Toast.makeText(this, "Please SAVE a contact in Settings!", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val history = JourneyService.lastLocations.takeLast(10).joinToString("\n")
            val historyText = if (history.isNotEmpty()) "Recent Locations:\n$history" else "Location tracking was off."

            sosManager.triggerSos(savedContacts)

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("police@example.com")) 
                putExtra(Intent.EXTRA_SUBJECT, "SOS ALERT! I NEED HELP")
                putExtra(Intent.EXTRA_TEXT, "I have triggered an SOS.\n\n$historyText\n\nAudio evidence is recorded on device.")
            }
            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
                // No email app
            }

            Toast.makeText(this, "SOS SENT!", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error sending SOS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- Community Logic ---
    private fun saveIncident(text: String) {
        val prefs = getSharedPreferences("incidents", MODE_PRIVATE)
        val current = prefs.getStringSet("logs", mutableSetOf()) ?: mutableSetOf()
        val timestamp = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
        current.add("$timestamp\n$text")
        prefs.edit().putStringSet("logs", current).apply()
    }

    private fun loadIncidents() {
        val prefs = getSharedPreferences("incidents", MODE_PRIVATE)
        val logs = prefs.getStringSet("logs", emptySet())
        if (logs.isNullOrEmpty()) {
            binding.tvIncidentLog.text = "No recent logs."
        } else {
            val sortedLogs = logs.sortedDescending().joinToString("\n\n-----------------\n\n")
            binding.tvIncidentLog.text = sortedLogs
        }
    }

    // --- Helpers ---
    private fun startFakeCall() {
        startActivity(Intent(this, FakeCallActivity::class.java))
    }

    private fun startShakeDetection() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopShakeDetection() {
        sensorManager.unregisterListener(shakeDetector)
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE
        )
        val missing = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
            return false
        }
        return true
    }
}
