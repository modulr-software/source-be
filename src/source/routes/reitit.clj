(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.openapi :as openapi]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [reitit.ring.middleware.exception :as exception]
            [malli.util :as mu]
            [source.middleware.interface :as mw]
            [source.db.interface :as db]
            [clojure.data.json :as json]
            [source.routes.user :as user]
            [source.routes.users :as users]
            [source.routes.me :as me]
            [source.routes.login :as login]
            [source.routes.register :as register]
            [source.routes.google-launch :as google-launch]
            [source.routes.google-redirect :as google-redirect]
            [source.routes.google-user :as google-user]
            [source.routes.admin :as admin]
            [source.routes.authorized :as authorized]
            [source.routes.business :as business]
            [source.routes.businesses :as businesses]
            [source.routes.sectors :as sectors]
            [source.routes.selection-schemas :as selection-schemas]
            [source.routes.selection-schema :as selection-schema]
            [source.routes.provider-selection-schemas :as provider-selection-schemas]
            [source.routes.output-schemas :as output-schemas]
            [source.routes.output-schema :as output-schema]
            [source.routes.providers :as providers]
            [source.routes.provider :as provider]
            [source.routes.content-types :as content-types]
            [source.routes.content-type :as content-type]
            [source.routes.xml :as xml]
            [source.routes.data :as data]
            [source.util :as util]
            [source.datastore.interface :as store]))

(defn route [handlers]
  (reduce (fn [acc [k v]]
            (let [{:keys [summary parameters responses]} (util/metadata v)]
              (merge acc {k {:summary summary
                             :parameters parameters
                             :responses responses
                             :handler v}})))
          {} handlers))

(defn create-app []
  (let [ds (db/ds :master)
        store (store/ds :datahike)]
    (ring/ring-handler
     (ring/router
      [["/swagger.json" {:get {:no-doc true
                               :swagger {:info {:title "source-api"
                                                :description "swagger docs for source api with malli and reitit-ring"
                                                :version "0.0.1"}
                                         :securityDefinitions {"auth" {:type :apiKey
                                                                       :in :header
                                                                       :name "Authorization"}}}
                               :handler (swagger/create-swagger-handler)}}]

       ["/openapi.json" {:get {:no-doc true
                               :openapi {:info {:title "source-api"
                                                :description "openapi3 docs for source api with malli and reitit-ring"
                                                :version "0.0.1"}
                                         :components {:securitySchemes {"bearerAuth" {:type :http
                                                                                      :scheme :bearer
                                                                                      :bearerFormat "JWT"
                                                                                      :description "JWT Authorization using the Bearer scheme"}}}}
                               :handler (openapi/create-openapi-handler)}}]

       ["/users"        {:middleware [[mw/apply-auth {:required-type :admin}]]
                         :tags #{"users"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
        [""             (route {:get users/get})]
        ["/:id"         (route {:get user/get
                                :patch user/patch})]]

       ["/me"           {:middleware [[mw/apply-auth]]
                         :tags #{"me"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
        [""             (route {:get me/get})]]

       ["/businesses"   {:middleware [[mw/apply-auth {:required-type :admin}]]
                         :tags #{"businesses"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
        [""             (route {:get businesses/get
                                :post business/post})]
        ["/:id"         (route {:patch business/patch})]]

       ["/sectors"      {:tags #{"sectors"}}
        [""             (route {:get sectors/get})]]

       ["/login"        {:tags #{"auth"}}
        [""             (route {:post login/post})]]

       ["/register"     {:tags #{"auth"}}
        [""             (route {:post register/post})]]

       ["/oauth2"
        ["/google"      {:tags #{"google"}}
         [""            (route {:get google-launch/get})]
         ["/callback"   {:no-doc true
                         :get google-redirect/get}]
         ["/user"       (route {:get google-user/get})]]]

       ["/protected"    {:middleware [[mw/apply-auth]]
                         :tags #{"protected"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
        ["/authorized"  (route {:get authorized/get})]]

       ["/providers"
        [""             {:get providers/get}]
        ["/:id"         {:get provider/get}]]

       ["/content-types"
        [""             {:get content-types/get}]
        ["/:id"         {:get content-type/get}]]

       ["/admin"                  {:middleware [[mw/apply-auth {:required-type :admin}]]
                                   :no-doc true
                                   :tags #{"admin"}
                                   :swagger {:security [{"auth" []}]}
                                   :openapi {:security [{:bearerAuth []}]}}
        ["/add-admin"             (route {:post admin/post})]
        ["/selection-schemas"
         [""                      {:get selection-schemas/get
                                   :post selection-schemas/post}]
         ["/:id"                  {:get selection-schema/get}]
         ["/providers/:id"        {:get provider-selection-schemas/get}]]
        ["/output-schemas"
         [""                      {:get output-schemas/get
                                   :post output-schemas/post}]
         ["/:id"                  {:get output-schema/get}]]
        ["/providers"
         [""                      {:post providers/post}]
         ["/:id"                  {:delete provider/delete}]]
        ["/ast"                   {:post xml/post}]
        ["/extract-data"          {:post data/post}]]]

      {:data {:coercion (reitit.coercion.malli/create
                         {:error-keys #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                          :compile mu/closed-schema
                          :strip-extra-keys true
                          :default-values true
                          :options nil})
              :middleware [[mw/apply-generic :ds ds :store store]
                           ;;[exception/exception-middleware]
                           ]}})
     (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"
                                             :config {:validatorUrl nil
                                                      :urls [{:name "swagger", :url "swagger.json"}
                                                             {:name "openapi", :url "openapi.json"}]
                                                      :urls.primaryName "swagger"
                                                      :operationsSorter "alpha"}})
      (ring/create-default-handler)))))

(comment
  (require '[source.middleware.auth.util :as auth.util]
           '[source.rss.youtube :as yt])

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

  (let [app (create-app)
        request {:uri "/providers"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/providers/1"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/content-types"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/content-types/1"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        store (store/ds :datahike)
        request {:uri "/admin/selection-schemas"
                 :request-method :post
                 :store store
                 :body {:record {:provider-id 1
                                 :output-schema-id 1}
                        :schema {:title {:path ["tag/body" "tag/feed" "tag/title" "content/0"]}}}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        request {:uri "/admin/selection-schemas/1"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        store (store/ds :datahike)
        request {:uri "/admin/selection-schemas"
                 :store store
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (defn get-url []
    (->> "https://www.youtube.com/@ThePrimeTimeagen"
         (yt/find-channel-id)
         (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))

  (let [app (create-app)
        request {:uri "/admin/ast"
                 :request-method :post
                 :body {:url (get-url)}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app)
        store (store/ds :datahike)
        request {:uri "/admin/extract-data"
                 :store store
                 :request-method :post
                 :body {:schema-id 1
                        :url (get-url)}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (println (store/entities-with store :selection-schemas/id))
    (println (store/find-entities store {:key :selection-schemas/id
                                         :value 1}))
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  ())
