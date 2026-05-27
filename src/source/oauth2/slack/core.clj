(ns source.oauth2.slack.core
  (:require [source.cache :as cache]
            [clj-oauth2.client :as oauth2]
            [source.config :as conf]
            [clojure.data.json :as json]))

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

(defn -slack-integration-details [auth-reqs-service uuid params]
  (let [result (-> (conf/read-value :oauth2 :slack)
                   (oauth2/get-access-token
                    params
                    (cache/get-item auth-reqs-service uuid)))]
    (cache/remove-item auth-reqs-service uuid)
    #_(slack-channel-id (:access-token result))
    result))
