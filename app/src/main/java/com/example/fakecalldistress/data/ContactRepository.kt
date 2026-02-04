package com.example.fakecalldistress.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ContactRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sos_contacts", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val CONTACTS_KEY = "saved_contacts"
    private val CALLER_INFO_KEY = "caller_info"

    // Save contacts locally (Offline first)
    fun saveContacts(contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        prefs.edit().putString(CONTACTS_KEY, json).apply()
        
        // TODO: Sync with Firebase here
        // FirebaseDatabase.getInstance().getReference("contacts").setValue(contacts)
    }

    // Get contacts
    fun getContacts(): List<Contact> {
        val json = prefs.getString(CONTACTS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Contact>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Save Fake Caller Details
    fun saveCallerInfo(info: CallerInfo) {
        val json = gson.toJson(info)
        prefs.edit().putString(CALLER_INFO_KEY, json).apply()
    }

    // Get Fake Caller Details
    fun getCallerInfo(): CallerInfo {
        val json = prefs.getString(CALLER_INFO_KEY, null)
        return if (json != null) {
            gson.fromJson(json, CallerInfo::class.java)
        } else {
            CallerInfo("Dad", "Mobile") // Default
        }
    }
}
