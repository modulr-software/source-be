(ns source.routes.businesses
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]))

(defn get
  {:summary "get all businesses"
   :responses {200 {:body [:map
                           [:businesses
                            [:map
                             [:id :int]
                             [:name :string]
                             [:url [:maybe :string]]
                             [:linkedin [:maybe :string]]
                             [:twitter [:maybe :string]]]]]}}}

  [{:keys [ds] :as _request}]
  (res/response {:businesses (businesses/businesses ds)}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
