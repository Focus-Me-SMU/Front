package com.example.yololo

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityQuestionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class question : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val client = OkHttpClient()

    companion object {
        private const val TAG = "CameraXApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitButton.setOnClickListener {
            Log.d(TAG, "Submit button clicked")
            val correctCount = checkAnswers()
            Log.d(TAG, "Correct answers: $correctCount")
            sendAnswersAndGetStatistics(correctCount)
        }
        hideSystemBars()
    }

    private fun hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun checkAnswers(): Int {
        var correctCount = 0
        if (binding.answerD.isChecked) correctCount++
        if (binding.answer1B.isChecked) correctCount++
        if (binding.answer2A.isChecked) correctCount++
        if (binding.answer3A.isChecked) correctCount++
        return correctCount
    }

    private fun sendAnswersAndGetStatistics(correctCount: Int) {
        val loadingDialog = ProgressDialog(this).apply {
            setMessage("처리 중...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonObject = JSONObject().apply {
                    put("score", correctCount)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("${getString(R.string.server_url)}/click_end")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Received response: $responseBody")
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    // 서버로부터 받은 통계 데이터 처리
                    val allFrameCount = jsonResponse.optInt("all_frame_count", 0)
                    val lookForwardCount = jsonResponse.optInt("Look_Forward_count", 0)
                    val awakeCount = jsonResponse.optInt("awake_count", 0)
                    val drowsyCount = jsonResponse.optInt("drowsy_count", 0)
                    val yellingCount = jsonResponse.optInt("yelling_count", 0)
                    val finalScore = jsonResponse.optDouble("final_score", 0.0)
                    val topScore = jsonResponse.optDouble("top_score", 0.0)
                    val medScore = jsonResponse.optDouble("med_score", 0.0)
                    val finalScoreAll = jsonResponse.optJSONArray("final_score_all") ?: JSONArray()

                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()

                        // 통계 데이터를 SharedPreferences에 저장
                        val sharedPref = getSharedPreferences("StatisticsData", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("ALL_FRAME_COUNT", allFrameCount)
                            putInt("LOOK_FORWARD_COUNT", lookForwardCount)
                            putInt("AWAKE_COUNT", awakeCount)
                            putInt("DROWSY_COUNT", drowsyCount)
                            putInt("YELLING_COUNT", yellingCount)
                            putFloat("FINAL_SCORE", finalScore.toFloat())
                            putFloat("MED_SCORE", medScore.toFloat())
                            putFloat("TOP_SCORE", topScore.toFloat())
                            putString("FINAL_SCORES", finalScoreAll.toString())
                            apply()
                        }

                        // 통계 페이지로 이동
                        Log.d(TAG, "Moving to statistics page")
                        val intent = Intent(this@question, statistic::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티를 종료
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        Toast.makeText(this@question, "서버 응답 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@question, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}