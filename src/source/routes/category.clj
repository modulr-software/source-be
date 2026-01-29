(ns source.routes.category
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get category by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "category id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (res/response (hon/find-one ds {:tname :categories
                                  :where [:= :id (:id path-params)]})))
