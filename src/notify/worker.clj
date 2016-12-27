
(ns notify.worker)


; public static final int NOP     = 0;    // void message
;
; // type constants
; public static final int FORUM_NEW_MSG    = 101; // i == topic_id, j == msg_id
; public static final int FORUM_NEW_TOPIC  = 102; // i == group_id, j == topic_id
; public static final int FORUM_TOPIC_MOVE = 103; // i == from_group, j == topic_id
;
; public static final int MAIL_NEW_MSG     = 201; // i == to_user
;
; //
;
; public long     stamp;          // timestamp
; public int              type;           // message type
;
; public int              user_id;        // originating user ID or 0
; public String   ipaddr;         // request source addr
;
; public int              i;                      // int param
; public int              j;                      // secondary int param
; public String   s;                      // string data (limited to 64000)



; if cmd == "cleanup":
;     worker.cleanup()
; elif cmd == "notify":
;     notify.notify_mail()
;     notify.notify_forum()
; else:
;     worker.run()


;;.
