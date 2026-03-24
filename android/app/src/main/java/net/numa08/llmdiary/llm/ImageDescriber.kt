package net.numa08.llmdiary.llm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.edge.aicore.Content
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Nano (on-device) を使って画像をテキストに変換する。
 */
@Singleton
class ImageDescriber @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "ImageDescriber"
        private const val MAX_IMAGE_DIMENSION = 1024
    }

    private val generativeModel by lazy {
        GenerativeModel(
            generationConfig = generationConfig {
                temperature = 0.2f
                topK = 16
                maxOutputTokens = 256
            },
        )
    }

    /**
     * 画像URIからテキスト説明を生成する。
     * @return 画像の説明文。失敗した場合は null。
     */
    suspend fun describe(imageUri: String): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadAndResizeBitmap(imageUri) ?: return@withContext null

            val content = Content.Builder()
                .addImage(bitmap)
                .addText("この写真に写っているものを日本語で簡潔に説明してください。場所、人、物、活動など、日記に役立つ情報を含めてください。")
                .build()

            val response = generativeModel.generateContent(content)
            val text = response.text?.trim()

            if (text.isNullOrBlank()) {
                Log.w(TAG, "Empty response from Gemini Nano for: $imageUri")
                null
            } else {
                Log.i(TAG, "Generated description (${text.length} chars) for: $imageUri")
                text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to describe image: $imageUri", e)
            null
        }
    }

    private fun loadAndResizeBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)

            // Decode bounds first
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // Calculate sample size
            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > MAX_IMAGE_DIMENSION) {
                sampleSize *= 2
            }

            // Decode with sample size
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap: $uriString", e)
            null
        }
    }
}
