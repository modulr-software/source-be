(ns source.routes.sectors
  (:require [source.services.sectors :as sectors]
            [ring.util.response :as res]))

(defn get
  {:summary "get all sectors"
   :responses {200 {:body [:map
                           [:sectors
                            [:map
                             [:id :int]
                             [:name :string]]]]}}}

  [{:keys [ds] :as _request}]
  (res/response {:sectors (sectors/sectors ds)}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
