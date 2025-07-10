(ns source.routes.businesses
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (res/response {:businesses (businesses/businesses ds)}))

(def get-parameters {:body [:map
                            [:name :string]
                            [:url {:optional true} :string]
                            [:linkedin {:optional true} :string]
                            [:twitter {:optional true} :string]]})

(def get-responses {200 {:body [:map
                                [:businesses
                                 [:map
                                  [:id :int]
                                  [:name :string]
                                  [:url {:optional true} :string]
                                  [:linkedin {:optional true} :string]
                                  [:twitter {:optional true} :string]]]]}})

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
