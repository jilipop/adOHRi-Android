#include "andrx.h"

static void timestamp_jump(RtpSession *session, void *a, void *b, void *c)
{
    LOGD("Calling timestamp_jump");
	if (verbose > 1)
		LOGV("|");
	rtp_session_resync(session);
}

static RtpSession* create_rtp_recv(const char *addr_desc, const int port,
		unsigned int jitter)
{
	RtpSession *session;

	session = rtp_session_new(RTP_SESSION_RECVONLY);
	rtp_session_set_scheduling_mode(session, FALSE);
	rtp_session_set_blocking_mode(session, FALSE);
	rtp_session_set_local_addr(session, addr_desc, port, -1);
	rtp_session_set_connected_mode(session, FALSE);
	rtp_session_enable_adaptive_jitter_compensation(session, TRUE);
	rtp_session_set_jitter_compensation(session, jitter); /* ms */
	rtp_session_set_time_jump_limit(session, jitter * 16); /* ms */
	if (rtp_session_set_payload_type(session, 0) != 0)
		abort();
	if (rtp_session_signal_connect(session, "timestamp_jump",
					timestamp_jump, 0) != 0)
	{
		abort();
	}

	rtp_session_enable_rtcp(session, FALSE);

	return session;
}

static int decode_one_frame(void *packet,
		size_t len,
		OpusDecoder *decoder,
		float *pcm)
{
    LOGD("Calling decode_one_frame");

	int numDecodedSamples;
	int framesWritten, samples = 1920;

	pcm = alloca(sizeof(*pcm) * samples * channels);

	if (packet == NULL) {
		numDecodedSamples = opus_decode_float(decoder, NULL, 0, pcm, samples, 1);
	} else {
		numDecodedSamples = opus_decode_float(decoder, packet, len, pcm, samples, 0);
	}
	if (numDecodedSamples < 0) {
		LOGE("Error on opus_decode: %s\n", opus_strerror(numDecodedSamples));
		return -1;
	}

//TODO
//	framesWritten = snd_pcm_writei(snd, pcm, numDecodedSamples);
//	if (framesWritten < 0) {
//		framesWritten = snd_pcm_recover(snd, framesWritten, 0);
//		if (framesWritten < 0) {
//			LOGE("Error on snd_pcm_writei: %s\n", snd_strerror(framesWritten));
//			return -1;
//		}
//		return 0;
//	}
//	if (framesWritten < numDecodedSamples)
//		LOGE("Short write %ld\n", framesWritten);

	return numDecodedSamples;
}

static int get_PCM_frame(RtpSession *session,
		OpusDecoder *decoder,
		float *pcm)
{
    LOGD("Calling getPcmFrame");

    int result, have_more;
    char buf[32768];
    void *packet;

    result = rtp_session_recv_with_ts(session, (uint8_t*)buf,
            sizeof(buf), timestamp, &have_more);
    assert(result >= 0);
    assert(have_more == 0);
    if (result == 0) {
        packet = NULL;
        if (verbose > 1)
            LOGV("#");
    } else {
        packet = buf;
        if (verbose > 1)
            LOGV(".");
    }

    result = decode_one_frame(packet, result, decoder, pcm);
    if (result == -1)
        return -1;

    /* Follow the RFC, payload 0 has 8kHz reference rate */
    timestamp += result * 8000 / rate;
}

//extern "C" JNIEXPORT void JNICALL Java_io_github_jilipop_ad_jni_AdReceiverJNI_run (JNIEnv* env, jobject thisObject)

static void andrx_init(RtpSession *session,
                     	OpusDecoder *decoder)
{
	int result, error;

	decoder = opus_decoder_create(rate, channels, &error);
	if (decoder == NULL) {
		LOGE("Error on opus_decoder_create: %s\n", opus_strerror(error));
		return;
	}

	ortp_init();
	ortp_scheduler_init();
	session = create_rtp_recv(addr, port, jitter);
	assert(session != NULL);

	LOGD("Receiver initialized.");

//TODO
//	result = snd_pcm_open(&snd, device, SND_PCM_STREAM_PLAYBACK, 0);
//	if (result < 0) {
//		LOGE("Error on snd_pcm_open: %s\n", snd_strerror(result));
//	return -1;
//	}
//	if (set_alsa_hw(snd, rate, channels, buffer * 1000) == -1){
//		LOGE("Error on snd_alsa_hw");
//	    return -1;
//    }
//	if (set_alsa_sw(snd) == -1){
//	    LOGE("Error on snd_alsa_sw");
//    	return -1;
//    }
}

static void andrx_deinit(RtpSession *session, OpusDecoder *decoder)
{
	rtp_session_destroy(session);
	ortp_exit();

//	ortp_global_stats_display();

	opus_decoder_destroy(decoder);
}
