(ns source.routes.provider
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/provider ds)
       (res/response)))

(defn delete [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/delete-provider! ds))
  (res/response {:message "successfully deleted provider"}))
