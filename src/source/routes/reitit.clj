(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [malli.util :as mu]
            [muuntaja.core :as muuntaja]
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
                   :responses {200 {:body [:map
                                           [:users
                                            [:vector
                                             [:map
                                              [:id :int]
                                              [:address {:optional true} :string]
                                              [:profile-image {:optional true} :string]
                                              [:email :string]
                                              [:firstname {:optional true} :string]
                                              [:lastname {:optional true} :string]
                                              [:type [:enum "creator" "distributor" "admin"]]
                                              [:email-verified {:optional true} :int]
                                              [:onboarded {:optional true} :int]
                                              [:mobile {:optional true} :string]]]]]}
                               401 {:body [:map [:message :string]]}
                               403 {:body [:map [:message :string]]}}
                   :handler users/get}}]
        ["/:id" {:get {:summary "get user by id"
                       :parameters {:path [:map [:id {:title "id"
                                                      :description "user id"} :int]]}
                       :responses {200 {:body [:map
                                               [:user
                                                [:map
                                                 [:id :int]
                                                 [:address {:optional true} :string]
                                                 [:profile-image {:optional true} :string]
                                                 [:email :string]
                                                 [:firstname {:optional true} :string]
                                                 [:lastname {:optional true} :string]
                                                 [:type [:enum "creator" "distributor" "admin"]]
                                                 [:email-verified {:optional true} :int]
                                                 [:onboarded {:optional true} :int]
                                                 [:mobile {:optional true} :string]]]]}
                                   401 {:body [:map [:message :string]]}
                                   403 {:body [:map [:message :string]]}}
                       :handler user/get}
                 :patch {:summary "update user by id"
                         :parameters {:path [:map [:id {:title "id"
                                                        :description "user id"} :int]]
                                      :body [:map
                                             [:address {:optional true} :string]
                                             [:profile-image {:optional true} :string]
                                             [:email :string]
                                             [:firstname {:optional true} :string]
                                             [:lastname {:optional true} :string]
                                             [:type [:enum "creator" "distributor" "admin"]]
                                             [:email-verified {:optional true} :int]
                                             [:onboarded {:optional true} :int]
                                             [:mobile {:optional true} :string]]}
                         :responses {200 {:body [:map
                                                 [:user
                                                  [:map
                                                   [:id :int]
                                                   [:address {:optional true} :string]
                                                   [:profile-image {:optional true} :string]
                                                   [:email :string]
                                                   [:firstname {:optional true} :string]
                                                   [:lastname {:optional true} :string]
                                                   [:type [:enum "creator" "distributor" "admin"]]
                                                   [:email-verified {:optional true} :int]
                                                   [:onboarded {:optional true} :int]
                                                   [:mobile {:optional true} :string]]]]}
                                     401 {:body [:map [:message :string]]}
                                     403 {:body [:map [:message :string]]}}
                         :handler user/patch}}]]
       ["/businesses" {:middleware [[mw/apply-auth {:required-type :admin}]]
                       :tags #{"businesses"}
                       :swagger {:security [{"auth" []}]}}
        ["" {:get {:summary "get all businesses"
                   :parameters {:body [:map
                                       [:name :string]
                                       [:url {:optional true} :string]
                                       [:linkedin {:optional true} :string]
                                       [:twitter {:optional true} :string]]}
                   :responses {200 {:body [:map
                                           [:businesses
                                            [:map
                                             [:id :int]
                                             [:name :string]
                                             [:url {:optional true} :string]
                                             [:linkedin {:optional true} :string]
                                             [:twitter {:optional true} :string]]]]}}
                   :handler businesses/get}
             :post {:summary "insert a business"
                    :parameters {:body [:map
                                        [:name :string]
                                        [:url {:optional true} :string]
                                        [:linkedin {:optional true} :string]
                                        [:twitter {:optional true} :string]]}
                    :responses {201 {:body [:map [:message :string]]}}
                    :handler business/post}}]
        ["/:id" {:patch {:summary "update business by id"
                         :parameters {:path [:map [:id {:title "id"
                                                        :description "business id"} :int]]
                                      :body [:map
                                             [:name :string]
                                             [:url {:optional true} :string]
                                             [:linkedin {:optional true} :string]
                                             [:twitter {:optional true} :string]]}
                         :responses {200 {:body [:map [:message :string]]}}
                         :handler business/patch}}]]
       ["/sectors" {:tags #{"sectors"}}
        ["" {:get {:summary "get all sectors"
                   :responses {200 {:body [:map
                                           [:sectors
                                            [:map
                                             [:id :int]
                                             [:name :string]]]]}}
                   :handler sectors/get}}]]
       ["/login" {:tags #{"auth"}}
        {:post {:summary "get user data and access token provided valid login credentials"
                :parameters {:body [:map
                                    [:email :string]
                                    [:password :string]]}
                :responses {200 {:body [:map
                                        [:user
                                         [:map
                                          [:id :int]
                                          [:address {:optional true} :string]
                                          [:profile-image {:optional true} :string]
                                          [:email :string]
                                          [:firstname {:optional true} :string]
                                          [:lastname {:optional true} :string]
                                          [:type [:enum "creator" "distributor" "admin"]]
                                          [:email-verified {:optional true} :int]
                                          [:onboarded {:optional true} :int]
                                          [:mobile {:optional true} :string]]]
                                        [:access-token :string]
                                        [:refresh-token :string]]}
                            401 {:body [:map [:message :string]]}}
                :handler login/post}}]
       ["/register" {:tags #{"auth"}}
        {:post {:summary "register a new user"
                :parameters {:body [:map
                                    [:email :string]
                                    [:password :string]
                                    [:confirm-password :string]]}
                :responses {200 {:body [:map
                                        [:user
                                         [:map
                                          [:id :int]
                                          [:address {:optional true} :string]
                                          [:profile-image {:optional true} :string]
                                          [:email :string]
                                          [:firstname {:optional true} :string]
                                          [:lastname {:optional true} :string]
                                          [:type [:enum "creator" "distributor" "admin"]]
                                          [:email-verified {:optional true} :int]
                                          [:onboarded {:optional true} :int]
                                          [:mobile {:optional true} :string]]]
                                        [:access-token :string]
                                        [:refresh-token :string]]}}
                :handler register/post}}]
       ["/oauth2" {:no-doc true}
        ["/google"
         ["" {:get google-launch/get}]
         ["/callback" {:get google-redirect/get}]]]
       ["/protected" {:middleware [[mw/apply-auth]]
                      :tags #{"protected"}
                      :swagger {:security [{"auth" []}]}}
        ["/authorized" {:get {:summary "checks if authenticated"
                              :responses {200 {:body [:map
                                                      [:user
                                                       [:map
                                                        [:id :int]
                                                        [:type [:enum "creator" "distributor" "admin"]]]]]}}
                              :handler authorized/get}}]]
       ["/admin" {:middleware [[mw/apply-auth {:required-type :admin}]]
                  :tags #{"admin"}
                  :swagger {:security [{"auth" []}]}}
        ["/add-admin" {:post {:summary "registers an admin user [admins only]"
                              :parameters {:body [:map
                                                  [:email :string]
                                                  [:password :string]
                                                  [:confirm-password :string]]}
                              :responses {201 {:body [:map [:message :string]]}
                                          401 {:body [:map [:message :string]]}
                                          403 {:body [:map [:message :string]]}}
                              :handler admin/post}}]]]

      {:data {:coercion (reitit.coercion.malli/create
                         {:error-keys #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                          :compile mu/closed-schema
                          :strip-extra-keys true
                          :default-values true
                          :options nil})
              :muuntaja muuntaja/instance
              :middleware [[mw/apply-generic :ds ds]]}})
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
