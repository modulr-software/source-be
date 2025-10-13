(ns source.routes.bundle-posts
  (:require [source.services.interface :as services]
            [source.db.util :as db.util]
            [clojure.walk :as walk]
            [ring.util.response :as res]
            [clojure.set :as set]))

(defn post
  {:summary "get all outgoing posts in the uuid-authorized bundle"
   :parameters {:body [:map [:category-ids [:vector :int]]]
                :query [:map
                        [:uuid :string]
                        [:limit {:optional true} :int]
                        [:start {:optional true} :int]
                        [:type {:optional true} :int]]}
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
               404 {:boy [:map [:message :string]]}}}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        {:keys [limit start type]} (walk/keywordize-keys query-params)
        {:keys [category-ids]} body

        content-type-comp (when type [:= :content-type-id type])
        start (when start (try (Integer/parseInt start) (catch Exception _)))
        limit (when limit (try (Integer/parseInt limit) (catch Exception _)))

        filtered-posts (shuffle (services/outgoing-posts bundle-ds {:where content-type-comp}))

        categorised-posts (vec (if (seq category-ids)
                                 (->> filtered-posts
                                      (mapv
                                       (fn [post]
                                         (when (seq (set/intersection
                                                     (set category-ids)
                                                     (->> {:feed-id (:feed-id post)}
                                                          (services/categories-by-feed ds)
                                                          (mapv :id)
                                                          (set))))
                                           post)))
                                      (remove nil?))
                                 filtered-posts))

        valid-start? (and (some? start) (>= start 0) (< start (count categorised-posts)))
        started-posts (if valid-start?
                        (subvec categorised-posts start)
                        categorised-posts)

        limited-posts (if (and (some? limit) (> (count started-posts) limit))
                        (subvec started-posts 0 limit)
                        started-posts)]

    (res/response limited-posts)))
