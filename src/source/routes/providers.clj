(ns source.routes.providers
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store] :as _request}]
  (-> (services/providers store)
      (res/response)))

(defn post [{:keys [store body] :as _request}]
  (let [{:keys [name]} body]
    (services/add-provider! store name)
    (res/response {:message "successfully added provider"})))

(comment 
  (require '[source.datastore.interface :as store])

  (services/add-provider! (store/ds :datahike) "YouTube")
  )
