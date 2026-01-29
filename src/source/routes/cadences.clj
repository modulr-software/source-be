(ns source.routes.cadences
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all cadences"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:label :string]
                            [:days :int]]]}}}
  [{:keys [ds] :as _request}]
  (->> (hon/find ds {:tname :cadences})
       (res/response)))
