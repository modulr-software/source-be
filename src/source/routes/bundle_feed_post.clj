(ns source.routes.bundle-feed-post
  (:require [ring.util.response :as res]
            [source.workers.bundles :as bundles]))

(defn get
  {:summary "get post by post id"
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
  (->> {:bundle-id bundle-id
        :post-id (:id path-params)}
       (bundles/get-post-by-feed-in-bundle! ds)
       (res/response)))
