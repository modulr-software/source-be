(ns source.routes.google-redirect
  (:require [ring.util.response :as res]
            [source.config :as conf]))

(defn get [{:keys [query-string] :as _req}]
  (-> :cors-origin
      (conf/read-value)
      (str "/oauth?" query-string)
      (res/redirect)))
