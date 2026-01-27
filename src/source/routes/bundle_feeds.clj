(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.db.util :as db.util]
            [clojure.walk :as walk]
            [source.workers.bundles :as bundles]))

(defn post
  {:summary "get all feeds present in the bundle authorised by uuid"
   :parameters {:query [:map
                        [:uuid :string]
                        [:type {:optional true} :int]
                        [:latest {:optional true} :boolean]
                        [:nonfiltered {:optional true} :boolean]]
                :body [:map [:category-ids [:vector :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:title :string]
                            [:display-picture [:maybe :string]]
                            [:url [:maybe :string]]
                            [:rss-url :string]
                            [:user-id :int]
                            [:provider-id [:maybe :int]]
                            [:created-at :string]
                            [:updated-at [:maybe :string]]
                            [:content-type-id :int]
                            [:cadence-id :int]
                            [:baseline-id :int]
                            [:ts-and-cs [:maybe :int]]
                            [:state [:enum "live" "not live" "pending"]]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [type latest nonfiltered]} (walk/keywordize-keys query-params)]
    (->> {:bundle-id bundle-id
          :type type
          :latest latest
          :category-ids (:category-ids body)
          :nonfiltered nonfiltered}
         (bundles/get-feeds-in-bundle! ds)
         (res/response))))

(comment
  (def ds (db.util/conn :master))

  ())
