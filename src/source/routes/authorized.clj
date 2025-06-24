(ns source.routes.authorized)

(defn handler [req]
  {:status 200
   :body (:user req)})

