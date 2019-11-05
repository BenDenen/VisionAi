# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# ---- Android dependencies ----
find_library(android-lib android)
find_library(log-lib log)
include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()

# ----- Boost include -----

if (DEFINED ENV{ROOT_BOOST_PATH})
    set(BOOST_ROOT "$ENV{ROOT_BOOST_PATH}/")
    set(Boost_INCLUDE_DIR "$ENV{ROOT_BOOST_PATH}/")
    # TODO We would use `if (APPLE)`, similarly to cv-assist-core switches, but APPLE is not
    # implemented in cmake 3.6, which is shipped with Android SDK.
elseif (${CMAKE_HOST_SYSTEM} MATCHES "Darwin")
    set(BOOST_ROOT /usr/local/opt/boost)
    set(Boost_INCLUDE_DIR ${BOOST_ROOT}/include)
else ()
    message(FATAL_ERROR "Boost is not found you should it with ROOT_BOOST_PATH env!")
endif()

include_directories(${Boost_INCLUDE_DIR})




file(GLOB_RECURSE UTILS_SOURCES "src/main/cpp/utils/*.cpp")
file(GLOB_RECURSE UTILS_HEADERS "src/main/cpp/utils/*.hpp")

add_library(image-utils SHARED ${UTILS_SOURCES} ${UTILS_HEADERS})
target_link_libraries(
        image-utils
        ${log-lib}
        ${android-lib}
)