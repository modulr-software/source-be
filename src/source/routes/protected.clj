(ns source.routes.protected)

(defn authorized [req]
  {:status 200
   :body (:user req)})

