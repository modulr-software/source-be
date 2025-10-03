(ns source.routes.bundle
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get
  {:summary "get bundle metadata by authorized uuid"
   :parameters {:query [:map 
                        [:uuid :string]
                        [:key :string]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:uuid :string]
                           [:user-id :int]
                           [:video :int]
                           [:podcast :int]
                           [:blog :int]
                           [:hash {:optional true} [:maybe :string]]
                           [:content-type-id :int]
                           [:ts-and-cs [:maybe :int]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id] :as _request}]
  (res/response (services/bundle ds {:id bundle-id})))
