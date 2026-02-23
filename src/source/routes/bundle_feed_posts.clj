(ns source.routes.bundle-feed-posts
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "Get all posts present within a given RSS feed by feed id, within the uuid-authorized bundle.
   This endpoint will update impressions analytics for the returned posts."
   :description "This endpoint will fetch all posts within the given feed, regardless of whether these posts made it into this bundle during post selection."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]
                :path [:map [:id {:title "id"
                                  :description "Feed ID"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:post-id :string]
                            [:feed-id :int]
                            [:creator-id :int]
                            [:content-type-id :int]
                            [:title :string]
                            [:thumbnail [:maybe :string]]
                            [:info [:maybe :string]]
                            [:url [:maybe :string]]
                            [:stream-url [:maybe :string]]
                            [:season [:maybe :int]]
                            [:episode [:maybe :int]]
                            [:redacted {:optional true} [:maybe :int]]
                            [:posted-at [:maybe :string]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [posts (hon/find ds {:tname :incoming-posts
                            :where [:= :feed-id (:id path-params)]
                            :ret :*})]
    (try
      (analytics/insert-post-impressions! ds posts bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response posts)))
