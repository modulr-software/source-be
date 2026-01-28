(ns source.routes.integration-filter-post
  (:require [ring.util.response :as res]
            [source.workers.integrations :as integrations]
            [source.db.honey :as hon]))

(defn get
  {:summary "Returns true if the post with the given id is filtered out by the integration with the given id"
   :parameters {:path [:map
                       [:id {:title "id"
                             :description "integration id"} :int]
                       [:post-id {:title "post-id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map [:filtered :boolean]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (let [returned (hon/find ds {:tname :filtered-feeds
                               :where [:and
                                       [:= :bundle-id (:id path-params)]
                                       [:= :post-id (:post-id path-params)]]})
        blocked (if (seq returned) true false)]
    (res/response {:filtered blocked})))

(defn post
  {:summary "filters out the post with the given id from the bundle with the given bundle id"
   :parameters {:path [:map
                       [:id {:title "id"
                             :description "bundle id"} :int]
                       [:post-id {:title "post-id"
                                  :description "post id"} :int]]
                :body [:map
                       [:filtered :boolean]]}
   :responses {:body {200 [:map [:message :string]]
                      401 [:map [:message :string]]
                      403 [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (->> {:filtered (:filtered body)
        :bundle-id (:id path-params)
        :post-id (:post-id path-params)}
       (integrations/update-filtered-posts! ds))
  (res/response {:message "successfully updated post filtering"}))
