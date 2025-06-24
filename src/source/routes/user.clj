(ns source.routes.user
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get-handler [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/user ds)
       (assoc {} :user)
       (res/response)))

(defn patch-handler [{:keys [ds body path-params] :as _request}]
  (services/update-user! ds 
                         {:id (:id path-params) 
                          :values body})
  (res/response {:message "successfully updated user"}))

(comment
  (require '[source.db.interface :as db])
  (get-handler {:ds (db/ds :master) :path-params {:id 5}})
  (patch-handler {:ds (db/ds :master)
                  :path-params {:id 5}
                  :body {:firstname "kiigan"
                         :lastname "korinzu"}})
  ())
