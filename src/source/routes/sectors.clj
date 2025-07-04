(ns source.routes.sectors
  (:require [source.services.sectors :as sectors]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (res/response {:sectors (sectors/sectors ds)}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
