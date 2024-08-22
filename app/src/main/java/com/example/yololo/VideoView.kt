package com.example.yololo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityVideoViewBinding

class VideoView : AppCompatActivity() {
    private lateinit var binding: ActivityVideoViewBinding
    private var isFullScreen = false

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                playVideo(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoView = binding.videoView
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.full.setOnClickListener {
            toggleFullScreen()
        }

        // 갤러리에서 비디오 선택 버튼
        binding.selectVideo.setOnClickListener {
            openGallery()
        }

        videoView.setMediaController(mediaController)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun playVideo(uri: Uri) {
        binding.videoView.setVideoURI(uri)
        binding.videoView.requestFocus()
        binding.videoView.start()
    }

    private fun toggleFullScreen() {
        if (isFullScreen) {
            exitFullScreen()
        } else {
            enterFullScreen()
        }
    }

    private fun enterFullScreen() {
        val params = binding.videoView.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.videoView.layoutParams = params

        binding.back.visibility = View.GONE
        binding.videoTitle.visibility = View.GONE
        binding.selectVideo.visibility = View.GONE

        binding.full.setImageResource(R.drawable.baseline_fullscreen_exit_24)
        isFullScreen = true
    }

    private fun exitFullScreen() {
        val params = binding.videoView.layoutParams
        params.width = resources.getDimensionPixelSize(R.dimen.video_weight)
        params.height = resources.getDimensionPixelSize(R.dimen.video_height)
        binding.videoView.layoutParams = params

        binding.back.visibility = View.VISIBLE
        binding.videoTitle.visibility = View.VISIBLE
        binding.selectVideo.visibility = View.VISIBLE

        binding.full.setImageResource(R.drawable.baseline_fullscreen_24)
        isFullScreen = false
    }
}