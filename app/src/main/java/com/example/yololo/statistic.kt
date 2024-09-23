package com.example.yololo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yololo.databinding.ActivityStatisticBinding

class statistic : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 통계 데이터 추출
        val allFrameCount = intent.getIntExtra("ALL_FRAME_COUNT", 0)
        val lookForwardCount = intent.getIntExtra("LOOK_FORWARD_COUNT", 0)
        val awakeCount = intent.getIntExtra("AWAKE_COUNT", 0)
        val drowsyCount = intent.getIntExtra("DROWSY_COUNT", 0)
        val yellingCount = intent.getIntExtra("YELLING_COUNT", 0)

        // UI에 통계 데이터 표시
        updateUIWithStatistics(allFrameCount, lookForwardCount, awakeCount, drowsyCount, yellingCount)

        // ... (기타 필요한 UI 초기화 및 이벤트 처리)
    }

    private fun updateUIWithStatistics(allFrameCount: Int, lookForwardCount: Int, awakeCount: Int, drowsyCount: Int, yellingCount: Int) {
        // 예시: TextView에 통계 데이터 표시
        binding.allFrameCountTextView.text = "총 프레임 수: $allFrameCount"
        binding.lookForwardCountTextView.text = "전방 주시 횟수: $lookForwardCount"
        binding.awakeCountTextView.text = "깨어있는 상태 횟수: $awakeCount"
        binding.drowsyCountTextView.text = "졸린 상태 횟수: $drowsyCount"
        binding.yellingCountTextView.text = "소리 지르는 횟수: $yellingCount"

        // 필요에 따라 추가적인 UI 업데이트 수행
        // 예: 차트 그리기, 백분율 계산 등
    }

    // ... (기타 필요한 메서드들)
}