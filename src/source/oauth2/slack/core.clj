(ns source.oauth2.slack.core
  (:require [source.cache :as cache]
            [clj-oauth2.client :as oauth2]
            [source.config :as conf]
            [clojure.data.json :as json]
            [org.httpkit.client :as client]))

(defn auth-request [access-token]
  {:headers {"Authorization" (str "Bearer " access-token)}})

(defn integration-info [auth-request]
  (let [body (->
              (oauth2/get
               "https://slack.com/api/oauth.v2.access"
               auth-request)
              (:body))]
    (println body)
    (if body
      (get-in (json/read-json body {:key-fn keyword}) [:incoming_webhook :channel_id])
      nil)))

(defn slack-channel-id [access-token]
  (-> access-token
      (auth-request)
      (integration-info)))

(defn -auth-uri [auth-reqs-service]
  (let [req (cache/add-item auth-reqs-service
                            (oauth2/make-auth-request
                             (conf/read-value :oauth2 :slack)))]
    {:uri (get-in req [:item :uri])
     :uuid (:uuid req)}))

#_(defn -slack-integration-details [auth-reqs-service uuid params]
    (let [result (-> (conf/read-value :oauth2 :slack)
                     (oauth2/get-access-token
                      params
                      (cache/get-item auth-reqs-service uuid)))]
      (cache/remove-item auth-reqs-service uuid)
      {:access-token (:access-token result)
       :channel-id (slack-channel-id (:access-token result))}))

(defn -slack-integration-details [auth-reqs-service uuid params]
  (let [response @(client/request
                   {:method :post
                    :url "https://slack.com/api/oauth.v2.access"
                    :form-params {:code (:code params)
                                  :client_id (conf/read-value :oauth2 :slack :client-id)
                                  :client_secret (conf/read-value :oauth2 :slack :client-secret)
                                  :redirect_uri (conf/read-value :oauth2 :slack :redirect-uri)}
                    :as :json})
        body (:body response)
        channel-id (get-in body [:incoming_webhook :channel_id])
        access-token (get-in body [:access_token])]
    {:channel-id channel-id
     :access-token access-token}))
