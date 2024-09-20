package com.example.yololo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yololo.databinding.ActivitySentenceReading2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class sentence_reading2 : AppCompatActivity() {
    private lateinit var binding: ActivitySentenceReading2Binding
    private lateinit var cameraExecutor: ExecutorService
    private val client = OkHttpClient()
    private var cameraProvider: ProcessCameraProvider? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySentenceReading2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextBtn2.isEnabled = false
        binding.nextBtn2.alpha = 0.5f

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 프리뷰 크기 조정
        binding.viewFinderSentence.layoutParams.width = 1
        binding.viewFinderSentence.layoutParams.height = 1
        binding.viewFinderSentence.requestLayout()

        // 다른 뷰를 앞으로 가져오기 (카메라 프리뷰 숨기기)
        binding.sentence.visibility = View.VISIBLE
        binding.sentence.bringToFront()

        binding.back2.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.nextBtn2.setOnClickListener {
            sendClickNextToServer("버튼 클릭")
            startActivity(Intent(this, sentence_reading3::class.java))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinderSentence.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BitmapImageAnalyzer { bitmap ->
                        sendBitmapToServer(bitmap)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    private inner class BitmapImageAnalyzer(private val listener: (Bitmap) -> Unit) : ImageAnalysis.Analyzer {
        private var lastProcessedTimestamp = 0L
        private val processingInterval = 500 // 밀리초 단위, VideoView와 동일하게 설정

        override fun analyze(image: ImageProxy) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastProcessedTimestamp < processingInterval) {
                image.close()
                return
            }

            lastProcessedTimestamp = currentTimestamp

            CoroutineScope(Dispatchers.Default).launch {
                val bitmap = image.toBitmap()
                withContext(Dispatchers.Main) {
                    listener(bitmap)
                }
                image.close()
            }
        }

        private fun ImageProxy.toBitmap(): Bitmap? {
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    private fun showCustomToast(message: String, textColor: Int = Color.WHITE, backgroundColor: Int = Color.BLACK) {
        val layout = layoutInflater.inflate(R.layout.toast, null)
        val textView = layout.findViewById<TextView>(R.id.custom_toast_message)
        textView.text = message
        textView.setTextColor(textColor)
        (textView.background as GradientDrawable).setColor(backgroundColor)

        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    private fun sendBitmapToServer(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("frame", "frame.jpg",
                    byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("width", bitmap.width.toString())
                .addFormDataPart("height", bitmap.height.toString())
                .addFormDataPart("format", "jpeg")
                .build()

            val request = Request.Builder()
                .url(getString(R.string.server_url))
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Received response: $responseBody")
                    val jsonObject = JSONObject(responseBody ?: "{}")

                    val showToast = jsonObject.optBoolean("show_toast", false)
                    if (showToast) {
                        val toastMessage = jsonObject.optString("toast_message", "")
                        withContext(Dispatchers.Main) {
                            showCustomToast(toastMessage, Color.BLACK, Color.YELLOW)
                        }
                    }
                    val message = jsonObject.optString("message", "")
                    Log.d(TAG, "Received message: $message")
                    if (message == "next") {
                        Log.d(TAG, "Next message received, enabling button")
                        withContext(Dispatchers.Main) {
                            enableNextButton()
                        }
                    }
                    else {
                        Log.d(TAG, "Next message not received") // 추가된 로그
                    }
                    // sentence_count 업데이트 (UI 업데이트 등)
                    val sentenceCount = jsonObject.optInt("sentence_count", -1)
                    if (sentenceCount != -1) {
                        withContext(Dispatchers.Main) {
                            updateSentenceCount(sentenceCount)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to send image: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending image", e)
            }
        }
    }

    private fun enableNextButton() {
        Log.d(TAG, "Enabling next button")
        binding.nextBtn2.isEnabled = true
        binding.nextBtn2.alpha = 1.0f
        binding.nextBtn2.setBackgroundColor(Color.parseColor("#FF636261"))

        Log.d(TAG, "Next button enabled and color changed")
    }

    private fun updateSentenceCount(count: Int) {
        // TODO: UI에 sentence_count 반영하는 로직 구현
        // 예: binding.sentenceCountTextView.text = "Sentence Count: $count"
        Log.d(TAG, "Updated sentence count: $count")
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun sendClickNextToServer(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val requestBody = message.toRequestBody("text/plain".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("${getString(R.string.server_url)}/click_next")  // 서버의 클릭 이벤트 처리 엔드포인트
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@sentence_reading2, "클릭 정보가 서버로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@sentence_reading2, "서버 전송 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@sentence_reading2, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp2"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}