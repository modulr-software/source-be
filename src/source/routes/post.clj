(ns source.routes.post
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get post by id"
   :parameters {:path [:map [:post-id {:title "post-id"
                                       :description "post id"} :int]]}
   :responses  {200 {:body [:map
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
                            [:posted-at [:maybe :string]]]}}}

  [{:keys [ds user path-params] :as _request}]
  (-> (hon/find-one ds {:tname :incoming-posts
                        :where [:and
                                [:= :id (:post-id path-params)]
                                [:= :creator-id (:id user)]]})
      (res/response)))
