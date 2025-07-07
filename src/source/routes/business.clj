(ns source.routes.business
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]))

(defn post [{:keys [ds body] :as _request}]
  (businesses/insert-business! ds {:data body})
  (res/response {:message "successfully added business"}))

(defn patch [{:keys [ds body path-params] :as _request}]
  (businesses/update-business! ds {:id (:id path-params)
                                   :values body})
  (res/response {:message "successfully updated business"}))

