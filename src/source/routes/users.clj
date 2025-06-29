(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn handler [{:keys [ds] :as _request}]
  (res/response {:users (services/users ds)}))

(comment
  (require '[source.db.interface :as db])
  (handler {:ds (db/ds :master)})
  ())
