(ns source.routes.categories
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all categories"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}}}
  [{:keys [ds] :as _request}]
  (->> (hon/find ds {:tname :categories})
       (res/response)))
