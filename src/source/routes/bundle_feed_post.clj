(ns source.routes.bundle-feed-post
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "get post in outgoing feed for the associated uuid-authorized bundle by post id, updates click analytics"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:post-id {:title "post-id"
                                       :description "post id"} :int]]}
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
  (let [post (hon/find-one ds {:tname :incoming-posts
                               :where [:= :id (:id path-params)]})]
    (analytics/insert-post-click! ds post bundle-id)
    (res/response post)))
