(ns source.workers.integration-channels.interface)

(defprotocol IntegrationChannel
  "Base protocol for an integration channel client.

  `send-posts!` dispatches on the record type to deliver a batch of posts
  to the platform."

  (view-config [this]
    "Returns the internal state of this integration channel")
  (handshake [this]
    "Checks if the integration channel is valid, if it is currently marked as invalid. 
    Throws if found invalid, does nothing if already valid.")
  (send-posts! [this posts]
    "Send a batch of posts to this integration channel."))
