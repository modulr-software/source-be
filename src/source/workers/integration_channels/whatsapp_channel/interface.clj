(ns source.workers.integration-channels.whatsapp-channel.interface
  (:require [source.workers.integration-channels.interface :as intchan]
            [source.workers.whatsapp :as wa]))

(defn create-client
  [{:keys [valid _token chat-id phone-number] :as opts}]
  (when-not (or valid
                (wa/group-participant? chat-id phone-number))
    (throw (ex-info "phone number is not a member of the WhatsApp group"
                    {:chat-id chat-id :phone-number phone-number})))
  (let [config (select-keys opts [:token :chat-id :phone-number])]
    (reify intchan/IntegrationChannel
      (send-posts! [_ {:keys [posts chat-id]}]
        (wa/send-posts! {:posts    posts
                         :group-id (or chat-id (:chat-id config))})))))
