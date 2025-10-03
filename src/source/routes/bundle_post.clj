(ns source.routes.bundle-post
  (:require [source.services.interface :as services]
            [source.db.util :as db.util]
            [ring.util.response :as res]))

(defn get
  {:summary "get a single outgoing post in the uuid-authorized bundle by post id"
   :parameters {:query [:map
                        [:uuid :string]
                        [:key :string]]
                :path [:map [:id {:title "id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map
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

  [{:keys [bundle-id path-params] :as _request}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        id (:id path-params)]
    (res/response (services/outgoing-post bundle-ds {:id id}))))

