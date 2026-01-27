(ns source.routes.bundle-feed-posts
  (:require [ring.util.response :as res]
            [source.workers.bundles :as bundles]))

(defn get
  {:summary "get all posts by feed id"
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
  (->> {:bundle-id bundle-id
        :feed-id (:id path-params)}
       (bundles/get-posts-by-feed-in-bundle! ds)
       (res/response)))
