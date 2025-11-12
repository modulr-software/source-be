(ns source.routes.post
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

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
  (-> (services/incoming-post ds {:where [:and
                                          [:= :id (:post-id path-params)]
                                          [:= :creator-id (:id user)]]})
      (res/response)))
