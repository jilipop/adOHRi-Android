#include <inttypes.h>
#include <memory>

#include "SoundEngine.h"
#include "log.h"

extern "C" {
#include "andrx.h"
}

SoundEngine::SoundEngine()
        : mLatencyCallback(std::make_unique<LatencyTuningCallback>()),
        mErrorCallback(std::make_unique<ErrorCallback>(*this)) {
}

void SoundEngine::setBufferSizeInBursts(int32_t numBursts) {
    std::lock_guard<std::mutex> lock(mLock);
    if (!mStream) return;

    mLatencyCallback->setBufferTuneEnabled(numBursts == kBufferSizeAutomatic);
    auto result = mStream->setBufferSizeInFrames(
            numBursts * mStream->getFramesPerBurst());
    if (result) {
        LOGD("Buffer size successfully changed to %d", result.value());
    } else {
        LOGW("Buffer size could not be changed, %d", result.error());
    }
}

void SoundEngine::setAudioApi(oboe::AudioApi audioApi) {
    if (mAudioApi != audioApi) {
        mAudioApi = audioApi;
        reopenStream();
    }
}

void SoundEngine::setChannelCount(int channelCount) {
    if (mChannelCount != channelCount) {
        mChannelCount = channelCount;
        reopenStream();
    }
}

void SoundEngine::setDeviceId(int32_t deviceId) {
    if (mDeviceId != deviceId) {
        mDeviceId = deviceId;
        if (reopenStream() != oboe::Result::OK) {
            LOGW("Open stream failed, forcing deviceId to Unspecified");
            mDeviceId = oboe::Unspecified;
        }
    }
}

oboe::Result SoundEngine::openPlaybackStream() {
    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setFormat(oboe::AudioFormat::Float)
        ->setFormatConversionAllowed(true)
        ->setDataCallback(mLatencyCallback.get())
        ->setErrorCallback(mErrorCallback.get())
        ->setAudioApi(mAudioApi)
        ->setChannelCount(mChannelCount)
        ->setDeviceId(mDeviceId)
        ->openStream(mStream);
    if (result == oboe::Result::OK) {
        mChannelCount = mStream->getChannelCount();
    }
    return result;
}

void SoundEngine::restart() {
    // The stream will have already been closed by the error callback.
    mLatencyCallback->reset();
    start();
}

oboe::Result SoundEngine::start() {
    std::lock_guard<std::mutex> lock(mLock);
    mIsLatencyDetectionSupported = false;

    andrx_init(session, decoder);

    auto result = openPlaybackStream();
    if (result == oboe::Result::OK){
        mAudioSource =  std::make_shared<IAudioSource>(mStream->getSampleRate(),
                mStream->getChannelCount());
        mLatencyCallback->setSource(std::dynamic_pointer_cast<IAudioSource>(mAudioSource));

        LOGD("Stream opened: AudioAPI = %d, channelCount = %d, deviceID = %d",
                 mStream->getAudioApi(),
                 mStream->getChannelCount(),
                 mStream->getDeviceId());

        result = mStream->start();
        if (result != oboe::Result::OK) {
            LOGE("Error starting playback stream. Error: %s", oboe::convertToText(result));
            mStream->close();
            mStream.reset();
        } else {
            mIsLatencyDetectionSupported = (mStream->getTimestamp((CLOCK_MONOTONIC)) !=
                                            oboe::Result::ErrorUnimplemented);
        }
    } else {
        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }
    return result;
}

oboe::Result SoundEngine::stop() {
    oboe::Result result = oboe::Result::OK;
    // Stop, close and delete in case not already closed.
    std::lock_guard<std::mutex> lock(mLock);

    andrx_deinit(session, decoder);

    if (mStream) {
        result = mStream->stop();
        mStream->close();
        mStream.reset();
    }
    return result;
}

oboe::Result SoundEngine::reopenStream() {
    if (mStream) {
        stop();
        return start();
    } else {
        return oboe::Result::OK;
    }
}