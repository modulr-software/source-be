(ns source.routes.integration-filter-feed
  (:require [ring.util.response :as res]
            [source.workers.integrations :as integrations]
            [source.db.honey :as hon]))

(defn get
  {:summary "Returns true if the feed with the given id is filtered out by the given integration by ID"
   :parameters {:path [:map
                       [:id {:title "id"
                             :description "Integration ID"} :int]
                       [:feed-id {:title "FeedId"
                                  :description "feed ID"} :int]]}
   :responses {200 {:body [:map [:filtered :boolean]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]

  (let [returned (hon/find ds {:tname :filtered-feeds
                               :where [:and
                                       [:= :bundle-id (:id path-params)]
                                       [:= :feed-id (:feed-id path-params)]]})
        blocked (if (seq returned) true false)]
    (res/response {:filtered blocked})))

(defn post
  {:summary "Filters out the feed with the given id from the given integration by ID"
   :description "Filtering a feed out from the integration means that the given feed and all its posts will not appear when pulling content from the bundle."
   :parameters {:path [:map
                       [:id {:title "id"
                             :description "Integration ID"} :int]
                       [:feed-id {:title "feedId"
                                  :description "Feed ID"} :int]]
                :body [:map
                       [:filtered :boolean]]}
   :responses {:body {200 [:map [:message :string]]
                      401 [:map [:message :string]]
                      403 [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (->> {:filtered (:filtered body)
        :bundle-id (:id path-params)
        :feed-id (:feed-id path-params)}
       (integrations/update-filtered-feeds! ds))
  (res/response {:message "Successfully updated feed filtering."}))
