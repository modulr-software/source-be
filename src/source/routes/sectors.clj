(ns source.routes.sectors
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all sectors"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds] :as _request}]
  (res/response (hon/find ds {:tname :sectors
                              :ret :*})))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
