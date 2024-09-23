package com.example.yololo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityStatisticBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class statistic : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticBinding

    companion object {
        private const val TAG = "StatisticActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 통계 데이터 추출 및 로깅
        val allFrameCount = intent.getIntExtra("ALL_FRAME_COUNT", 0)
        val lookForwardCount = intent.getIntExtra("LOOK_FORWARD_COUNT", 0)
        val awakeCount = intent.getIntExtra("AWAKE_COUNT", 0)
        val drowsyCount = intent.getIntExtra("DROWSY_COUNT", 0)
        val yellingCount = intent.getIntExtra("YELLING_COUNT", 0)

        Log.d(TAG, "Received data: ALL_FRAME_COUNT=$allFrameCount, LOOK_FORWARD_COUNT=$lookForwardCount, AWAKE_COUNT=$awakeCount, DROWSY_COUNT=$drowsyCount, YELLING_COUNT=$yellingCount")

        // 파이 차트 생성
        createPieChart(allFrameCount, lookForwardCount, awakeCount, drowsyCount, yellingCount)
    }

    private fun createPieChart(allFrameCount: Int, lookForwardCount: Int, awakeCount: Int, drowsyCount: Int, yellingCount: Int) {
        val entries = ArrayList<PieEntry>()
        if (lookForwardCount >= 0) entries.add(PieEntry(lookForwardCount.toFloat(), "시선 불일치 (${lookForwardCount})"))
        if (awakeCount >= 0) entries.add(PieEntry(awakeCount.toFloat(), "깨어 있음 (${awakeCount})"))
        if (drowsyCount >= 0) entries.add(PieEntry(drowsyCount.toFloat(), "졸림 (${drowsyCount})"))
        if (yellingCount >= 0) entries.add(PieEntry(yellingCount.toFloat(), "하품 (${yellingCount})"))

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
            centerText = "운전 상태\n(총 ${allFrameCount}프레임)"
            setCenterTextSize(16f)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            invalidate() // 차트 갱신
            animateY(1000) // 1초 동안 애니메이션 적용
        }

        Log.d(TAG, "Chart created with ${entries.size} entries")
    }
}