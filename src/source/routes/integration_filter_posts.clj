(ns source.routes.integration-filter-posts
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "Gets all filtered post ids for the given integration by ID"
   :description "If a post appears in this list, it will not be returned when pulling content from the bundle."
   :parameters {:path [:map [:id {:title "id"
                                  :description "Integration ID"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:post-id :int]
                            [:bundle-id :int]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (res/response (hon/find ds {:tname :filtered-posts
                              :where [:= :bundle-id (:id path-params)]})))
