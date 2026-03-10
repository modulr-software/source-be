(ns source.routes.bundle-feed-post
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]
            [source.db.util :as db.util]
            [honey.sql.helpers :as hsql]
            [taoensso.telemere :as t]))

(defn get
  {:summary "Get a single post by post id belonging to an RSS feed in the associated uuid-authorized bundle. 
   This endpoint updates click analytics for the returned post."
   :description "This endpoint will fetch a single post belonging to the given feed, regardless of whether the post made it into the bundle during post selection."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]
                :path [:map [:post-id {:title "postId"
                                       :description "Post ID"} :int]]}
   :responses {200 {:body
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
                     [:posted-at [:maybe :string]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [post (hon/find-one ds (-> (db.util/tname :outgoing-posts bundle-id)
                                  (hsql/where [:= :id (:post-id path-params)])))]
    (try
      (analytics/insert-post-click! ds post bundle-id)
      (catch Exception e (t/log! {:level :error
                                  :msg (str "Failed to insert post click on bundle feed post: " (.getMessage e))})))
    (res/response post)))
