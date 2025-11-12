(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.openapi :as openapi]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [malli.util :as mu]
            [source.middleware.interface :as mw]
            [clojure.data.json :as json]
            [source.util :as util]
            [source.routes.user :as user]
            [source.routes.users :as users]
            [source.routes.me :as me]
            [source.routes.me-business :as me-business]
            [source.routes.me-sectors :as me-sectors]
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
            [source.routes.cadences :as cadences]
            [source.routes.categories :as categories]
            [source.routes.category :as category]
            [source.routes.baselines :as baselines]
            [source.routes.content-types :as content-types]
            [source.routes.content-type :as content-type]
            [source.routes.feeds :as feeds]
            [source.routes.feed :as feed]
            [source.routes.feed-categories :as feed-categories]
            [source.routes.integrations :as integrations]
            [source.routes.integration :as integration]
            [source.routes.integration-key :as integration-key]
            [source.routes.integration-categories :as integration-categories]
            [source.routes.integration-filter-feeds :as integration-filter-feeds]
            [source.routes.integration-filter-feed :as integration-filter-feed]
            [source.routes.integration-filter-posts :as integration-filter-posts]
            [source.routes.integration-filter-post :as integration-filter-post]
            [source.routes.bundle :as bundle]
            [source.routes.bundle-categories :as bundle-categories]
            [source.routes.bundle-feeds :as bundle-feeds]
            [source.routes.bundle-feed :as bundle-feed]
            [source.routes.bundle-feed-posts :as bundle-feed-posts]
            [source.routes.bundle-feed-post :as bundle-feed-post]
            [source.routes.bundle-posts :as bundle-posts]
            [source.routes.bundle-post :as bundle-post]
            [source.routes.posts :as posts]
            [source.routes.post :as post]
            [source.routes.post-prune :as post-prune]
            [source.routes.admin-feeds :as admin-feeds]
            [source.routes.xml :as xml]
            [source.routes.data :as data]
            [source.routes.jobs :as jobs]
            [source.routes.job :as job]
            [source.routes.job-deregister :as job-deregister]
            [source.routes.job-start :as job-start]
            [source.routes.job-stop :as job-stop]
            [source.routes.report :as report]
            [source.routes.approve-feed :as approve-feed]
            [source.routes.reject-feed :as reject-feed]))

(defn route [handlers]
  (reduce (fn [acc [k v]]
            (let [{:keys [middleware summary parameters responses]} (util/metadata v)]
              (merge acc {k {:middleware middleware
                             :summary summary
                             :parameters parameters
                             :responses responses
                             :handler v}})))
          {} handlers))

