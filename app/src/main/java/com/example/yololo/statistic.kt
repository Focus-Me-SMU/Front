package com.example.yololo

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

class statistic : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticBinding

    companion object {
        private const val TAG = "StatisticActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Statistic activity onCreate started")

        try {
            binding = ActivityStatisticBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val sharedPref = getSharedPreferences("StatisticsData", Context.MODE_PRIVATE)
            val allFrameCount = sharedPref.getInt("ALL_FRAME_COUNT", 0)
            val lookForwardCount = sharedPref.getInt("LOOK_FORWARD_COUNT", 0)
            val awakeCount = sharedPref.getInt("AWAKE_COUNT", 0)
            val drowsyCount = sharedPref.getInt("DROWSY_COUNT", 0)
            val yellingCount = sharedPref.getInt("YELLING_COUNT", 0)
            val finalScore = sharedPref.getFloat("FINAL_SCORE", 0f)
            val avgScore = sharedPref.getFloat("AVG_SCORE", 0f)
            val topScore = sharedPref.getFloat("TOP_SCORE", 0f)

            Log.d(TAG, "Loaded data: allFrameCount=$allFrameCount, finalScore=$finalScore, avgScore=$avgScore, topScore=$topScore")

            // 파이 차트 생성
            createPieChart(finalScore.toInt(), avgScore.toInt(), topScore.toInt())
            createBarChart(allFrameCount, lookForwardCount, awakeCount, drowsyCount, yellingCount)

            binding.nextBtn4.setOnClickListener {
                startActivity(Intent(this, statistic2::class.java))
            }

            Log.d(TAG, "Statistic activity onCreate finished successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    private fun createPieChart(final_score: Int, avg_score: Int, top_10_percent_score: Int) {
        val entries = ArrayList<PieEntry>()
        if (final_score >= 0) entries.add(PieEntry(final_score.toFloat(), "최종 점수 (${final_score})"))
        if (avg_score >= 0) entries.add(PieEntry(avg_score.toFloat(), "평균 점수 (${avg_score})"))
        if (top_10_percent_score >= 0) entries.add(PieEntry(top_10_percent_score.toFloat(), "상위 10% (${top_10_percent_score})"))
        if (entries.isEmpty()) {
            Log.e(TAG, "No data to display in chart")
            return
        }

        val dataSet = PieDataSet(entries, "통계")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChart))

        with(binding.pieChart) {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            centerText = "전체 통계"
            setCenterTextSize(16f)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            invalidate() // 차트 갱신
            animateY(1000) // 1초 동안 애니메이션 적용
        }

        Log.d(TAG, "Chart created with ${entries.size} entries")
    }
    private fun createBarChart(allFrameCount: Int, lookForwardCount: Int, awakeCount: Int, drowsyCount: Int, yellingCount: Int) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, lookForwardCount.toFloat()))
        entries.add(BarEntry(1f, awakeCount.toFloat()))
        entries.add(BarEntry(2f, drowsyCount.toFloat()))
        entries.add(BarEntry(3f, yellingCount.toFloat()))

        val dataSet = BarDataSet(entries, "통계")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 20f

        val data = BarData(dataSet)

        with(binding.barChart) {
            this.data = data
            description.isEnabled = false
            setFitBars(true)

            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)

            xAxis.valueFormatter = object : ValueFormatter() {
                private val labels = arrayOf("시선 불일치", "깨어 있음", "졸림", "하품")
                override fun getFormattedValue(value: Float): String {
                    return labels[value.toInt()]
                }
            }

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 16f
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            invalidate() // 차트 갱신
            animateY(1000) // 1초 동안 애니메이션 적용
        }

        Log.d(TAG, "Bar chart created with ${entries.size} entries")
    }
}