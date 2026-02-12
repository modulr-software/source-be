(ns source.routes.bundle-feed-posts
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "get all posts in the outgoing feed for the associated uuid-authorized bundle by feed id"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
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
