cmake_minimum_required(VERSION 3.4.1)

set(THIRD_PARTY_LIB_DIR "${CMAKE_SOURCE_DIR}/libs")
set(THIRD_PARTY_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/include")

set(CMAKE_CXX_STANDARD 17)

include_directories(
        ${THIRD_PARTY_INCLUDE_DIR}/breakpad
)

add_library(breakpad STATIC IMPORTED)
set_target_properties(breakpad PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libbreakpad_client.a)

find_library(
        log-lib
        log )

add_library(
        nativecrashhandler
        SHARED
        CrashHandler.cpp )

target_link_libraries(
        nativecrashhandler
        breakpad
        ${log-lib} )
