(ns source.routes.integration-filter-feeds
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "gets all filtered feed ids by integration id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:feed-id :int]
                            [:bundle-id :int]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (res/response (services/filtered-feeds ds {:where [:= :bundle-id (:id path-params)]})))
