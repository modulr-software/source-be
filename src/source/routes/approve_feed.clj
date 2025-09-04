(ns source.routes.approve-feed
  (:require [source.services.interface :as services]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]
            [ring.util.response :as res]))

(defn post
  {:summary "approve the feed with the given feed-id and allow it to go live"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (let [{:keys [id user-id title]} (services/feed ds path-params)
        {:keys [email firstname]} (services/user ds {:id user-id})]
    (services/update-feed! ds {:id (:id path-params)
                               :data {:state "live"}})
    (gmail/send-email {:to email
                       :subject "Feed Approval"
                       :body (templates/feed-approval {:creator-name firstname
                                                       :feed-title title
                                                       :feed-id id})
                       :type :text/html})
    (res/response {:message "successfully approved feed"})))
