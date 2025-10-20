(ns source.routes.categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.db.util :as db.util]))

(defn get
  {:summary "get all categories"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}}}
  [{:keys [ds] :as _request}]
  (->> (services/categories ds)
       (res/response)))
