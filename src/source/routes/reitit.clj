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
        ["" (fn [_request] {:status 200 :body {:message "success"}})]
        ["users" 
         ["" routes/users]
         ["/:id" {:get routes/user
                  :patch routes/update-user}]]
        ["login" routes/login]
        ["register" routes/register]]]))))

(comment

  (let [app (create-app)
        request {:uri "/users" :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/users/2" :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/users/5" 
                 :request-method :patch
                 :body {:firstname "kiigan" 
                        :lastname "korinzu"}}]
    (-> request
        app
        :body 
        (json/read-json {:key-fn keyword})))
  ())
