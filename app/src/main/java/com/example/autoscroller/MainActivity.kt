package com.example.autoscroller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.autoscroller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("scroller_prefs", Context.MODE_PRIVATE)

        // Load saved values
        binding.switchEnable.isChecked = prefs.getBoolean("enabled", false)
        binding.switchAutoLike.isChecked = prefs.getBoolean("auto_like", false)
        binding.timeout1.setText(prefs.getInt("t1", 3).toString())
        binding.timeout2.setText(prefs.getInt("t2", 7).toString())
        binding.timeout3.setText(prefs.getInt("t3", 15).toString())
        binding.timeout4.setText(prefs.getInt("t4", 25).toString())
        binding.timeout5.setText(prefs.getInt("t5", 40).toString())

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            savePresets()
            prefs.edit().putBoolean("enabled", isChecked).apply()
        }

        binding.switchAutoLike.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_like", isChecked).apply()
        }

        binding.btnSettings.setOnClickListener {
            savePresets()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun savePresets() {
        val prefs = getSharedPreferences("scroller_prefs", Context.MODE_PRIVATE).edit()
        prefs.putInt("t1", binding.timeout1.text.toString().toIntOrNull() ?: 3)
        prefs.putInt("t2", binding.timeout2.text.toString().toIntOrNull() ?: 7)
        prefs.putInt("t3", binding.timeout3.text.toString().toIntOrNull() ?: 15)
        prefs.putInt("t4", binding.timeout4.text.toString().toIntOrNull() ?: 25)
        prefs.putInt("t5", binding.timeout5.text.toString().toIntOrNull() ?: 40)
        prefs.apply()
    }
}
