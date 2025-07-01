(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.services.users :as users]))

(defn handler [{:keys [ds] :as _request}]
  (res/response {:users (users/users ds)}))

(comment
  (require '[source.db.interface :as db])
  (handler {:ds (db/ds :master)})
  ())
