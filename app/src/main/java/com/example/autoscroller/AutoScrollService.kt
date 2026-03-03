package com.example.autoscroller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.random.Random

class AutoScrollService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val prefs = getSharedPreferences("scroller_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", false)
            
            if (enabled) {
                // 1. Human-like: Sometimes "Watch" longer, sometimes "Skip" fast
                val isWatchLong = Random.nextInt(100) < 30 // 30% chance to watch long
                
                val timeouts = listOf(
                    prefs.getInt("t1", 3),
                    prefs.getInt("t2", 7),
                    prefs.getInt("t3", 15),
                    prefs.getInt("t4", 25),
                    prefs.getInt("t5", 40)
                )

                val baseDelay = if (isWatchLong) {
                    Random.nextInt(15, 45) // 15-45 seconds
                } else {
                    timeouts.random()
                }

                // 2. Micro-pause before action (1-2s)
                val thinkTime = Random.nextLong(1000, 2500)
                
                handler.postDelayed({
                    performActionFlow(prefs.getBoolean("auto_like", false))
                }, (baseDelay * 1000).toLong() + thinkTime)
                
                // Reschedule next check
                handler.postDelayed(this, (baseDelay * 1000).toLong() + thinkTime + 1000)
            } else {
                handler.postDelayed(this, 3000) // Check again in 3s if disabled
            }
        }
    }

    private fun performActionFlow(shouldAutoLike: Boolean) {
        if (shouldAutoLike && Random.nextInt(100) < 20) { // 20% chance to like
            // Simulate human looking then liking
            handler.postDelayed({
                tryDoubleTapToLike()
            }, Random.nextLong(2000, 5000))
        }

        // Delay after potential like before scrolling
        handler.postDelayed({
            performSwipeUp()
        }, Random.nextLong(1000, 3000))
    }

    private fun performSwipeUp() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Randomized gesture coordinates (±10% variance)
        val startX = (width / 2) + Random.nextInt(-50, 50)
        val startY = (height * 0.8).toInt() + Random.nextInt(-50, 50)
        val endY = (height * 0.2).toInt() + Random.nextInt(-50, 50)

        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(startX.toFloat(), endY.toFloat())

        val gestureDuration = Random.nextLong(250, 500) // Randomized speed

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, gestureDuration))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun tryDoubleTapToLike() {
        // Accessibility-friendly double tap in middle of screen for Insta Reel Like
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2f
        val centerY = displayMetrics.heightPixels / 2f

        val path = Path()
        path.moveTo(centerX, centerY)
        
        val gestureBuilder = GestureDescription.Builder()
        // Double tap is two strokes close in time
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 100, 50))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onServiceConnected() {
        isRunning = true
        handler.post(scrollRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional: Detect if user manually scrolled to reset timer
    }

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(scrollRunnable)
    }
}
