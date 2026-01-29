(ns source.routes.post-prune
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn post
  {:summary "Update redacted status of post with the given id"
   :parameters {:path [:map [:post-id {:title "post-id"
                                       :description "post id"} :int]]
                :body [:map
                       [:redacted :boolean]]}
   :responses  {200 {:body [:map [:message :string]]}
                401 {:body [:map [:message :string]]}
                403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params user body] :as _request}]
  (hon/update! ds {:tname :incoming-posts
                   :where [:and
                           [:= :id (:post-id path-params)]
                           [:= :creator-id (:id user)]]
                   :data {:redacted (if (:redacted body) 1 0)}})
  (res/response {:message "successfully updated post"}))
