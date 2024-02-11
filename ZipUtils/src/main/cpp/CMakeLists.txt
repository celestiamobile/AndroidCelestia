cmake_minimum_required(VERSION 3.4.1)

set(THIRD_PARTY_DIR "${CMAKE_SOURCE_DIR}/../../../../dependencies")
set(THIRD_PARTY_LIB_DIR "${THIRD_PARTY_DIR}/libs")
set(THIRD_PARTY_INCLUDE_DIR "${THIRD_PARTY_DIR}/include")

set(CMAKE_CXX_STANDARD 17)

include_directories(
        ${THIRD_PARTY_INCLUDE_DIR}/libzip
)

add_library(libzip STATIC IMPORTED)
set_target_properties(libzip PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_LIB_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libzip.a)

find_library(
        log-lib
        log )

add_library(
        ziputils
        SHARED
        ZipUtils.cpp )

target_link_libraries(
        ziputils
        z
        libzip
        ${log-lib} )
