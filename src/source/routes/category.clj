(ns source.routes.category
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get
  {:summary "get category by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "category id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (res/response (services/category ds path-params)))
