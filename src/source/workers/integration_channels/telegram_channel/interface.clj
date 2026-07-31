(ns source.workers.integration-channels.telegram-channel.interface
  (:require [source.workers.integration-channels.interface :as protocol]
            [source.workers.telegram-client :as tg]))

(defrecord TelegramChannel [token chat-id]
  protocol/IntegrationChannel
  (send-posts! [this {:keys [posts chat-id] :as _opts}]
    (tg/send-posts! {:posts   posts
                     :chat-id (or chat-id (:chat-id this))})))

(defn create-client
  [{:keys [valid token chat-id] :as _opts}]
  (when-not valid
    (try
      (tg/get-chat (tg/create-client (cond-> {}
                                     token    (assoc :token token)
                                     chat-id  (assoc :chat-id chat-id)))
                   {:chat-id chat-id})
      (catch Exception _
        (throw (ex-info "telegram chat-id is not valid or bot is not a member"
                        {:chat-id chat-id})))))
  (TelegramChannel. token chat-id))
