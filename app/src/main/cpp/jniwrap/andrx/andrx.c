#include "andrx.h"



void timestamp_jump(RtpSession *session, void *a, void *b, void *c)
{
    LOGD("Calling timestamp_jump");
	LOGV("|");
	rtp_session_resync(session);
}

RtpSession* create_rtp_recv(const char *addr_desc, const int port,
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



void andrx_init(RtpSession *session, OpusDecoder *decoder, unsigned int rate, unsigned int channels, const char *addr, unsigned int port, unsigned int jitter)
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

	LOGD("Receiver initialized.");
}

void andrx_deinit(RtpSession *session, OpusDecoder *decoder)
{
	rtp_session_destroy(session);
	ortp_exit();
	opus_decoder_destroy(decoder);

	LOGD("Receiver destroyed.");
}
