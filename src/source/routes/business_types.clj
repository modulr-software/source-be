(ns source.routes.business-types
  (:require [source.db.honey :as db]
            [ring.util.response :as res]))

(defn get
  {:summary "Get all business types"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}
  [ds]
  (res/response (db/find ds {:tname :business-types
                             :ret :*})))
