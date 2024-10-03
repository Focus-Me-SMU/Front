package com.example.yololo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityStatistic2Binding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import org.json.JSONArray
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class statistic2 : AppCompatActivity() {
    private lateinit var binding: ActivityStatistic2Binding

    companion object {
        private const val TAG = "CameraXApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatistic2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        createLineChart()

        binding.nextBtn5.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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

    @SuppressLint("UseCompatLoadingForDrawables", "DefaultLocale")
    private fun createLineChart() {
        try {
            val sharedPref = getSharedPreferences("StatisticsData", Context.MODE_PRIVATE)
            val finalScoresJson = sharedPref.getString("FINAL_SCORES", "[]")
            val finalScores = JSONArray(finalScoresJson)
            val finalScore = sharedPref.getFloat("FINAL_SCORE", 0f)

            val scoresList = List(finalScores.length()) { i ->
                finalScores.optDouble(i, 0.0).toFloat()
            }

            // 평균과 표준편차 계산
            val mean = scoresList.average().toFloat()
            val stdDev = sqrt(scoresList.map { (it - mean).pow(2) }.average()).toFloat()

            // 표준정규분포 곡선 생성
            val normalDistEntries = List(100) { i ->
                val x = (i.toFloat() / 100 * 6) - 3 // -3에서 3까지
                val y = (1 / sqrt(2 * Math.PI)) * exp(-0.5 * x.pow(2))
                Entry(x, y.toFloat())
            }

            // 점수 데이터로 Entry 생성 (표준화)
            val entries = scoresList.map { score ->
                val standardizedScore = (score - mean) / stdDev
                Entry(standardizedScore, 0f)
            }

            // final_score에 대한 특별 Entry 생성
            val finalScoreStandardized = (finalScore - mean) / stdDev
            val finalScoreEntry = Entry(finalScoreStandardized, 0f).apply {
                icon = getDrawable(R.drawable.star_icon)
            }

            val scoresDataSet = LineDataSet(entries, "표준화된 점수").apply {
                color = ColorTemplate.MATERIAL_COLORS[0]
                setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
                lineWidth = 0f
                circleRadius = 4f
                setDrawCircleHole(false)
                setDrawValues(false)
            }

            val finalScoreDataSet = LineDataSet(listOf(finalScoreEntry), "Your Score").apply {
                color = Color.RED
                setCircleColor(Color.RED)
                lineWidth = 0f
                circleRadius = 8f
                setDrawCircleHole(false)
                setDrawValues(false)
                setDrawIcons(true)
                iconsOffset = MPPointF(0f, -8f)
            }

            val normalDistDataSet = LineDataSet(normalDistEntries, "표준정규분포").apply {
                color = Color.BLUE
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
            }

            // Your Score를 지나는 수직선 생성
            val verticalLineEntries = listOf(
                Entry(finalScoreStandardized, 0f),
                Entry(finalScoreStandardized, 1f)  // 1f는 y축의 최대값
            )
            val verticalLineDataSet = LineDataSet(verticalLineEntries, "").apply {
                color = Color.RED
                lineWidth = 1f
                setDrawCircles(false)
                setDrawValues(false)
            }

            val data =
                LineData(normalDistDataSet, scoresDataSet, finalScoreDataSet, verticalLineDataSet)

            binding.lineChart.apply {
                this.data = data
                description.isEnabled = false
                legend.isEnabled = true
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    axisMinimum = -3f
                    axisMaximum = 3f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.1f", value)
                        }
                    }
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                }

                axisRight.isEnabled = false

                animateX(1500)
            }

            // 등수 및 상위 퍼센트 계산
            val finalScoreRank = scoresList.count { it > finalScore } + 1
            val totalScores = scoresList.size
            val rankPercentage = (finalScoreRank.toFloat() / totalScores.toFloat() * 100)

            val rankMessage = "당신의 점수는 상위 %.1f%%에 해당합니다. (%d등/%d명 중)"
            binding.rankTextView.text =
                String.format(rankMessage, rankPercentage, finalScoreRank, totalScores)

            Log.d(TAG, "Line chart created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating line chart", e)
        }
    }
}