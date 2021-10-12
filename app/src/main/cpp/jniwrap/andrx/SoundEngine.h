#ifndef SoundEngine_H
#define SoundEngine_H

#include <oboe/Oboe.h>
#include "IRestartable.h"
#include "defaults.h"

extern "C" {
#include "andrx.h"
}

constexpr int32_t kBufferSizeAutomatic = 0;

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

private:
    oboe::Result reopenStream();
    oboe::Result openPlaybackStream();

    int Run_rx(RtpSession *session, OpusDecoder *decoder);
    int Decode_one_frame(void *packet, size_t len, OpusDecoder *decoder);

    unsigned int rate = DEFAULT_RATE,
    		jitter = DEFAULT_JITTER,
    		channels = DEFAULT_CHANNELS,
    		port = DEFAULT_PORT;
    const char *addr = DEFAULT_ADDR;

    std::shared_ptr<oboe::AudioStream> mStream;

    int32_t        mDeviceId = oboe::Unspecified;
    int32_t        mChannelCount = oboe::Unspecified;
    oboe::AudioApi mAudioApi = oboe::AudioApi::Unspecified;
    std::mutex     mLock;
};

#endif