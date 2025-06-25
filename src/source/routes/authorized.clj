(ns source.routes.authorized)

(defn get [{:keys [user] :as _request}]
  {:status 200
   :body {:user user}})

