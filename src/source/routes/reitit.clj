(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [source.middleware.interface :as mw]
            [source.db.interface :as db]
            [clojure.data.json :as json]
            [source.routes.user :as user]
            [source.routes.users :as users]
            [source.routes.login :as login]
            [source.routes.register :as register]
            [source.routes.google-launch :as google-launch]
            [source.routes.google-redirect :as google-redirect]
            [source.routes.admin :as admin]
            [source.routes.authorized :as authorized]
            [source.routes.selection-schema :as selection-schema]))

(defn create-app []
  (let [ds (db/ds :master)]
    (ring/ring-handler
     (ring/router
      [["/" {:middleware [[mw/apply-generic :ds ds]]}
        ["" (fn [_request] {:status 200 :body {:message "success"}})]
        ["users"
         ["" users/handler]
         ["/:id" {:get user/get
                  :patch user/patch}]]
        ["login" {:post login/post}]
        ["register" {:post register/post}]
        ["oauth2"
         ["/google"
          ["" {:get google-launch/get}]
          ["/callback" {:get google-redirect/get}]]]
        ["protected" {:middleware [[mw/apply-auth]]}
         ["/authorized" {:get authorized/get}]]
        ["admin" {:middleware [[mw/apply-auth {:required-type :admin}]]}
         ["/add-admin" {:post admin/post}]
         ["/selection-schema" {:post selection-schema/post}]]]]))))

(comment
  (require '[source.middleware.auth.util :as auth.util]
           '[source.datastore.util :as store.util]
           '[datalevin.core :as dl])

  (let [app (create-app)
        request {:uri "/users" :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/users/3" :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/users/3"
                 :request-method :patch
                 :body {:firstname "Keagan"
                        :lastname "Collins"}}]
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

  (let [app (create-app)
        request {:uri "/oauth2/google"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        store (store.util/conn "datalevin")
        ;; open table before operation
        thing (dl/open-dbi store "selection-schemas")
        request {:uri "/admin/selection-schema"
                 :request-method :post
                 :store store
                 :body {:record {:provider-id 1
                                 :output-schema-id 1}
                        :schema {:hi "hello"}}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))
  ())
