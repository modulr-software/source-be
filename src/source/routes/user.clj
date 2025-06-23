(ns source.routes.user
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn handler [{:keys [ds params] :as _request}]
  (->> [:path :id]
       (get-in params)
       (services/user ds)
       (assoc {} :user)
       (res/response)))

(comment
  (require '[source.db.interface :as db])
  (handler {:ds (db/ds :master) :params {:path "1"}})
  ())
