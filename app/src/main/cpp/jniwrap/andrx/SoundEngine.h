#ifndef SoundEngine_H
#define SoundEngine_H

#include <oboe/Oboe.h>

#include "LatencyTuningCallback.h"
#include "IRestartable.h"
#include "ErrorCallback.h"

extern "C" {
#include "andrx.h"
}

class SoundEngine : public IRestartable {

public:
    SoundEngine();

    virtual ~SoundEngine() = default;

    oboe::Result start();
    oboe::Result stop();

    void restart() override;

    void setDeviceId(int32_t deviceId);
    void setChannelCount(int channelCount);
    void setAudioApi(oboe::AudioApi audioApi);
    void setBufferSizeInBursts(int32_t numBursts);

    std::shared_ptr<RtpSession> getSession() {
        return session;
    }
    std::shared_ptr<OpusDecoder> getDecoder() {
            return decoder;
        }

private:
    oboe::Result reopenStream();
    oboe::Result openPlaybackStream();

    std::shared_ptr<oboe::AudioStream> mStream;
    std::shared_ptr<RtpSession> session;
    std::shared_ptr<OpusDecoder> decoder;
    std::unique_ptr<LatencyTuningCallback> mLatencyCallback;
    std::unique_ptr<ErrorCallback> mErrorCallback;

    int32_t        mDeviceId = oboe::Unspecified;
    int32_t        mChannelCount = oboe::Unspecified;
    oboe::AudioApi mAudioApi = oboe::AudioApi::Unspecified;
    std::mutex     mLock;
}

#endif