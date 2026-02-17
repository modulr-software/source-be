(ns source.routes.xml
  (:require [source.workers.xml-schemas :as xml]
            [ring.util.response :as res]))

(defn post [{:keys [body] :as _request}]
  (let [{:keys [url]} body]
    (-> (xml/ast url)
        (res/response))))
