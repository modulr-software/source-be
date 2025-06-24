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
        ["login" {:post routes/login}]
        ["register" routes/register]
        ["protected" {:middleware [[mw/apply-auth]]}
         ["/authorized" routes/authorized]]
        ["admin" {:middleware [[mw/apply-auth {:required-type :admin}]]}
         ["/add-admin" {:post routes/add-admin}]]]]))))

(comment
  (require '[source.middleware.auth.util :as auth.util])

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

  (let [app (create-app)
        request {:uri "/protected/authorized"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 5 :type "distributor"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/admin/add-admin"
                 :request-method :post
                 :body {:email "test@test.com"
                        :password "test"
                        :confirm-password "test"}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  ())
