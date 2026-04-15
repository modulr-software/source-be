(ns source.routes.businesses
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all businesses"
   :responses {200 {:body [:map
                           [:businesses
                            [:map
                             [:id :int]
                             [:name :string]
                             [:address [:maybe :string]]
                             [:url [:maybe :string]]
                             [:linkedin [:maybe :string]]
                             [:twitter [:maybe :string]]]]]}}}

  [{:keys [ds] :as _request}]
  (res/response {:businesses (hon/find ds {:tname :businesses
                                           :ret :*})}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
