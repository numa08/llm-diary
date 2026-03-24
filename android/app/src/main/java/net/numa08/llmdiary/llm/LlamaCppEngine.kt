package net.numa08.llmdiary.llm

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * llama.cpp ベースの LLM エンジン実装。
 * JNI 経由で llama.cpp のネイティブライブラリを呼び出す。
 */
@Singleton
class LlamaCppEngine @Inject constructor() : LlmEngine {

    companion object {
        private const val TAG = "LlamaCppEngine"
        private const val MAX_TOKENS = 1024

        init {
            System.loadLibrary("llm_diary_jni")
        }
    }

    // JNI native methods
    private external fun nativeLoadModel(modelPath: String)
    private external fun nativeGenerateText(prompt: String, maxTokens: Int): String
    private external fun nativeUnloadModel()
    private external fun nativeIsModelLoaded(): Boolean

    override suspend fun loadModel(modelPath: String) = withContext(Dispatchers.IO) {
        Log.i(TAG, "Loading model: $modelPath")
        nativeLoadModel(modelPath)
        Log.i(TAG, "Model loaded successfully")
    }

    override suspend fun generateText(prompt: String, images: List<Bitmap>): String =
        withContext(Dispatchers.IO) {
            check(isModelLoaded()) { "Model is not loaded. Call loadModel() first." }

            if (images.isNotEmpty()) {
                Log.w(TAG, "Image input is not yet supported in llama.cpp engine, ignoring ${images.size} images")
            }

            Log.i(TAG, "Generating text (prompt length: ${prompt.length} chars)")
            val result = nativeGenerateText(prompt, MAX_TOKENS)
            Log.i(TAG, "Generation complete (${result.length} chars)")
            result
        }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Unloading model")
        nativeUnloadModel()
    }

    override fun isModelLoaded(): Boolean = nativeIsModelLoaded()
}
