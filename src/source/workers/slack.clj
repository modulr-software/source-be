(ns source.workers.slack
  (:require [clojure.data.json :as json]
            [source.workers.bundles :as bundles]
            [org.httpkit.client :as http]
            [source.config :as conf]))

(defn send-slack-message! [channel-id message]
  @(http/request {:url "https://slack.com/api/chat.postMessage"
                  :method :post
                  :headers {"Authorization" (str "Bearer " (conf/read-value :slack :token))
                            "Content-Type" "application/json; charset=utf-8"}
                  :body (json/write-str {:channel channel-id
                                         :text message})}))

(defn slack-post! [ds bundle-id channel-id]
  (let [post (-> (bundles/get-outgoing-posts
                   ds
                   {:bundle-id bundle-id
                    :start 0
                    :limit 1
                    :truncate true})
                 (:data)
                 (first))
        message (cond
                  (= (:content-type-id post) 1)
                  (str (:feed-title post) "\n" (:stream-url post))
                  (= (:content-type-id post) 2)
                  (str (:feed-title post) "\n" (:info post) "\n" (:thumbnail post))
                  (= (:content-type-id post) 3)
                  (str (:feed-title post) "\n" (:info post) "\n" (:thumbnail post)))]
    (send-slack-message! channel-id message)))
