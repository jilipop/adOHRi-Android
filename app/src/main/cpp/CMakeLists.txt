cmake_minimum_required(VERSION 3.14)

project("AD")

add_library(andrx SHARED jniwrap/andrx/andrx.c
                         jniwrap/andrx/andrx.h
                         jniwrap/andrx/defaults.h
                         jniwrap/andrx/log.h
                         jniwrap/andrx/IRestartable.h
                         jniwrap/andrx/jni.cpp
                         jniwrap/andrx/SoundEngine.cpp
                         jniwrap/andrx/SoundEngine.h)

include_directories(jniwrap/include)

add_library(ortp SHARED IMPORTED)
set_target_properties(ortp PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libortp.so)

add_library(bctoolbox SHARED IMPORTED)
set_target_properties(bctoolbox PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libbctoolbox.so)

add_library(opus SHARED IMPORTED)
set_target_properties(opus PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libopus.so)

find_package (oboe REQUIRED CONFIG)
find_library(c c)
find_library(m m)
find_library(dl dl)
find_library(log log)

target_link_libraries(andrx ortp bctoolbox opus oboe::oboe ${c} ${m} ${dl} ${log})