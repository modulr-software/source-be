(ns source.routes.slack
  (:require [source.oauth2.slack.interface :as slack]
            [ring.util.response :as res]
            [source.config :as conf]))

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
                        [:state :string]]}
   :responses {200 {:body [:map [:message :string]]}}}
  [{:keys [query-params] :as req}]
  (let [{:keys [state]} query-params
        result (slack/slack-integration-details state (:params req))]
    (res/response {:message (str result)})))
