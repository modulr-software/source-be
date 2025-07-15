(ns source.routes.google-redirect
  (:require [ring.util.response :as res]
            [source.config :as conf]))

(defn get [{:keys [query-string] :as req}]
  (-> (conf/read-value :cors-origin)
      (str "/oauth?" query-string)
      (res/redirect)))
