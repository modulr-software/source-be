(ns source.routes.bundle-posts
  (:require [source.services.interface :as services]
            [source.db.util :as db.util]
            [clojure.walk :as walk]
            [ring.util.response :as res]
            [clojure.set :as set]
            [honey.sql.helpers :as hsql]
            [source.services.analytics.interface :as analytics]
            [source.workers.bundles :as bundles]))

(defn post
  {:summary "get all outgoing posts in the uuid-authorized bundle"
   :parameters {:body [:map [:category-ids [:vector :int]]]
                :query [:map
                        [:uuid :string]
                        [:limit {:optional true} :int]
                        [:start {:optional true} :int]
                        [:type {:optional true} :int]
                        [:latest {:optional true} [:enum "true" "false"]]]}
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
  (let [{:keys [limit start type latest]} (walk/keywordize-keys query-params)]
    (->> {:bundle-id bundle-id
          :limit limit
          :start start
          :type type
          :latest latest
          :category-ids (:category-ids body)}
         (bundles/get-outgoing-posts! ds)
         (res/response))))
