#ifndef ANDRX_H
#define ANDRX_H

#include <string.h>
#include <opus/opus.h>
#include <ortp/ortp.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <jni.h>

#include "log.h"

void timestamp_jump(RtpSession *session, void *a, void *b, void *c);

RtpSession* create_rtp_recv(const char *addr_desc, const int port,
		unsigned int jitter);

#endif