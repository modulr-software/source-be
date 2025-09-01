(ns source.routes.posts
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all posts by feed id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:post-id :string]
                            [:feed-id :int]
                            [:creator-id :int]
                            [:content-type-id :int]
                            [:title :string]
                            [:info [:maybe :string]]
                            [:url [:maybe :string]]
                            [:stream-url [:maybe :string]]
                            [:season [:maybe :int]]
                            [:episode [:maybe :int]]
                            [:redacted [:maybe :int]]
                            [:posted-at [:maybe :string]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (-> (services/incoming-posts ds {:where [:= :feed-id (:id path-params)]})
      (res/response)))
