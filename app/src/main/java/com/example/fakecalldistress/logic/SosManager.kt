package com.example.fakecalldistress.logic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.example.fakecalldistress.data.Contact
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SosManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun triggerSos(contacts: List<Contact>) {
        if (contacts.isEmpty()) return

        getLastLocation { location ->
            val message = if (location != null) {
                "SOS! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "SOS! I need help. GPS unavailable, but I am in danger."
            }
            
            sendSmsToContacts(contacts, message)
        }
    }

    private fun getLastLocation(onLocation: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocation(null)
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocation(location)
        }.addOnFailureListener {
            onLocation(null)
        }
    }

    private fun sendSmsToContacts(contacts: List<Contact>, message: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        
        for (contact in contacts) {
            if (contact.phoneNumber.isNotBlank()) {
                try {
                    smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
