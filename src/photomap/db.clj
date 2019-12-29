
(ns photomap.db
  (:require
    [monger.query :as mq]
    [mlib.logger :refer [warn]]
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
