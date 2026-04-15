(ns source.routes.baselines
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all baselines"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:label :string]
                            [:min :int]
                            [:max :int]]]}}}
  [{:keys [ds] :as _request}]
  (->> (hon/find ds {:tname :baselines})
       (res/response)))
