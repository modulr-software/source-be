(ns source.routes.bundle
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get bundle metadata by authorized uuid"
   :parameters {:query [:map [:uuid :string]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:uuid :string]
                           [:user-id :int]
                           [:video :int]
                           [:podcast :int]
                           [:blog :int]
                           [:hash [:maybe :string]]
                           [:content-type-id :int]
                           [:ts-and-cs [:maybe :int]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id] :as _request}]
  (res/response (hon/find-one ds {:tname :bundles
                                  :where [:= :id bundle-id]})))
