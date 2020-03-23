cmake_minimum_required(VERSION 3.4.1)

include_directories(bzip)   #引入头文件
file(GLOB bzip_source_file bzip/*.c)   #引入bzip中所有的c文件
add_library(
        native-lib
        SHARED
        native-lib.cpp
        bspatch.c
        ${bzip_source_file})

find_library(
        log-lib
        log)

target_link_libraries(
        native-lib
        ${log-lib})