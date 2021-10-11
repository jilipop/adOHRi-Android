cmake_minimum_required(VERSION 3.14)

project("AD")

add_library(andrx SHARED jniwrap/andrx/andrx.c
                         jniwrap/andrx/andrx.h
                         jniwrap/andrx/AndrxOutput.cpp
                         jniwrap/andrx/AndrxOutput.h
                         jniwrap/andrx/DataCallback.h
                         jniwrap/andrx/defaults.h
                         jniwrap/andrx/device.c
                         jniwrap/andrx/device.h
                         jniwrap/andrx/ErrorCallback.h
                         jniwrap/andrx/IAudioSource.h
                         jniwrap/andrx/IRestartable.h
                         jniwrap/andrx/LatencyTuningCallback.cpp
                         jniwrap/andrx/LatencyTuningCallback.h
                         jniwrap/andrx/log.h
                         jniwrap/andrx/SoundEngine.cpp
                         jniwrap/andrx/SoundEngine.h
                         jniwrap/andrx/trace.cpp
                         jniwrap/andrx/trace.h)

#add_library(andrx SHARED jniwrap/andrx/andrx.c jniwrap/andrx/device.c)

include_directories(jniwrap/include)

add_library(ortp SHARED IMPORTED)
set_target_properties(ortp PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libortp.so)

add_library(bctoolbox SHARED IMPORTED)
set_target_properties(bctoolbox PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libbctoolbox.so)

add_library(opus SHARED IMPORTED)
set_target_properties(opus PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libopus.so)

#add_library(minialsa SHARED IMPORTED)
#set_target_properties(minialsa PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libapcm.so)

#add_library(c++ SHARED IMPORTED)
#set_target_properties(c++ PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libc++_shared.so)

find_package (oboe REQUIRED CONFIG)
find_library(c c)
find_library(m m)
find_library(dl dl)
find_library(log log)

target_link_libraries(andrx ortp bctoolbox opus oboe::oboe ${c} ${m} ${dl} ${log})


#target_link_libraries(andrx ortp bctoolbox opus minialsa ${c} ${m} ${dl} ${log})


#add_library(ortpwrapped SHARED jniwrap/ortp_wrap.c)

#add_library(ortp STATIC IMPORTED)
#set_target_properties(ortp PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libortp.a)
#add_library(bctoolbox STATIC IMPORTED)
#set_target_properties(bctoolbox PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libbctoolbox.a)
#add_library(mbedtls STATIC IMPORTED)
#set_target_properties(mbedtls PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libmbedtls.a)
#add_library(mbedcrypto STATIC IMPORTED)
#set_target_properties(mbedcrypto PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libmbedcrypto.a)
#add_library(mbedx509 STATIC IMPORTED)
#set_target_properties(mbedx509 PROPERTIES IMPORTED_LOCATION ${CMAKE_HOME_DIRECTORY}/libs/${ANDROID_ABI}/libmbedx509.a)

#set(CMAKE_POSITION_INDEPENDENT_CODE ON)