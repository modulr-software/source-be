(ns source.routes.business
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]
            [source.db.util :as db.util]))

(defn post [{:keys [ds body] :as _request}]
  (businesses/insert-business! ds body)
  (res/response {:message "successfully added business"}))

(defn patch [{:keys [ds body path-params] :as _request}]
  (businesses/update-business! ds {:id (:id path-params)
                                   :values body})
  (res/response {:message "successfully updated business"}))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
