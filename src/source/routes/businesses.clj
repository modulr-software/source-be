(ns source.routes.businesses
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (res/response {:businesses (businesses/businesses ds)}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
