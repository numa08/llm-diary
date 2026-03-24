package net.numa08.llmdiary.llm

import android.graphics.Bitmap

interface LlmEngine {
    suspend fun loadModel(modelPath: String)
    suspend fun generateText(prompt: String, images: List<Bitmap> = emptyList()): String
    suspend fun unloadModel()
    fun isModelLoaded(): Boolean
}
