(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [source.middleware.interface :as mw]
            [source.db.interface :as db]
            [clojure.data.json :as json]
            [source.routes.interface :as routes]))

(defn create-app []
  (let [ds (db/ds :master)]
    (ring/ring-handler
     (ring/router
      [["/" {:middleware [[mw/apply-generic :ds ds]]}
        ["" (fn [request] {:status 200 :body {:message "success"}})]
        ["users" routes/users]
        ["user" routes/user]
        ["login" routes/login]
        ["register" routes/register]]]))))

(comment

  (let [app (create-app)
        request {:uri "/users" :request-method :get}]
    (-> request
        app
        :body
        (json/read-str {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/user" :request-method :get}]
    (-> request
        app
        :body
        (json/read-str {:key-fn keyword})))

  ())
