(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.db.util :as db.util]
            [source.workers.bundles :as bundles]
            [source.services.analytics.interface :as analytics]))

(defn post
  {:summary "Get all RSS feeds present in the bundle authorised by uuid.
   This endpoint will update impressions analytics for the returned RSS feeds."
   :parameters {:query [:map
                        [:uuid {:description "Bundle UUID"} :string]
                        [:type {:optional true
                                :description "Filters by content type ID"} :int]
                        [:latest {:optional true
                                  :description "Filters by most recently uploaded feeds"} :boolean]
                        [:nonfiltered {:optional true
                                       :description "Marking this field as true will disable all filters"} :boolean]]
                :body [:map [:category-ids [:vector :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:title :string]
                            [:display-picture [:maybe :string]]
                            [:url [:maybe :string]]
                            [:rss-url :string]
                            [:user-id :int]
                            [:provider-id [:maybe :int]]
                            [:created-at :string]
                            [:updated-at [:maybe :string]]
                            [:content-type-id :int]
                            [:cadence-id :int]
                            [:baseline-id :int]
                            [:ts-and-cs [:maybe :int]]
                            [:state [:enum "live" "not live" "pending"]]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [type latest nonfiltered]} query-params
        feeds (->> {:bundle-id bundle-id
                    :type type
                    :latest latest
                    :category-ids (:category-ids body)
                    :nonfiltered nonfiltered}
                   (bundles/get-outgoing-feeds ds))]
    (try
      (analytics/insert-feed-impressions! ds feeds bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response feeds)))

(comment
  (def ds (db.util/conn :master))

  ())
