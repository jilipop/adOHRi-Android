/*
 * Copyright 2019 The Android Open Source Project
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

#ifndef DATA_CALLBACK_H
#define DATA_CALLBACK_H

#include <vector>
#include <oboe/AudioStreamCallback.h>

#include "log.h"
#include "IRestartable.h"
#include "IAudioSource.h"

class DataCallback : public oboe::AudioStreamDataCallback {
public:
    DataCallback() {}
    virtual ~DataCallback() = default;

    virtual oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override {

        if (mIsThreadAffinityEnabled && !mIsThreadAffinitySet) {
            setThreadAffinity();
            mIsThreadAffinitySet = true;
        }

        float *outputBuffer = static_cast<float *>(audioData);

        std::shared_ptr<IAudioSource> localSource = mAudioSource;
        if (!localSource) {
            LOGE("Audio source not set!");
            return oboe::DataCallbackResult::Stop;
        }
        localSource->fillFrame(outputBuffer);
        return oboe::DataCallbackResult::Continue;
    }

    void setSource(std::shared_ptr<IAudioSource> source) {
        mAudioSource = source;
    }

    /**
     * Reset the callback to its initial state.
     */
    void reset(){
        mIsThreadAffinitySet = false;
    }

    std::shared_ptr<IAudioSource> getSource() {
        return mAudioSource;
    }

    /**
     * Set the CPU IDs to bind the audio callback thread to
     *
     * @param mCpuIds - the CPU IDs to bind to
     */
    void setCpuIds(std::vector<int> cpuIds){
        mCpuIds = std::move(cpuIds);
    }

    /**
     * Enable or disable binding the audio callback thread to specific CPU cores. The CPU core IDs
     * can be specified using @see setCpuIds. If no CPU IDs are specified the initial core which the
     * audio thread is called on will be used.
     *
     * @param isEnabled - whether the audio callback thread should be bound to specific CPU core(s)
     */
    void setThreadAffinityEnabled(bool isEnabled){
        mIsThreadAffinityEnabled = isEnabled;
        LOGD("Thread affinity enabled: %s", (isEnabled) ? "true" : "false");
    }

private:
    std::shared_ptr<IAudioSource> mAudioSource;
    std::vector<int> mCpuIds; // IDs of CPU cores which the audio callback should be bound to
    std::atomic<bool> mIsThreadAffinityEnabled { false };
    std::atomic<bool> mIsThreadAffinitySet { false };

    /**
     * Set the thread affinity for the current thread to mCpuIds. This can be useful to call on the
     * audio thread to avoid underruns caused by CPU core migrations to slower CPU cores.
     */
    void setThreadAffinity() {

        pid_t current_thread_id = gettid();
        cpu_set_t cpu_set;
        CPU_ZERO(&cpu_set);

        // If the callback cpu ids aren't specified then bind to the current cpu
        if (mCpuIds.empty()) {
            int current_cpu_id = sched_getcpu();
            LOGD("Binding to current CPU ID %d", current_cpu_id);
            CPU_SET(current_cpu_id, &cpu_set);
        } else {
            LOGD("Binding to %d CPU IDs", static_cast<int>(mCpuIds.size()));
            for (size_t i = 0; i < mCpuIds.size(); i++) {
                int cpu_id = mCpuIds.at(i);
                LOGD("CPU ID %d added to cores set", cpu_id);
                CPU_SET(cpu_id, &cpu_set);
            }
        }

        int result = sched_setaffinity(current_thread_id, sizeof(cpu_set_t), &cpu_set);
        if (result == 0) {
            LOGV("Thread affinity set");
        } else {
            LOGW("Error setting thread affinity. Error no: %d", result);
        }

        mIsThreadAffinitySet = true;
    }

};

#endif