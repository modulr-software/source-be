(ns source.routes.user
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/user ds)
       (assoc {} :user)
       (res/response)))

(defn patch [{:keys [ds body path-params] :as _request}]
  (services/update-user! ds
                         {:id (:id path-params)
                          :values body})
  (res/response {:message "successfully updated user"}))

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master) :path-params {:id 5}})
  (patch {:ds (db/ds :master)
          :path-params {:id 5}
          :body {:firstname "kiigan"
                 :lastname "korinzu"}})
  ())
