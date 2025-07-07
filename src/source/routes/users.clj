(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.services.users :as users]))

(defn get [{:keys [ds] :as _request}]
  (res/response {:users (users/users ds)}))

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master)})
  ())
