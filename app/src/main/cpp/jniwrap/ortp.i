%module ortp

%{
#include <bctoolbox/list.h>

#include <ortp/port.h>
#include <ortp/payloadtype.h>
#include <ortp/rtp.h>
#include <ortp/rtcp.h>
#include <ortp/rtpprofile.h>
#include <ortp/rtpsession.h>
#include <ortp/rtpsignaltable.h>
#include <ortp/event.h>
#include <ortp/logging.h>
#include <ortp/sessionset.h>
#include <ortp/utils.h>
#include <ortp/str_utils.h>
#include <ortp/ortp.h>
%}

#define ORTP_PUBLIC

ORTP_PUBLIC void ortp_init(void);

ORTP_PUBLIC void ortp_scheduler_init(void);
ORTP_PUBLIC void ortp_exit(void);
ORTP_PUBLIC void ortp_global_stats_display(void);

ORTP_PUBLIC RtpSession *rtp_session_new(int mode);
ORTP_PUBLIC void rtp_session_set_scheduling_mode(RtpSession *session, int yesno);
ORTP_PUBLIC void rtp_session_set_blocking_mode(RtpSession *session, int yesno);
ORTP_PUBLIC int rtp_session_signal_connect(RtpSession *session,const char *signal_name, RtpCallback cb, void *user_data);
ORTP_PUBLIC void rtp_session_set_jitter_compensation(RtpSession *session, int milisec);
ORTP_PUBLIC void rtp_session_enable_adaptive_jitter_compensation(RtpSession *session, bool_t val);
ORTP_PUBLIC void rtp_session_set_time_jump_limit(RtpSession *session, int milliseconds);
ORTP_PUBLIC int rtp_session_set_local_addr(RtpSession *session,const char *addr, int rtp_port, int rtcp_port); 
ORTP_PUBLIC int rtp_session_set_payload_type(RtpSession *session, int pt);
ORTP_PUBLIC void rtp_session_set_connected_mode(RtpSession *session, bool_t yesno);
ORTP_PUBLIC void rtp_session_enable_rtcp(RtpSession *session, bool_t yesno);
ORTP_PUBLIC int rtp_session_recv_with_ts(RtpSession *session, uint8_t *buffer, int len, uint32_t ts, int *have_more);
ORTP_PUBLIC void rtp_session_resync(RtpSession *session);
ORTP_PUBLIC void rtp_session_destroy(RtpSession *session);
