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
import com.example.yololo.databinding.ActivityStatisticBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class statistic : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticBinding

    companion object {
        private const val TAG = "CameraXApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Statistic activity onCreate started")

        try {
            binding = ActivityStatisticBinding.inflate(layoutInflater)
            setContentView(binding.root)

            CoroutineScope(Dispatchers.Default).launch {
                val sharedPref = getSharedPreferences("StatisticsData", Context.MODE_PRIVATE)
                val allFrameCount = sharedPref.getInt("ALL_FRAME_COUNT", 0)
                val lookForwardCount = sharedPref.getInt("LOOK_FORWARD_COUNT", 0)
                val awakeCount = sharedPref.getInt("AWAKE_COUNT", 0)
                val drowsyCount = sharedPref.getInt("DROWSY_COUNT", 0)
                val yellingCount = sharedPref.getInt("YELLING_COUNT", 0)
                val finalScore = sharedPref.getFloat("FINAL_SCORE", 0f)
                val medScore = sharedPref.getFloat("MED_SCORE", 0f)
                val topScore = sharedPref.getFloat("TOP_SCORE", 0f)

                Log.d(TAG, "Loaded data: allFrameCount=$allFrameCount, finalScore=$finalScore, medScore=$medScore, topScore=$topScore")

                withContext(Dispatchers.Main) {
                    createPieChart(awakeCount, drowsyCount, yellingCount, lookForwardCount)
                    createBarChart(finalScore, topScore, medScore)
                }
            }

            binding.nextBtn4.setOnClickListener {
                startActivity(Intent(this, statistic2::class.java))
            }

            hideSystemBars()
            Log.d(TAG, "Statistic activity onCreate finished successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
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

    private fun createPieChart(awakeCount: Int, drowsyCount: Int, yellingCount: Int, lookForwardCount: Int) {
        try {
            val entries = ArrayList<PieEntry>()
            if (awakeCount > 0) entries.add(PieEntry(awakeCount.toFloat(), "집중 중 ($awakeCount)"))
            if (drowsyCount > 0) entries.add(PieEntry(drowsyCount.toFloat(), "졸음 ($drowsyCount)"))
            if (yellingCount > 0) entries.add(PieEntry(yellingCount.toFloat(), "하품 ($yellingCount)"))
            if (lookForwardCount > 0) entries.add(PieEntry(lookForwardCount.toFloat(), "다른곳 봄 ($lookForwardCount)"))

            if (entries.isEmpty()) {
                Log.w(TAG, "No data to display in pie chart")
                return
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = Color.BLACK
                valueTextSize = 12f
                yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
                valueLinePart1Length = 0.3f
                valueLinePart2Length = 0.7f
                valueLineColor = Color.BLACK
                valueLineWidth = 1f
                sliceSpace = 3f
            }

            val data = PieData(dataSet).apply {
                setValueFormatter(object : PercentFormatter() {
                    @SuppressLint("DefaultLocale")
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f%%", value)
                    }
                })
            }

            binding.pieChart.apply {
                this.data = data
                description.isEnabled = false
                isDrawHoleEnabled = true
                setUsePercentValues(true)
                setEntryLabelColor(Color.BLACK)
                setEntryLabelTextSize(11f)
                centerText = "당신의 집중도"
                setCenterTextSize(16f)
                legend.isEnabled = false  // 범례 비활성화
                setDrawEntryLabels(true)  // 엔트리 레이블 활성화
                setExtraOffsets(20f, 20f, 20f, 20f)
                invalidate()
                animateY(1000)
            }

            Log.d(TAG, "Pie chart created with ${entries.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pie chart", e)
        }
    }

    private fun createBarChart(finalScore: Float, topScore: Float, medScore: Float) {
        try {
            val entries = listOf(
                BarEntry(0f, finalScore),
                BarEntry(1f, medScore),
                BarEntry(2f, topScore)
            )

            val dataSet = BarDataSet(entries, "").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = Color.BLACK
                valueTextSize = 20f
            }

            val data = BarData(dataSet)

            binding.barChart.apply {
                this.data = data
                description.isEnabled = false
                setFitBars(true)

                xAxis.apply {
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        private val labels = arrayOf("내 점수", "상위 50%", "상위 10%")
                        override fun getFormattedValue(value: Float): String = labels[value.toInt()]
                    }
                }

                axisLeft.apply {
                    setDrawGridLines(false)
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false

                legend.apply {
                    isEnabled = true
                    textSize = 30f
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }

                invalidate()
                animateY(1000)
            }

            Log.d(TAG, "Bar chart created with ${entries.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating bar chart", e)
        }
    }
}