package com.example.yololo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivitySentenceReadingBinding

class Sentence_Reading : AppCompatActivity() {
    private lateinit var binding: ActivitySentenceReadingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySentenceReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}