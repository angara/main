(ns angara.webapp.forum.topic
  (:import [java.time LocalDateTime Duration])
  (:require
    [clojure.string :as str]
    [tick.core :as t]
    ;
    [mlib.core :refer [hesc to-long]]
   ))


(set! *warn-on-reflection* true)

(def TOPIC_PAGE_SIZE 50)
(def FORUM_ATTACH_THUMB_MSGID 320000)
(def ATTACH_URI_BASE "/upload")


; - - - - - - - - - - - - - - - - - - -

(def ^Duration UPDATED_MESSAGE_DURATION (t/new-duration 5 :minutes))

(defn is-updated? [^LocalDateTime created-at ^LocalDateTime updated-at]
  (and created-at updated-at
       (t/> updated-at (t/>> created-at UPDATED_MESSAGE_DURATION))))

; - - - - - - - - - - - - - - - - - - -


(def URL_LENGTH_MAX 100)
(def MSGID_TYPE_1   1200000)

(def RE_TAIL_SPACES #"\s+$")
(def RE_INDENT      #"(?m)((?<=^)(  +))") ;; 3 spaces
(def RE_NLBR        #"(?m)(\n)")
(def RE_URL         #"(?m)\b(https?://[-a-zA-Z0-9()@:%_\+\.,;~#?&\/=]{4,})")
(def RE_MSGREF      #"(?m)(\[\d{4,8}])")
(def RE_CITE_Q      #"(?ms)\[q\](.*?)\[/q\]")
(def RE_BOLD_1      #"(?m)(?<=^|\s)\*(\S+)\*(?=$|\s)")
(def RE_UNDER_1     #"(?m)(?<=^|\s)_(\S+)_(?=$|\s)")
(def RE_BOLD_2      #"(?m)(?<=^|\s)\*\*(.+?)\*\*(?=$|\s)")
(def RE_UNDER_2     #"(?m)(?<=^|\s)__(.+?)__(?=$|\s)")


(comment
  
  (str/replace "\n\n\n\n" RE_TAIL_SPACES "")
  ;; => "" 

  (str/replace "qwe *bold1*" RE_BOLD_1 (fn [[_ m]] (str "<b>" m "</b>")))
  ;; => "qwe <b>bold1</b>"

  (str/replace "qwe *bold1**" RE_BOLD_1 (fn [[_ m ]] (str "<b>" m "</b>")))
  ;; => "qwe *bold1**"
  
  (str/replace "\"Тестовая страница: https://angara.net/forum/topic/121358\"" RE_URL 
               (fn [[_ m]] (prn m) (str ">>" m "<<")) )
  ;; => "\"Тестовая страница: >>https://angara.net/forum/topic/121358<<\""

  ,)


(defn format-body [s re_bold re_under]
    (-> (hesc s)
        (str/replace RE_TAIL_SPACES "")
        (str/replace RE_INDENT "&nbsp;&nbsp;&nbsp; ")
        (str/replace re_bold (fn [[_ m]] (str "<b>" m "</b>")))
        (str/replace re_under (fn [[_ m]] (str "<u>" m "</u>")))
        (str/replace RE_URL (fn [[_ m]]
                              (str "<a class=\"forum_ahref\" target=\"_blank\" href=\"" m "\">" 
                                   (if (< URL_LENGTH_MAX (count m))
                                     (str (subs m 0 URL_LENGTH_MAX) "...")
                                     m
                                     )
                                   "</a>"
                                   )
                             ))
        (str/replace RE_CITE_Q (fn [[_ m]]
                                 (str "<span class=\"forum_cite_q\">" m "</span>")
                                 ))
        (str/replace RE_NLBR "<br/>\n")
        (str/replace RE_MSGREF (fn [[_ m]]
                                 (str "<span class=\"forum_msgref cursor-pointer\"" 
                                      " onclick=\"return forum_popmsg(" m ")\">" m "</span>")
                                 ))
        ))
  
  
(defn format-msg-text [{:keys [msgid body]}]
  (cond
    (< msgid MSGID_TYPE_1) 
    (format-body body RE_BOLD_1 RE_UNDER_1)
    :else 
    (format-body body RE_BOLD_2 RE_UNDER_2)))


(comment
  
  (format-msg-text {:msgid 1 :body 
                    "\"Тестовая страница: https://angara.net/forum/topic/121358\""
                    })
  ;; => "\"Тестовая страница: <a class=\"forum_ahref\" 
  ;;  target=\"_blank\" href=\"https://angara.net/forum/topic/121358\">https://angara.net/forum/topic/121358</a>\""
  
  ,)


(defn attach-uri [msgid ext]
  (when (< 1000 msgid)
    (let [s (str msgid)
          len (.length s)]
      (str ATTACH_URI_BASE "/"
           (subs s (- len 4) (- len 2)) "/" (subs s (- len 2) len) "/f_" s ext)
      )))


(defn format-attach [{:keys [msgid attach]}]
  (when-not (str/blank? attach)
    (let [[ext size] (str/split attach #"\s")
          fsize (quot (to-long size) 1024)
          ]
      (if (and (< FORUM_ATTACH_THUMB_MSGID msgid) (= "jpg" ext))
        [:div.forum_attach
          [:a.forum_ahref {:target "_blank" :href (attach-uri msgid ".jpg")}
            [:img.forum_attach_thumb {:src (attach-uri msgid "_s.jpg") }]]]
        [:div.forum_attach
          "Файл: " [:a.forum_ahref {:target "_blank" :href (attach-uri msgid (str "." ext))} 
                      "/" fsize " kb/." ext]]
        ))))


(comment

  (attach-uri 1234 ".jpg")
  ;; => "/upload/12/34/f_1234.jpg"

  (attach-uri 1451521 "_s.jpg")
  ;; => "/upload/15/21/f_1451521_s.jpg"
  
  ,)

  ;;  static String formatAttach(Msg m) {
  ;;       String[] ap = m.attachParam();
  ;;       if( ap == null ) {
  ;;           return "";
  ;;       }

  ;;       StringBuilder sb = new StringBuilder();
  ;;       sb.append("<div class=\"attach\">");

  ;;       String f_uri = ap[0];
  ;;       int fsize = Lib.to_i( ap[2] ) / 1024;
  ;;       String a = "<a target=\"_blank\" href=\""+f_uri+"\""+
  ;;               ((m.msgid < MSGID_W99)?" data-w99='1'":"")+">";

  ;;       // jpeg thumbnails implemented after this msgid
  ;;       if( m.msgid > 320000 && m.attach.startsWith("jpg ") )
  ;;       {
  ;;           sb.append(a);
  ;;           sb.append("<img class=\"thumb\" src=\""+m.attach_baseuri()+"_s.jpg\" align=\"middle\" /></a>");
  ;;       }
  ;;       else {
  ;;           sb.append( "Файл:" );
  ;;       }

  ;;       sb.append(" "+a+"["+fsize+" kb]."+ap[1]+"</a>");

  ;;       if(f_uri.endsWith(".kml") || f_uri.endsWith(".kmz")) {
  ;;               sb.append("<i class=\"i-kml\" style=\"margin-left: 6px;\" kml=\""+"http://angara.net"+f_uri+"\"></i>");
  ;;       }
  ;;       sb.append("</div>");
  ;;       return sb.toString();
  ;;   }

;; public String [] attachParam ()
;; {if (attach == null) return null;
;;  try {String [] t = attach.split ("\\s+", 2);
;;       String type = t [0];
;;       String size = t [1];

;;       String d0 = Integer.toString ((msgid % 10000) / 100);
;;       if (d0.length () < 2) d0 = "0" +d0;

;;       String d1 = Integer.toString (msgid % 100);
;;       if (d1.length () < 2) d1 = "0" +d1;

;;       String uri = Const.UPLOAD_BASE_URI +d0+ "/" +d1+ "/" +ATTACH_PREFIX+msgid+ "." +type;

;;       return new String [] {uri, type, size};
;;       }
;;  catch (Exception ex) {L.warning ("wrong attach param: " +msgid+ " " +ex);
;;                        }
;;  return null;
;;  }


        ;; public String formatBody()
        ;; {


        ;; m = (this.msgid < MSGID_UNDER2)? bold.matcher(s): bold2.matcher(s);
        ;;         s = m.replaceAll( "<b>$1</b>" );

        ;; m = (this.msgid < MSGID_UNDER2)? under.matcher(s): under2.matcher(s);
        ;;         s = m.replaceAll( "<u>$1</u>" );

        ;;         m = url.matcher( s );
        ;;         // s = m.replaceAll( "<a href=\"$0\" target=\"_blank\">$0</a>" );

        ;;         StringBuffer sb = new StringBuffer( s.length() );
        ;;     while( m.find() )
        ;;     {
        ;;         String g = m.group( 0 );
        ;;         g = (g == null)? "": Matcher.quoteReplacement( g );
        ;;         m.appendReplacement( sb, "<a href=\""+g+"\" target=\"_blank\">"+
        ;;                 (( g.length() > URL_LENGTH_MAX )? g.substring( 0, URL_LENGTH_MAX ) + "..." : g) +"</a>" );
        ;;     }
        ;;     m.appendTail( sb );
        ;;     s = sb.toString();

        ;;         m = quote.matcher( s );
        ;;         s = m.replaceAll( "<span class=\"msgQuote\">$1</span>" );

        ;;         m = nl2br.matcher( s );
        ;;         s = m.replaceAll( "<br/>" );

        ;;         m = msgref.matcher( s );
        ;;         s = m.replaceAll( "<span class=\"msgRef ptr\" onclick=\"return forum_popmsg($0)\">$0</span>" );

        ;;         return s;
        ;; }
