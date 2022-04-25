#include <inttypes.h>
#include <memory>

#include "SoundEngine.h"

SoundEngine::SoundEngine(){};

void SoundEngine::setBufferSizeInBursts(int32_t numBursts) {
    std::lock_guard<std::mutex> lock(mLock);
    if (!mStream) return;

    auto result = mStream->setBufferSizeInFrames(
            numBursts * mStream->getFramesPerBurst());
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
            mDeviceId = oboe::Unspecified;
        }
    }
}

oboe::Result SoundEngine::openPlaybackStream() {
    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setChannelConversionAllowed(true)
        ->setDirection(oboe::Direction::Output)
        ->setContentType(oboe::ContentType::Speech)
        ->setUsage(oboe::Usage::Media)
        ->setFormat(oboe::AudioFormat::Float)
        ->setFormatConversionAllowed(true)
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
    start();
}

oboe::Result SoundEngine::start() {
    std::lock_guard<std::mutex> lock(mLock);

    Andrx_init();

    auto result = openPlaybackStream();
    if (result == oboe::Result::OK){
        result = mStream->start();
        if (result != oboe::Result::OK) {
            mStream->close();
            mStream.reset();
        }
    }
    isPlayRequested = true;
    rxThread = std::thread(&SoundEngine::Run_rx, this);

    return result;
}

int SoundEngine::Run_rx() {

    int timestamp = 0;

    while (isPlayRequested) {
        int result, have_more;
        char buf[32768];
        void *packet;

        result = rtp_session_recv_with_ts(session, (uint8_t*)buf,
                sizeof(buf), timestamp, &have_more);

        if (result == 0) {
            packet = NULL;
        } else {
            packet = buf;
        }

        result = Play_one_frame(packet, result);
        if (result == -1)
            return -1;

        timestamp += result * referenceRate / rate;
    }
    return 0;
}

int SoundEngine::Play_one_frame(void *packet, size_t len) {

	int numDecodedSamples;
	long samples = 1920;

	float pcm[sizeof(float) * samples * channels];
	if (packet == NULL) {
		numDecodedSamples = opus_decode_float(decoder, NULL, 0, pcm, samples, 1);
	} else {
	    const unsigned char *char_packet = static_cast<unsigned char*>(packet);
		numDecodedSamples = opus_decode_float(decoder, char_packet, len, pcm, samples, 0);
	}
	if (numDecodedSamples < 0) {
		return -1;
	}

    long timeOutNanos = numDecodedSamples * referenceRate / rate * 1000 * 1000;
    oboe::ResultWithValue<int32_t> framesWritten = mStream->write(pcm, numDecodedSamples, timeOutNanos);

	return numDecodedSamples;
}

void SoundEngine::Andrx_init()
{
	int result, error;

	decoder = opus_decoder_create(rate, channels, &error);
	if (decoder == NULL) {
		return;
	}
	ortp_init();
	ortp_scheduler_init();

	session = create_rtp_recv(addr, port, jitter);
}

void SoundEngine::Andrx_deinit()
{
	rtp_session_destroy(session);
	session = NULL;
	ortp_exit();
	opus_decoder_destroy(decoder);
	decoder = NULL;
}

oboe::Result SoundEngine::stop() {
    oboe::Result result = oboe::Result::OK;
    // Stop, close and delete in case not already closed.
    std::lock_guard<std::mutex> lock(mLock);

    isPlayRequested = false;
    rxThread.join();

    if (mStream) {
        result = mStream->stop();
        mStream->close();
        mStream.reset();
        Andrx_deinit();
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