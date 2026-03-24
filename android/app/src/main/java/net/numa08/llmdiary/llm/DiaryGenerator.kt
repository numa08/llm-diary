package net.numa08.llmdiary.llm

import com.google.android.gms.location.DetectedActivity
import net.numa08.llmdiary.data.local.entity.ActivityEvent
import net.numa08.llmdiary.data.local.entity.HealthData
import net.numa08.llmdiary.data.local.entity.LocationEvent
import net.numa08.llmdiary.data.local.entity.PhotoEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryGenerator @Inject constructor(
    private val llmEngine: LlmEngine,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())

    suspend fun generate(
        activities: List<ActivityEvent>,
        locations: List<LocationEvent>,
        photos: List<PhotoEvent>,
        healthData: HealthData?,
        date: String,
    ): String {
        val prompt = buildPrompt(activities, locations, photos, healthData, date)
        return llmEngine.generateText(prompt)
    }

    private fun buildPrompt(
        activities: List<ActivityEvent>,
        locations: List<LocationEvent>,
        photos: List<PhotoEvent>,
        healthData: HealthData?,
        date: String,
    ): String = buildString {
        appendLine("あなたはスマートフォンの持ち主の1日を日記として記録するアシスタントです。")
        appendLine("以下のセンサーデータを元に、${date}の日記をMarkdown形式で書いてください。")
        appendLine("持ち主の視点で、自然な日本語で書いてください。")
        appendLine()

        if (activities.isNotEmpty()) {
            appendLine("## 行動記録")
            activities.forEach { event ->
                val time = timeFormatter.format(Instant.ofEpochMilli(event.timestamp))
                val activityName = activityTypeToString(event.activityType)
                val location = event.address ?: event.latitude?.let { "($it, ${event.longitude})" } ?: ""
                appendLine("- $time: $activityName (確信度: ${event.confidence}%) $location")
            }
            appendLine()
        }

        if (locations.isNotEmpty()) {
            appendLine("## 訪問した場所")
            locations.forEach { event ->
                val time = timeFormatter.format(Instant.ofEpochMilli(event.timestamp))
                val place = event.address ?: "(${event.latitude}, ${event.longitude})"
                appendLine("- $time: $place")
            }
            appendLine()
        }

        if (photos.isNotEmpty()) {
            appendLine("## 撮影した写真 (${photos.size}枚)")
            photos.forEach { photo ->
                val time = timeFormatter.format(Instant.ofEpochMilli(photo.timestamp))
                val location = photo.address ?: photo.latitude?.let { "($it, ${photo.longitude})" } ?: ""
                val desc = photo.description
                if (desc != null) {
                    appendLine("- $time: $desc $location")
                } else {
                    appendLine("- $time: 写真撮影 $location")
                }
            }
            appendLine()
        }

        if (healthData != null) {
            appendLine("## 健康データ")
            healthData.steps?.let { appendLine("- 歩数: $it 歩") }
            healthData.heartRateAvg?.let {
                appendLine("- 心拍数: 平均 ${it.toInt()} bpm (最小 ${healthData.heartRateMin?.toInt()}, 最大 ${healthData.heartRateMax?.toInt()})")
            }
            healthData.sleepDurationMinutes?.let {
                val hours = it / 60
                val minutes = it % 60
                appendLine("- 睡眠: ${hours}時間${minutes}分")
            }
            healthData.exerciseDurationMinutes?.let {
                appendLine("- 運動: ${it}分 (${healthData.exerciseType})")
            }
            healthData.weightKg?.let { appendLine("- 体重: ${"%.1f".format(it)} kg") }
            appendLine()
        }

        appendLine("---")
        appendLine("上記のデータを元に、この日の日記をMarkdownで出力してください。")
    }

    private fun activityTypeToString(type: Int): String = when (type) {
        DetectedActivity.IN_VEHICLE -> "車移動"
        DetectedActivity.ON_BICYCLE -> "自転車"
        DetectedActivity.ON_FOOT -> "徒歩"
        DetectedActivity.RUNNING -> "ランニング"
        DetectedActivity.STILL -> "静止"
        DetectedActivity.TILTING -> "傾き"
        DetectedActivity.WALKING -> "歩行"
        else -> "不明($type)"
    }
}
