
(ns photomap.db
  (:require
    [taoensso.timbre :refer [debug info warn]]
    [monger.collection :as mc]
    [monger.query :as mq]
    [mdb.core :refer [dbc try-warn]]))
;

(def PHOTOS "abot_photos")

(defn hist []
  (try-warn "hist:"
    (mq/with-collection (dbc) PHOTOS
      (mq/find {})
      (mq/sort {:ts -1}))))
;

;;.
