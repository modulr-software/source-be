(ns source.logger
  (:require
   [source.util :as util]))

(defn log [message]
  (println (str "SOURCE [" (util/get-utc-timestamp-string) "] LOG: " message)))

(defn log-warning [message]
  (println (str "SOURCE [" (util/get-utc-timestamp-string) "] WARNING: " message)))

(defn log-error [message]
  (println (str "SOURCE [" (util/get-utc-timestamp-string) "] ERROR: " message)))
