package com.example.yololo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.yololo.R.id.camerabtn

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val SentenceCard = findViewById<CardView>(R.id.sentence_cardview)
        val VideoCard = findViewById<CardView>(R.id.video_cardview)
        val camera = findViewById<Button>(camerabtn)

        SentenceCard.setOnClickListener {
            startActivity(Intent(this, Sentence_Reading::class.java))
        }

        VideoCard.setOnClickListener {
            startActivity(Intent(this, VideoView::class.java))
        }
        camera.setOnClickListener {
            startActivity(Intent(this, Camera::class.java))
        }
    }
}