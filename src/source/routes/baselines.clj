(ns source.routes.baselines
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all baselines"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:label :string]
                            [:min :int]
                            [:max :int]]]}}}
  [{:keys [ds] :as _request}]
  (->> (services/baselines ds)
       (res/response)))
