(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.coercion.malli]
            [reitit.ring.malli]
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
            [source.routes.business :as business]
            [source.routes.businesses :as businesses]
            [source.routes.sectors :as sectors]))

(defn create-app []
  (let [ds (db/ds :master)]
    (ring/ring-handler
     (ring/router
      [["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "source-api"
                                :description "swagger docs for source api with malli and reitit-ring"}
                         :securityDefinitions {"auth" {:type :apiKey
                                                       :in :header
                                                       :name "Authorization"}}}
               :handler (swagger/create-swagger-handler)}}]
       ["/users" {:middleware [[mw/apply-auth {:required-type :admin}]]
                  :tags #{"users"}
                  :swagger {:security [{"auth" []}]}}
        ["" {:get {:summary "get all users"
                   :responses users/get-responses
                   :handler users/get}}]
        ["/:id" {:get {:summary "get user by id"
                       :parameters user/get-parameters
                       :responses user/get-responses
                       :handler user/get}
                 :patch {:summary "update user by id"
                         :parameters user/patch-parameters
                         :responses user/patch-responses
                         :handler user/patch}}]]
       ["/businesses" {:middleware [[mw/apply-auth {:required-type :admin}]]
                       :tags #{"businesses"}
                       :swagger {:security [{"auth" []}]}}
        ["" {:get {:summary "get all businesses"
                   :parameters businesses/get-parameters
                   :responses businesses/get-responses
                   :handler businesses/get}
             :post {:summary "insert a business"
                    :parameters business/post-parameters
                    :responses business/post-responses
                    :handler business/post}}]
        ["/:id" {:patch {:summary "update business by id"
                         :parameters business/patch-parameters
                         :responses business/patch-responses
                         :handler business/patch}}]]
       ["/sectors" {:tags #{"sectors"}}
        ["" {:get {:summary "get all sectors"
                   :responses sectors/get-responses
                   :handler sectors/get}}]]
       ["/login" {:tags #{"auth"}
                  :post {:summary "get user data and access token provided valid login credentials"
                         :parameters login/post-parameters
                         :responses login/post-responses
                         :handler login/post}}]
       ["/register" {:tags #{"auth"}
                     :post {:summary "register a new user"
                            :parameters register/post-parameters
                            :responses register/post-responses
                            :handler register/post}}]
       ["/oauth2" {:no-doc true}
        ["/google"
         ["" {:get google-launch/get}]
         ["/callback" {:get google-redirect/get}]]]
       ["/protected" {:middleware [[mw/apply-auth]]
                      :tags #{"protected"}
                      :swagger {:security [{"auth" []}]}}
        ["/authorized" {:get {:summary "checks if authenticated"
                              :responses authorized/get-responses
                              :handler authorized/get}}]]
       ["/admin" {:middleware [[mw/apply-auth {:required-type :admin}]]
                  :tags #{"admin"}
                  :swagger {:security [{"auth" []}]}}
        ["/add-admin" {:post {:summary "registers an admin user"
                              :parameters admin/post-parameters
                              :responses admin/post-responses
                              :handler admin/post}}]]]

      {:data {:middleware [[mw/apply-generic :ds ds]]}})
     (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"})
      (ring/create-default-handler)))))

(comment
  (require '[source.middleware.auth.util :as auth.util])

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
        request {:uri "/businesses"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/businesses"
                 :request-method :post
                 :body {:name "beep"
                        :url "https://beep.com"}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/businesses/1"
                 :request-method :patch
                 :body {:name "thebest"
                        :url "http://thebest.com"}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/sectors"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  ())
