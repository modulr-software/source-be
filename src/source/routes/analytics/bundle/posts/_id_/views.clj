(ns source.routes.analytics.bundle.posts.-id-.views
  (:require [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.services.interface :as services]))

(defn post
  {:summary "Explicitly insert a view event for the post with the given id for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [post (services/incoming-post ds {:id (:id path-params)})]
    (analytics/insert-post-view! ds post bundle-id)
    (res/response {:message "Successfully inserted view event"})))
