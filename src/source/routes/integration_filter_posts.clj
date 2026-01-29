(ns source.routes.integration-filter-posts
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "gets all filtered post ids by integration id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:post-id :int]
                            [:bundle-id :int]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (res/response (hon/find ds {:tname :filtered-posts
                              :where [:= :bundle-id (:id path-params)]})))
