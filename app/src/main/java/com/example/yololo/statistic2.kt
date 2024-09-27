package com.example.yololo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityStatistic2Binding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class statistic2 : AppCompatActivity() {
    private lateinit var binding: ActivityStatistic2Binding

    companion object {
        private const val TAG = "StatisticActivity2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatistic2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        createLineChart()

        binding.nextBtn5.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun createLineChart() {
        val entries = ArrayList<Entry>()

        // 예시 데이터 - 실제로는 시간에 따른 졸음 정도 데이터가 필요합니다
        entries.add(Entry(1f, 30f))
        entries.add(Entry(2f, 40f))
        entries.add(Entry(3f, 20f))
        entries.add(Entry(4f, 50f))
        entries.add(Entry(5f, 10f))

        val dataSet = LineDataSet(entries, "전체 통계")
        dataSet.apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선으로 연결
            cubicIntensity = 0.2f // 곡선의 강도 조절
        }

        val data = LineData(dataSet)

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
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString() + "시"
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMaximum = 100f
                axisMinimum = 0f
                granularity = 1f
            }

            axisRight.isEnabled = false

            animateX(1500)
        }
    }
}