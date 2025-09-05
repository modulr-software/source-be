(ns source.routes.cadences
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all cadences"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:label :string]
                            [:days :int]]]}}}
  [{:keys [ds] :as _request}]
  (->> (services/cadences ds)
       (res/response)))
