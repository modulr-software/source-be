(ns source.routes.content-type
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get content type by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "content type id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/content-type ds)
       (res/response)))
