(ns source.routes.content-type
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get content type by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "content type id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (->> (hon/find-one ds {:tname :content-types
                         :where [:= :id (:id path-params)]})
       (res/response)))
