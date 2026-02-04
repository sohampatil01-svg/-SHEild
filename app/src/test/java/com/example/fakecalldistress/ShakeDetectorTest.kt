package com.example.fakecalldistress

import com.example.fakecalldistress.logic.ShakeDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun processSensorData_detectsStrongShake() {
        val detector = ShakeDetector {}
        
        // Use standard gravity for test
        val gravity = 9.81f
        
        // Force = 3g on X axis (approx 29.43)
        val x = 30f 
        val y = 0f
        val z = 0f
        
        val isShake = detector.processSensorData(x, y, z, gravity)
        
        assertTrue("Should detect shake > 2.7g", isShake)
    }

    @Test
    fun processSensorData_ignoresWeakShake() {
        val detector = ShakeDetector {}
        val gravity = 9.81f
        
        // Force = 1g (Just standing still)
        val x = 0f
        val y = 0f
        val z = 9.81f
        
        val isShake = detector.processSensorData(x, y, z, gravity)
        
        assertFalse("Should not detect normal gravity as shake", isShake)
    }
}