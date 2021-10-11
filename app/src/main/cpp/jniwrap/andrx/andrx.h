#ifndef ANDRX_H
#define ANDRX_H

#include <string.h>
#include <opus/opus.h>
#include <ortp/ortp.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <jni.h>

#include "defaults.h"
#include "log.h"

static int timestamp = 0;
static unsigned int verbose = DEFAULT_VERBOSE,
        buffer = DEFAULT_BUFFER,
		rate = DEFAULT_RATE,
		jitter = DEFAULT_JITTER,
		channels = DEFAULT_CHANNELS,
		port = DEFAULT_PORT;
static const char *device = DEFAULT_DEVICE,
		*addr = DEFAULT_ADDR;

static void timestamp_jump(RtpSession *session, void *a, void *b, void *c);

static RtpSession* create_rtp_recv(const char *addr_desc, const int port,
		unsigned int jitter);

static int decode_one_frame(void *packet,
		size_t len,
		OpusDecoder *decoder,
		float *pcm);

static int get_PCM_frame(RtpSession *session,
		OpusDecoder *decoder,
		float *pcm);

static void andrx_init(RtpSession *session,
                    OpusDecoder *decoder);

static void andrx_deinit(RtpSession *session, OpusDecoder *decoder);


#endif