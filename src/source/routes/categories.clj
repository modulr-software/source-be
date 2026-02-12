(ns source.routes.categories
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.workers.categories :as categories]))

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

(defn used-categories
  {:summary "get all categories for which feeds and posts exist in the system"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}}}
  [{:keys [ds] :as _request}]
  (res/response (categories/used-categories ds)))
