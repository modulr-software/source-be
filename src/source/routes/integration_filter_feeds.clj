(ns source.routes.integration-filter-feeds
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "Gets all filtered feed IDs by integration ID"
   :description "Returns a list of all feed IDs that have been filtered out from the given integration. If a feed appears in this list, it will not be returned when pulling content from the bundle."
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:feed-id :int]
                            [:bundle-id :int]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (res/response (hon/find ds {:tname :filtered-feeds
                              :where [:= :bundle-id (:id path-params)]})))
