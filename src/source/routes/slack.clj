(ns source.routes.slack
  (:require [source.oauth2.slack.interface :as slack]
            [ring.util.response :as res]
            [source.config :as conf]
            [source.workers.integration-channels :as channels]))

(defn launch
  {:summary "begins slack oauth flow"
   :responses {200 {:body [:map
                           [:uuid :string]
                           [:uri :string]]}}}
  [_req]
  (res/response (slack/auth-uri)))

(defn redirect
  [{:keys [query-string] :as _req}]
  (-> (conf/read-value :cors-origin)
      (str "/oauth/slack?" query-string)
      (res/redirect)))

(defn complete
  {:summary "completes the slack oauth flow"
   :parameters {:query [:map
                        [:code :string]
                        [:scope :string]
                        [:state :int]]}
   :responses {200 {:body [:map [:message :string]]}}}
  [{:keys [ds js query-params] :as req}]
  (let [{:keys [state]} query-params
        {:keys [access-token channel-id]} (slack/slack-integration-details (:params req))]
    (channels/create-channel! ds js {:platform "slack"
                                     :bundle-id state
                                     :channel-id channel-id
                                     :access-token access-token
                                     :post-interval (* 1000 60 60 24)})
    (res/response {:message "successfully added channel"})))
