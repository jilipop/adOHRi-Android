include(FetchContent)

#option (USE_SUPERBUILD ON)

#set(DEPS_SOURCE_DIR .)

#if (USE_SUPERBUILD)
#  project (SUPERBUILD NONE)
#  include (cmake/SuperBuild.cmake)
#  return()
#else()

project("ortp")

FetchContent_Declare(
  ortp
  GIT_REPOSITORY "https://github.com/BelledonneCommunications/ortp"
  GIT_TAG 1ef30496bd19f892bc376672d809b69acfa92801 #v5.0.24
)

FetchContent_Declare(
  bctoolbox
  GIT_REPOSITORY "https://github.com/BelledonneCommunications/bctoolbox"
  GIT_TAG 203d4f5a4fdf8ef8fc6cd10bab9cb28fd228c867 #v5.0.24
)

message(STATUS "Fetching bctoolbox")

set(CMAKE_FIND_ROOT_PATH "${CMAKE_FIND_ROOT_PATH};${CMAKE_HOME_DIRECTORY}/ortp/bctoolbox/mbedtls/${ANDROID_ABI};${CMAKE_HOME_DIRECTORY}/ortp/bctoolbox/mbedtls")

message(STATUS "CMAKE_FIND_ROOT_PATH = ${CMAKE_FIND_ROOT_PATH}")

set(ENABLE_TESTS OFF)
set(ENABLE_TESTS_COMPONENT OFF)
set(ENABLE_POLARSSL OFF)
set(ENABLE_DECAF OFF)
FetchContent_MakeAvailable(bctoolbox)

message(STATUS "Fetching ortp")
set(bctoolbox_DIR ${CMAKE_HOME_DIRECTORY}/ortp/bctoolbox/cmake)
set(ENABLE_DOC NO)
FetchContent_MakeAvailable(ortp)

#  set (CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
#  set (CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
#  set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

#endif()
