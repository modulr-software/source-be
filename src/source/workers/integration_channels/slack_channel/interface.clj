(ns source.workers.integration-channels.slack-channel.interface
  (:require [source.workers.integration-channels.interface :as protocol]
            [source.workers.slack-client :as slack]))

(defrecord SlackChannel [token chat-id]
  protocol/IntegrationChannel
  (send-posts! [this {:keys [posts chat-id token] :as _opts}]
    (slack/send-posts! {:posts      posts
                        :channel-id (or chat-id (:chat-id this))
                        :token      (or token (:token this))})))

(defn create-client
  [{:keys [token chat-id] :as _opts}]
  (SlackChannel. token chat-id))
