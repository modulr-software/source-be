(ns source.oauth2.slack.interface
  (:require [source.oauth2.slack.core :as slack]
            [source.cache :as cache]))

(def ^:private auth-reqs-service (cache/create-cache))

(defn auth-uri []
  (slack/-auth-uri auth-reqs-service))

(defn slack-integration-details [uuid params]
  (slack/-slack-integration-details auth-reqs-service uuid params))
