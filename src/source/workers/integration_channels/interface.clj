(ns source.workers.integration-channels.interface)

(defprotocol IntegrationChannel
  "Base protocol for an integration channel client.

  `send-posts!` dispatches on the record type to deliver a batch of posts
  to the platform."

  (send-posts! [this opts]
    "Send a batch of posts to this integration channel."))

(comment
  (require '[source.workers.integration-channels.whatsapp-channel.interface :as wa-chan])
  (require '[source.workers.integration-channels.interface :as intchan])
  (require '[source.workers.whatsapp :as wa])
  (require '[source.workers.bundles :as bundles])
  (require '[source.util :as util])
  (require '[source.db.util :as db.util])

  (def ds (db.util/conn))
  (def bundle-id 26)
  (def group-id (wa/group-id-by-name "thisisatest"))

  ;; valid — phone-number is a member of the group
  (def channel (wa-chan/create-client {:chat-id      group-id
                                       :phone-number "27607205781"
                                       :valid        false}))

  ;; invalid — throws "phone number is not a member of the WhatsApp group"
  (wa-chan/create-client {:chat-id      group-id
                          :phone-number "00000000000"
                          :valid        false})

  ;; send a batch of posts through the channel — :chat-id overrides the instance's
  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed     (util/get-utc-timestamp-string)
                                        :limit    3}))
                        :chat-id group-id})

  ;; same call, omitting :chat-id — falls back to the chat-id captured at create-client
  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed     (util/get-utc-timestamp-string)
                                        :limit    3}))})
  ())
