# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

file(GLOB_RECURSE SOURCES "src/main/cpp/*.cpp")
file(GLOB_RECURSE HEADERS "src/main/cpp/*.hpp")

add_library(native-lib SHARED ${SOURCES} ${HEADERS} )