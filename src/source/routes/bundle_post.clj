(ns source.routes.bundle-post
  (:require [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.db.honey :as hon]
            [source.db.util :as db.util]
            [honey.sql.helpers :as hsql]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [malli.util :as mu]))

(defn get
  {:summary "Get a single post by post id in the uuid-authorized bundle.
   Used to return a single post present in the bundle.
   This endpoint updates click analytics for the returned post."
   :description "This endpoint will pull a single post by ID that made it into the bundle during post selection."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]
                :path [:map [:id {:title "id"
                                  :description "Post ID"} :int]]}
   :responses (-> (api/success (-> schemas/Post
                                   (mu/assoc :feed-title :string)))
                  (api/not-found))}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [post (hon/execute! ds (-> (hsql/select-distinct :p.* [:f.title :feed-title])
                                  (hsql/from [(:tname (db.util/tname :outgoing-posts bundle-id)) :p])
                                  (hsql/join [:feeds :f] [:= :p.feed-id :f.id])
                                  (hsql/where [:= :p.id (:id path-params)]))
                           {:ret :1})]
    (try
      (analytics/insert-post-click! ds post bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response post)))
