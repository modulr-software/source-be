(ns source.routes.reject-feed
  (:require [source.services.interface :as services]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]
            [ring.util.response :as res]))

(defn patch
  {:summary "reject the feed with the given feed-id and prevent it from going live"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]
                :body [:map [:message :string]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (let [{:keys [user-id title]} (services/feed ds path-params)
        {:keys [email firstname]} (services/user ds {:id user-id})]
    (services/update-feed! ds {:id (:id path-params)
                               :data {:state "not live"}})
    (gmail/send-email {:to email
                       :subject "Feed Rejection"
                       :body (templates/feed-rejection {:creator-name firstname
                                                        :feed-title title
                                                        :reason (:message body)})
                       :type :text/html})
    (res/response {:message "successfully rejected feed"})))
