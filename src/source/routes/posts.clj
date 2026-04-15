(ns source.routes.posts
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

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

  [{:keys [ds path-params] :as _request}]
  (-> (hon/find ds {:tname :incoming-posts
                    :where [:= :feed-id (:id path-params)]
                    :ret :*})
      (res/response)))
