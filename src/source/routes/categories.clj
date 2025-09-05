(ns source.routes.categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all categories"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}
  [{:keys [ds] :as _request}]
  (->> (services/categories ds)
       (res/response)))
