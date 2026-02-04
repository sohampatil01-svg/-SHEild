package com.example.fakecalldistress.ui

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fakecalldistress.data.ContactRepository
import com.example.fakecalldistress.logic.SosManager
import com.example.fakecalldistress.databinding.ActivityFakeCallBinding
import java.util.Locale

class FakeCallActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityFakeCallBinding
    private var ringtone: Ringtone? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var sosManager: SosManager
    private lateinit var contactRepository: ContactRepository
    
    // Timer to track call duration visually
    private var callSeconds = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            callSeconds++
            val minutes = callSeconds / 60
            val seconds = callSeconds % 60
            binding.tvCallStatus.text = String.format("%02d:%02d", minutes, seconds)
            timerHandler.postDelayed(this, 1000)
        }
    }

    // Dead Man's Switch (Triggers SOS if not answered/declined in 30s)
    private var deadManTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactRepository = ContactRepository(this)
        sosManager = SosManager(this)
        tts = TextToSpeech(this, this)

        // Load Custom Caller ID
        val callerInfo = contactRepository.getCallerInfo()
        binding.tvCallerName.text = callerInfo.name

        // Play default ringtone
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone?.play()

        // Start Dead Man's Switch (30 seconds)
        startDeadManTimer()

        // Button Actions
        binding.fabAnswer.setOnClickListener {
            cancelDeadManTimer()
            answerCall()
        }

        binding.fabDecline.setOnClickListener {
            cancelDeadManTimer()
            finishCall()
        }

        binding.btnEndCall.setOnClickListener {
            finishCall()
        }
    }

    private fun startDeadManTimer() {
        deadManTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() {
                // If call wasn't answered/cancelled, ASSUME DANGER
                triggerEmergency()
            }
        }.start()
    }

    private fun cancelDeadManTimer() {
        deadManTimer?.cancel()
    }

    private fun triggerEmergency() {
        ringtone?.stop()
        val contacts = contactRepository.getContacts()
        if (contacts.isNotEmpty()) {
            sosManager.triggerSos(contacts)
            Toast.makeText(this@FakeCallActivity, "No response - SOS SENT!", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun answerCall() {
        ringtone?.stop()
        
        // SWITCH UI TO "IN CALL" MODE
        binding.groupIncoming.visibility = View.GONE
        binding.groupInCall.visibility = View.VISIBLE
        binding.tvCallStatus.text = "00:00"
        
        // Start Timer
        timerHandler.postDelayed(timerRunnable, 1000)

        // Play Realistic Audio Script
        if (isTtsReady) {
            // Using a script with pauses to simulate a real human checking in
            val script = "Hey. ... I'm just checking in on you. ... You don't have to say anything. ... Just listen. ... If you are safe, just say yes. ... If you are not safe, tell me where you are right now. ... I am listening."
            
            // Set pitch lower to sound a bit more serious/natural (optional)
            tts?.setPitch(0.9f)
            tts?.setSpeechRate(0.85f)
            tts?.speak(script, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun finishCall() {
        ringtone?.stop()
        tts?.stop()
        tts?.shutdown()
        timerHandler.removeCallbacks(timerRunnable)
        finish()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onDestroy() {
        ringtone?.stop()
        timerHandler.removeCallbacks(timerRunnable)
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}