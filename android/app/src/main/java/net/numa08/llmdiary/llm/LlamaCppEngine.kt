package net.numa08.llmdiary.llm

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * llama.cpp ベースの LLM エンジン実装。
 * TODO: llama.cpp Android バインディングの統合。
 * 現在はスタブ実装。実際の推論ロジックは llama.cpp の JNI バインディングに依存する。
 */
@Singleton
class LlamaCppEngine @Inject constructor() : LlmEngine {

    private var loaded = false

    override suspend fun loadModel(modelPath: String) {
        // TODO: llama.cpp の llama_load_model_from_file を JNI 経由で呼び出す
        loaded = true
    }

    override suspend fun generateText(prompt: String, images: List<Bitmap>): String {
        check(loaded) { "Model is not loaded. Call loadModel() first." }
        // TODO: llama.cpp の llama_decode / llama_sampling を JNI 経由で呼び出す
        // マルチモーダルの場合は llava 等のプロジェクションレイヤーを使用
        return "（LLM推論はまだ実装されていません）"
    }

    override suspend fun unloadModel() {
        // TODO: llama_free でモデルを解放
        loaded = false
    }

    override fun isModelLoaded(): Boolean = loaded
}
