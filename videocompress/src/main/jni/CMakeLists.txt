cmake_minimum_required(VERSION 3.6)

set(CMAKE_VERBOSE_MAKEFILE on)

set(include "${CMAKE_CURRENT_SOURCE_DIR}/libyuv/include")
set(src "${CMAKE_CURRENT_SOURCE_DIR}/libyuv/source")

include_directories("${include}")

add_library(videocompress SHARED
    ${src}/compare_common.cc
    ${src}/compare_gcc.cc
    ${src}/compare_neon.cc
    ${src}/compare_neon64.cc
    ${src}/compare_win.cc
    ${src}/compare.cc
    ${src}/convert_argb.cc
    ${src}/convert_from_argb.cc
    ${src}/convert_from.cc
    ${src}/convert_jpeg.cc
    ${src}/convert_to_argb.cc
    ${src}/convert_to_i420.cc
    ${src}/convert.cc
    ${src}/cpu_id.cc
    ${src}/mjpeg_decoder.cc
    ${src}/mjpeg_validate.cc
    ${src}/planar_functions.cc
    ${src}/rotate_any.cc
    ${src}/rotate_argb.cc
    ${src}/rotate_common.cc
    ${src}/rotate_gcc.cc
    ${src}/rotate_mips.cc
    ${src}/rotate_neon.cc
    ${src}/rotate_neon64.cc
    ${src}/rotate_win.cc
    ${src}/rotate.cc
    ${src}/row_any.cc
    ${src}/row_common.cc
    ${src}/row_gcc.cc
    ${src}/row_mips.cc
    ${src}/row_neon.cc
    ${src}/row_neon64.cc
    ${src}/row_win.cc
    ${src}/scale_any.cc
    ${src}/scale_argb.cc
    ${src}/scale_common.cc
    ${src}/scale_gcc.cc
    ${src}/scale_mips.cc
    ${src}/scale_neon.cc
    ${src}/scale_neon64.cc
    ${src}/scale_win.cc
    ${src}/scale.cc
    ${src}/video_common.cc
    video.c)
set_target_properties(videocompress PROPERTIES
    LINKER_LANGUAGE CXX)

target_link_libraries(videocompress)