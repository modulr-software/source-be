(ns source.oauth2.slack.core
  (:require [source.cache :as cache]
            [clj-oauth2.client :as oauth2]
            [source.config :as conf]
            [clojure.data.json :as json]
            [org.httpkit.client :as client]))

(defn -auth-uri [auth-reqs-service]
  (let [req (cache/add-item auth-reqs-service
                            (oauth2/make-auth-request
                             (conf/read-value :oauth2 :slack)))]
    {:uri (get-in req [:item :uri])
     :uuid (:uuid req)}))

(defn -slack-integration-details [params]
  (let [response @(client/request
                   {:method :post
                    :url "https://slack.com/api/oauth.v2.access"
                    :form-params {:code (:code params)
                                  :client_id (conf/read-value :oauth2 :slack :client-id)
                                  :client_secret (conf/read-value :oauth2 :slack :client-secret)
                                  :redirect_uri (conf/read-value :oauth2 :slack :redirect-uri)}})
        body (json/read-str (:body response) {:key-fn keyword})
        channel-id (get-in body [:incoming_webhook :channel_id])
        access-token (get-in body [:access_token])]
    (println response)
    (println body)
    {:channel-id channel-id
     :access-token access-token}))
