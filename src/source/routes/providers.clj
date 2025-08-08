(ns source.routes.providers
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (-> (services/providers ds)
      (res/response)))

(defn post [{:keys [ds body] :as _request}]
  (let [{:keys [provider]} body]
    (services/insert-provider! ds {:data provider
                                   :ret :1})
    (res/response {:message "successfully added provider"})))

(comment
  (require '[source.datastore.interface :as store])

  (services/add-provider! (store/ds :datahike) "YouTube")
  ())
