#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <string>
#include <vector>
#include <mutex>

#include "llama.h"
#include "common.h"

#define TAG "LlamaCppJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static llama_model  *g_model   = nullptr;
static llama_context *g_ctx    = nullptr;
static std::mutex     g_mutex;

// ────────────────────────── helpers ──────────────────────────

static std::string jstring_to_string(JNIEnv *env, jstring js) {
    if (!js) return "";
    const char *utf = env->GetStringUTFChars(js, nullptr);
    std::string s(utf);
    env->ReleaseStringUTFChars(js, utf);
    return s;
}

static void throw_runtime(JNIEnv *env, const char *msg) {
    jclass cls = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(cls, msg);
}

// ────────────────────────── JNI exports ──────────────────────────

extern "C" {

JNIEXPORT void JNICALL
Java_net_numa08_llmdiary_llm_LlamaCppEngine_nativeLoadModel(
        JNIEnv *env, jobject /* this */, jstring modelPath) {

    std::lock_guard<std::mutex> lock(g_mutex);

    if (g_model) {
        LOGI("Model already loaded, unloading first");
        if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
        llama_model_free(g_model); g_model = nullptr;
    }

    std::string path = jstring_to_string(env, modelPath);
    LOGI("Loading model from: %s", path.c_str());

    auto model_params = llama_model_default_params();
    // Use GPU layers if available, fall back to CPU
    model_params.n_gpu_layers = 0;

    g_model = llama_model_load_from_file(path.c_str(), model_params);
    if (!g_model) {
        throw_runtime(env, "Failed to load llama model");
        return;
    }

    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx   = 2048;
    ctx_params.n_batch = 512;

    g_ctx = llama_init_from_model(g_model, ctx_params);
    if (!g_ctx) {
        llama_model_free(g_model);
        g_model = nullptr;
        throw_runtime(env, "Failed to create llama context");
        return;
    }

    LOGI("Model loaded successfully");
}

JNIEXPORT jstring JNICALL
Java_net_numa08_llmdiary_llm_LlamaCppEngine_nativeGenerateText(
        JNIEnv *env, jobject /* this */, jstring prompt, jint maxTokens) {

    std::lock_guard<std::mutex> lock(g_mutex);

    if (!g_model || !g_ctx) {
        throw_runtime(env, "Model is not loaded");
        return nullptr;
    }

    std::string prompt_str = jstring_to_string(env, prompt);
    const llama_vocab *vocab = llama_model_get_vocab(g_model);

    // Tokenize the prompt
    int n_prompt_max = prompt_str.size() + 256;
    std::vector<llama_token> tokens(n_prompt_max);
    int n_tokens = llama_tokenize(vocab, prompt_str.c_str(), prompt_str.size(),
                                  tokens.data(), n_prompt_max,
                                  /* add_special */ true, /* parse_special */ true);
    if (n_tokens < 0) {
        throw_runtime(env, "Failed to tokenize prompt");
        return nullptr;
    }
    tokens.resize(n_tokens);

    LOGI("Prompt tokens: %d", n_tokens);

    // Clear KV cache for fresh generation
    llama_kv_cache_clear(g_ctx);

    // Process prompt in batches
    llama_batch batch = llama_batch_init(512, 0, 1);

    for (int i = 0; i < n_tokens; i += 512) {
        int n_eval = std::min(512, n_tokens - i);
        llama_batch_clear(batch);
        for (int j = 0; j < n_eval; j++) {
            llama_batch_add(batch, tokens[i + j], i + j, {0}, false);
        }
        // Only compute logits for the last token of the last batch
        if (i + n_eval >= n_tokens) {
            batch.logits[batch.n_tokens - 1] = true;
        }
        if (llama_decode(g_ctx, batch) != 0) {
            llama_batch_free(batch);
            throw_runtime(env, "Failed to decode prompt");
            return nullptr;
        }
    }

    // Greedy sampling loop
    std::string result;
    int max_gen = (maxTokens > 0) ? maxTokens : 1024;
    llama_token eos = llama_vocab_eos(vocab);

    auto * smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(42));

    for (int i = 0; i < max_gen; i++) {
        llama_token new_token = llama_sampler_sample(smpl, g_ctx, -1);

        if (llama_vocab_is_eog(vocab, new_token)) {
            break;
        }

        // Convert token to text
        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            result.append(buf, n);
        }

        // Prepare next batch
        llama_batch_clear(batch);
        llama_batch_add(batch, new_token, n_tokens + i, {0}, true);
        if (llama_decode(g_ctx, batch) != 0) {
            LOGE("Failed to decode at token %d", i);
            break;
        }
    }

    llama_sampler_free(smpl);
    llama_batch_free(batch);

    LOGI("Generated %zu characters", result.size());
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_net_numa08_llmdiary_llm_LlamaCppEngine_nativeUnloadModel(
        JNIEnv *env, jobject /* this */) {

    std::lock_guard<std::mutex> lock(g_mutex);

    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    LOGI("Model unloaded");
}

JNIEXPORT jboolean JNICALL
Java_net_numa08_llmdiary_llm_LlamaCppEngine_nativeIsModelLoaded(
        JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
