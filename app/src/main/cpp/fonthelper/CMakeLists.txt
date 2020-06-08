cmake_minimum_required(VERSION 3.4.1)

find_library(
        log-lib
        log )

add_library(
        fonthelper
        SHARED
        FontHelper.cpp )

target_link_libraries(
        fonthelper
        ${log-lib} )
