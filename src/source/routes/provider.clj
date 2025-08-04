(ns source.routes.provider
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store path-params] :as _request}]
  (->> (:id path-params)
       (Integer/parseInt)
       (services/provider store)
       (res/response)))

(defn delete [{:keys [store path-params] :as _request}]
  (->> (:id path-params)
       (Integer/parseInt)
       (services/delete-provider! store))
  (res/response {:message "successfully deleted provider"}))
