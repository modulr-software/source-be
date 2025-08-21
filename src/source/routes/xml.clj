(ns source.routes.xml
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [body] :as _request}]
  (let [{:keys [url]} body]
    (-> (services/ast url)
        (res/response))))
