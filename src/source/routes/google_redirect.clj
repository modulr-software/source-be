(ns source.routes.google-redirect
  (:require [ring.util.response :as res]
            [source.config :as conf]))

(defn get [{:keys [query-string] :as req}]
  (res/redirect (str (conf/read-value :cors-origin) "/oauth?" query-string)))

