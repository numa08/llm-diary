cmake_minimum_required(VERSION 3.22.1)
project(llm_diary_jni LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---------- llama.cpp via FetchContent ----------
include(FetchContent)

FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp.git
    GIT_TAG        b5450
    GIT_SHALLOW    TRUE
)

# Disable targets we don't need
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(llama_cpp)

# ---------- JNI bridge ----------
find_package(JNI REQUIRED)

add_library(llm_diary_jni SHARED
    llama_jni.cpp
)

target_include_directories(llm_diary_jni PRIVATE
    ${llama_cpp_SOURCE_DIR}/include
    ${llama_cpp_SOURCE_DIR}/common
    ${JNI_INCLUDE_DIRS}
)

target_link_libraries(llm_diary_jni PRIVATE
    llama
    common
    ggml
    log
    android
)
