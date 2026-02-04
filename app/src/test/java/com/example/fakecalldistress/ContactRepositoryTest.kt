package com.example.fakecalldistress

import android.content.Context
import android.content.SharedPreferences
import com.example.fakecalldistress.data.Contact
import com.example.fakecalldistress.data.ContactRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class ContactRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: ContactRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE))).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        
        repository = ContactRepository(mockContext)
    }

    @Test
    fun saveContacts_savesJsonToPrefs() {
        val contacts = listOf(Contact("Test", "123"))
        repository.saveContacts(contacts)

        verify(mockEditor).putString(eq("saved_contacts"), anyString())
        verify(mockEditor).apply()
    }
}
