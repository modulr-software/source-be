(ns source.routes.bundle-posts
  (:require [source.services.interface :as services]
            [source.db.util :as db.util]
            [clojure.walk :as walk]
            [ring.util.response :as res]))

(defn get
  {:summary "get all outgoing posts in the uuid-authorized bundle"
   :parameters {:query [:map
                        [:uuid :string]
                        [:limit {:optional true} :int]
                        [:start {:optional true} :int]]}
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

  [{:keys [bundle-id query-params] :as _request}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        {:keys [limit start]} (walk/keywordize-keys query-params)]
    (res/response (services/outgoing-posts bundle-ds {:where (when start [:>= :id start])
                                                      :limit limit
                                                      :order-by [[:id :asc]]}))))
