cmake_minimum_required(VERSION 3.22.1)
project(celestiaxr VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── OpenXR SDK (headers + loader built from Khronos source) ──────────────────
include(FetchContent)

# Disable everything except the loader itself
set(BUILD_LOADER            ON  CACHE BOOL "" FORCE)
set(BUILD_API_LAYERS        OFF CACHE BOOL "" FORCE)
set(BUILD_TESTS             OFF CACHE BOOL "" FORCE)
set(BUILD_CONFORMANCE_TESTS OFF CACHE BOOL "" FORCE)

FetchContent_Declare(
    openxr_sdk
    GIT_REPOSITORY https://github.com/KhronosGroup/OpenXR-SDK.git
    GIT_TAG        release-1.1.38
    GIT_SHALLOW    ON
)
FetchContent_MakeAvailable(openxr_sdk)

# ── Android system libraries ──────────────────────────────────────────────────
find_library(log-lib     log)
find_library(android-lib android)
find_library(EGL-lib     EGL)
find_library(GLESv3-lib  GLESv3)

# ── Celestia native shared library ────────────────────────────────────────────
add_library(
    celestiaxr
    SHARED
        celestia_openxr.cpp
        jni_bridge.cpp
)

target_include_directories(celestiaxr PRIVATE
    ${openxr_sdk_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/../../../../Celestia/src/main/cpp
)

target_compile_definitions(celestiaxr PRIVATE
    XR_USE_PLATFORM_ANDROID
    XR_USE_GRAPHICS_API_OPENGL_ES
    ANDROID
)

find_package(Celestia REQUIRED CONFIG)

target_link_libraries(celestiaxr
    openxr_loader
    ${log-lib}
    ${android-lib}
    ${EGL-lib}
    ${GLESv3-lib}
    Celestia::celestia
)