(defn create-app [{:keys [ds store js]}]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"   {:get {:no-doc true
                               :swagger {:info {:title "source-api"
                                                :description "swagger docs for source api with malli and reitit-ring"
                                                :version "0.0.1"}
                                         :securityDefinitions {"auth" {:type :apiKey
                                                                       :in :header
                                                                       :name "Authorization"}
                                                               "apiKey" {:type :apiKey
                                                                         :in :header
                                                                         :name "Authorization"}}}
                               :handler (swagger/create-swagger-handler)}}]

     ["/openapi.json"   {:get {:no-doc true
                               :openapi {:info {:title "source-api"
                                                :description "openapi3 docs for source api with malli and reitit-ring"
                                                :version "0.0.1"}
                                         :components {:securitySchemes {"bearerAuth" {:type :http
                                                                                      :scheme :bearer
                                                                                      :bearerFormat "JWT"
                                                                                      :description "JWT Authorization using the Bearer scheme"}
                                                                        "apiKey" {:type :http
                                                                                  :scheme :bearer
                                                                                  :description "API Key authorization using the Bearer scheme"}}}}
                               :handler (openapi/create-openapi-handler)}}]

     ["/users"          {:middleware [[mw/apply-auth {:required-type :admin}]]
                         :tags #{"users"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
      [""               (route {:get users/get})]
      ["/:id"           (route {:get user/get
                                :patch user/patch})]]

     ["/me"             {:middleware [[mw/apply-auth]]
                         :tags #{"me"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
      [""               (route {:get me/get
                                :post me/post})]
      ["/business"      (route {:post me-business/post})]
      ["/sectors"       (route {:post me-sectors/post})]]

     ["/mail"             {:middleware [[mw/apply-auth]]
                           :tags #{"mail"}
                           :swagger {:security [{"auth" []}]}
                           :openapi {:security [{:bearerAuth []}]}}
      ["/report"          (route {:post report/post})]]

     ["/businesses"     {:middleware [[mw/apply-auth {:required-type :admin}]]
                         :tags #{"businesses"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
      [""               (route {:get businesses/get
                                :post business/post})]
      ["/:id"           (route {:patch business/patch})]]

     ["/sectors"        {:tags #{"sectors"}}
      [""               (route {:get sectors/get})]]

     ["/login"          {:tags #{"auth"}}
      [""               (route {:post login/post})]]

     ["/register"       {:tags #{"auth"}}
      [""               (route {:post register/post})]]

     ["/oauth2"
      ["/google"        {:tags #{"google"}}
       [""              (route {:get google-launch/get})]
       ["/callback"     {:no-doc true
                         :get google-redirect/get}]
       ["/user"         (route {:get google-user/get})]]]

     ["/protected"      {:middleware [[mw/apply-auth]]
                         :tags #{"protected"}
                         :swagger {:security [{"auth" []}]}
                         :openapi {:security [{:bearerAuth []}]}}
      ["/authorized"    (route {:get authorized/get})]]

     ["/providers"      {:tags #{"providers"}}
      [""               (route {:get providers/get})]
      ["/:id"           (route {:get provider/get})]]

     ["/cadences"       {:tags #{"cadences"}}
      [""               (route {:get cadences/get})]]

     ["/categories"     {:tags #{"categories"}}
      [""               (route {:get categories/get})]
      ["/:id"           (route {:get category/get})]]

     ["/baselines"      {:tags #{"baselines"}}
      [""               (route {:get baselines/get})]]

     ["/contentTypes"  {:tags #{"content types"}}
      [""               (route {:get content-types/get})]
      ["/:id"           (route {:get content-type/get})]]

     ["/integrations"   {:middleware [[mw/apply-auth]]
                         :tags #{"integrations"}}
      [""               (route {:get integrations/get
                                :post integrations/post})]
      ["/:id"
       [""              (route {:get integration/get
                                :post integration/post})]
       ["/key"          (route {:post integration-key/post})]
       ["/categories"   (route {:get integration-categories/get
                                :post integration-categories/post})]
       ["/filter"
        ["/feeds"
         [""            (route {:get integration-filter-feeds/get})]
         ["/:feed-id"   (route {:get integration-filter-feed/get
                                :post integration-filter-feed/post})]]
        ["/posts"
         [""            (route {:get integration-filter-posts/get})]
         ["/:post-id"   (route {:get integration-filter-post/get
                                :post integration-filter-post/post})]]]]]

     ["/feeds"          {:middleware [[mw/apply-auth]]
                         :tags #{"feeds"}}
      [""               (route {:get feeds/get
                                :post feeds/post})]
      ["/:id"
       [""              (route {:get feed/get
                                :post feed/post})]
       ["/posts"
        [""             (route {:get posts/get})]
        ["/:post-id"
         [""            (route {:get post/get})]
         ["/prune"      (route {:post post-prune/post})]]]
       ["/categories"   (route {:get feed-categories/get
                                :post feed-categories/post})]]]

     ["/bundle"        {:middleware [[mw/apply-bundle]]
                        :tags #{"bundles"}}
      [""               (route {:get bundle/get})]
      ["/categories"
       [""              (route {:get bundle-categories/get})]]
      ["/feeds"
       [""              (route {:post bundle-feeds/post})]
       ["/:id"
        [""             (route {:get bundle-feed/get})]
        ["/posts"
         [""            (route {:get bundle-feed-posts/get})]
         ["/:post-id"   (route {:get bundle-feed-post/get})]]]]
      ["/posts"
       [""              (route {:post bundle-posts/post})]
       ["/:id"          (route {:get bundle-post/get})]]]

     ["/api"             {:middleware [[mw/apply-api-key]]
                          :tags #{"api"}
                          :swagger {:security [{"apiKey" []}]}
                          :openapi {:security [{:apiKey []}]}}
      ["/bundle"         {:middleware [[mw/apply-bundle]]}
       [""               (route {:get bundle/get})]
       ["/categories"
        [""              (route {:get bundle-categories/get})]]
       ["/feeds"
        [""              (route {:post bundle-feeds/post})]
        ["/:id"
         [""             (route {:get bundle-feed/get})]
         ["/posts"
          [""            (route {:get bundle-feed-posts/get})]
          ["/:post-id"   (route {:get bundle-feed-post/get})]]]]
       ["/posts"
        [""              (route {:post bundle-posts/post})]
        ["/:id"          (route {:get bundle-post/get})]]]
      ["/categories"
       [""               (route {:get categories/get})]
       ["/:id"           (route {:get category/get})]]]

     ["/admin"                  {:middleware [[mw/apply-auth {:required-type :admin}]]
                                 :tags #{"admin"}
                                 :swagger {:security [{"auth" []}]}
                                 :openapi {:security [{:bearerAuth []}]}}
      ["/feeds"
       [""                      (route {:get admin-feeds/get})]
       ["/:id"
        ["/approve"             (route {:post approve-feed/post})]
        ["/reject"              (route {:post reject-feed/post})]]]
      ["/jobs"
       [""                      {:get jobs/get}]
       ["/manage"
        ["/register"            {:post jobs/post}]]
       ["/:id"
        [""                     {:get job/get}]
        ["/manage"
         ["/deregister"         {:get job-deregister/get}]
         ["/start"              {:get job-start/get}]
         ["/stop"               {:get job-stop/get}]]]]
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
       [""                      (route {:post providers/post})]
       ["/:id"                  (route {:delete provider/delete})]]
      ["/ast"                   {:post xml/post}]
      ["/extract-data"          {:post data/post}]]]

    {:data {:coercion (reitit.coercion.malli/create
                       {:error-keys #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                        :compile mu/closed-schema
                        :strip-extra-keys true
                        :default-values true
                        :options nil})
            :middleware [[mw/apply-generic :ds ds :store store :js js]]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/"
                                           :config {:validatorUrl nil
                                                    :urls [{:name "swagger", :url "swagger.json"}
                                                           {:name "openapi", :url "openapi.json"}]
                                                    :urls.primaryName "swagger"
                                                    :operationsSorter "alpha"}})
    (ring/create-default-handler))))

(comment
  (require '[source.middleware.auth.util :as auth.util]
           '[source.db.util :as db.util]
           '[congest.jobs :as js]
           '[source.datastore.interface :as store]
           '[source.rss.youtube :as yt])

  (def components {:ds (db.util/conn)
                   :store (store/ds :datahike)
                   :js (js/create-job-service [])})

  (let [app (create-app components)
        request {:uri "/users"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/users/3"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/users/3"
                 :request-method :patch
                 :body {:firstname "Keagan"
                        :lastname "Collins"}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/protected/authorized"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 5 :type "distributor"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
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

  (let [app (create-app components)
        request {:uri "/oauth2/google"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/businesses"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/businesses"
                 :request-method :post
                 :body {:name "beep"
                        :url "https://beep.com"}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/businesses/1"
                 :request-method :patch
                 :body {:name "thebest"
                        :url "http://thebest.com"}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/sectors"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/providers"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/providers/1"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/content-types"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/content-types/1"
                 :request-method :get}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (defn get-url []
    (->> "https://www.youtube.com/@ThePrimeTimeagen"
         (yt/find-channel-id)
         (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))

  (let [app (create-app components)
        request {:uri "/admin/feeds"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 5 :type "creator"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/admin/feeds"
                 :request-method :post
                 :body {:title "primeagen test"
                        :rss-url (get-url)
                        :provider-id 1
                        :content-type-id 1
                        :cadence-id 1
                        :baseline-id 1}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 5 :type "creator"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
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

  (let [app (create-app components)
        request {:uri "/admin/selection-schemas/1"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
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

  (let [app (create-app components)
        request {:uri "/admin/ast"
                 :request-method :post
                 :body {:url (get-url)}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
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

  (let [app (create-app components)
        request {:uri "/admin/jobs"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/admin/jobs/1"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/admin/jobs/manage/register"
                 :request-method :post
                 :body {:metadata {:initial-delay 10
                                   :auto-start true
                                   :stop-after-fail false,
                                   :interval 1000
                                   :recurring? true
                                   :args {:name "congest"}
                                   :handler "test"
                                   :created-at nil
                                   :sleep false}}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/admin/jobs/8/manage/deregister"
                 :request-method :get
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  (let [app (create-app components)
        request {:uri "/mail/report"
                 :request-method :post
                 :body {:message "I didn't get my cheesy fries how dare you"}
                 :headers {"authorization" (str "Bearer " (auth.util/sign-jwt {:id 1 :type "admin"}))}}]
    (-> request
        app
        :body
        (json/read-json {:key-fn keyword})))

  ())
