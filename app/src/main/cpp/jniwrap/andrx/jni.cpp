/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <oboe/Oboe.h>
#include "SoundEngine.h"

extern "C" {

    /**
     * Creates the audio engine
     *
     * @return a pointer to the audio engine. This should be passed to other methods
     */
    JNIEXPORT jlong JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1createEngine(
            JNIEnv *env,
            jclass /*unused*/) {

        // We use std::nothrow so `new` returns a nullptr if the engine creation fails
        SoundEngine *engine = new(std::nothrow) SoundEngine();
        if (engine == nullptr) {
            return 0;
        }
        return reinterpret_cast<jlong>(engine);
    }

    JNIEXPORT jint JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1startEngine(
            JNIEnv *env,
            jclass,
            jlong engineHandle) {

        SoundEngine *engine = reinterpret_cast<SoundEngine *>(engineHandle);
        return static_cast<jint>(engine->start());
    }

    JNIEXPORT jint JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1stopEngine(
            JNIEnv *env,
            jclass,
            jlong engineHandle) {

        SoundEngine *engine = reinterpret_cast<SoundEngine *>(engineHandle);
        return static_cast<jint>(engine->stop());
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1deleteEngine(
            JNIEnv *env,
            jclass,
            jlong engineHandle) {

        SoundEngine *engine = reinterpret_cast<SoundEngine *>(engineHandle);
        engine->stop();
        delete engine;
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1setAudioApi(
            JNIEnv *env,
            jclass type,
            jlong engineHandle,
            jint audioApi) {

        SoundEngine *engine = reinterpret_cast<SoundEngine*>(engineHandle);
        if (engine == nullptr) {
            return;
        }

        oboe::AudioApi api = static_cast<oboe::AudioApi>(audioApi);
        engine->setAudioApi(api);
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1setAudioDeviceId(
            JNIEnv *env,
            jclass,
            jlong engineHandle,
            jint deviceId) {

        SoundEngine *engine = reinterpret_cast<SoundEngine*>(engineHandle);
        if (engine == nullptr) {
            return;
        }
        engine->setDeviceId(deviceId);
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1setChannelCount(
            JNIEnv *env,
            jclass type,
            jlong engineHandle,
            jint channelCount) {

        SoundEngine *engine = reinterpret_cast<SoundEngine*>(engineHandle);
        if (engine == nullptr) {
            return;
        }
        engine->setChannelCount(channelCount);
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1setBufferSizeInBursts(
            JNIEnv *env,
            jclass,
            jlong engineHandle,
            jint bufferSizeInBursts) {

        SoundEngine *engine = reinterpret_cast<SoundEngine*>(engineHandle);
        if (engine == nullptr) {
            return;
        }
        engine->setBufferSizeInBursts(bufferSizeInBursts);
    }

    JNIEXPORT void JNICALL
    Java_io_github_jilipop_adohri_jni_AdReceiver_native_1setDefaultStreamValues(
            JNIEnv *env,
            jclass type,
            jint sampleRate,
            jint framesPerBurst) {
        oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
        oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
    }
}