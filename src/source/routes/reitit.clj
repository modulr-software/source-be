(ns source.routes.reitit
  (:require [reitit.ring :as ring]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [source.middleware.interface :as mw]
            [clojure.data.json :as json]
            [source.routes.util :refer [get patch post delete] :as rutil]
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
            [source.routes.business-types :as business-types]
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
            [source.routes.analytics.creator.general :as analytics-creator-general]
            [source.routes.analytics.creator.deltas :as analytics-creator-deltas]
            [source.routes.analytics.creator.top :as analytics-creator-top]
            [source.routes.analytics.creator.top-average :as analytics-creator-top-average]
            [source.routes.analytics.distributor.general :as analytics-distributor-general]
            [source.routes.analytics.distributor.top :as analytics-distributor-top]
            [source.routes.analytics.distributor.top-average :as analytics-distributor-top-average]
            [source.routes.analytics.bundle.posts.-id-.views :as analytics-bundle-posts-id-views]
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
            [source.routes.jobs-view :as jobs-view]
            [source.routes.job :as job]
            [source.routes.job-deregister :as job-deregister]
            [source.routes.job-start :as job-start]
            [source.routes.job-stop :as job-stop]
            [source.routes.report :as report]
            [source.routes.approve-feed :as approve-feed]
            [source.routes.reject-feed :as reject-feed]))

(defn create-app [{:keys [ds store js]}]
  (ring/ring-handler
   (ring/router
    [(rutil/swagger-route)
     (rutil/openapi-route)

     ["/users" {:middleware [[mw/apply-auth {:required-type :admin}]]
                :tags #{"users"}
                :swagger {:security [{"auth" []}]}
                :openapi {:security [{:bearerAuth []}]}}

      ["" (get users/get)]
      ["/:id" (-> (get user/get)
                  (patch user/patch))]]

     ["/me" {:middleware [[mw/apply-auth]]
             :tags #{"me"}
             :swagger {:security [{"auth" []}]}
             :openapi {:security [{:bearerAuth []}]}}

      ["" (-> (get me/get)
              (post me/post))]
      ["/business" (-> (get me-business/get)
                       (post me-business/post))]
      ["/sectors" (-> (get me-sectors/get)
                      (post me-sectors/post))]]

     ["/mail" {:middleware [[mw/apply-auth]]
               :tags #{"mail"}
               :swagger {:security [{"auth" []}]}
               :openapi {:security [{:bearerAuth []}]}}

      ["/report" (post report/post)]]

     ["/businesses" {:middleware [[mw/apply-auth {:required-type :admin}]]
                     :tags #{"businesses"}
                     :swagger {:security [{"auth" []}]}
                     :openapi {:security [{:bearerAuth []}]}}

      ["" (-> (get businesses/get)
              (post business/post))]
      ["/:id" (patch business/patch)]]

     ["/business" {:tags #{"businesses"}}
      ["/types" (get business-types/get)]]

     ["/sectors" {:tags #{"sectors"}}
      ["" (get sectors/get)]]

     ["/login" {:tags #{"auth"}}
      ["" (post login/post)]]

     ["/register" {:tags #{"auth"}}
      ["" (post register/post)]]

     ["/oauth2"
      ["/google" {:tags #{"google"}}

       ["" (get google-launch/get)]
       ["/callback" {:no-doc true}
        ["" (get google-redirect/get)]]
       ["/user" (get google-user/get)]]]

     ["/protected" {:middleware [[mw/apply-auth]]
                    :tags #{"protected"}
                    :swagger {:security [{"auth" []}]}
                    :openapi {:security [{:bearerAuth []}]}}

      ["/authorized" (get authorized/get)]]

     ["/providers" {:tags #{"providers"}}
      ["" (get providers/get)]
      ["/:id" (get provider/get)]]

     ["/cadences" {:tags #{"cadences"}}
      ["" (get cadences/get)]]

     ["/categories" {:tags #{"categories"}}
      ["" (get categories/get)]
      ["/:id" (get category/get)]]

     ["/baselines" {:tags #{"baselines"}}
      ["" (get baselines/get)]]

     ["/contentTypes" {:tags #{"content types"}}
      ["" (get content-types/get)]
      ["/:id" (get content-type/get)]]

     ["/integrations" {:middleware [[mw/apply-auth]]
                       :tags #{"integrations"}}

      ["" (->  (get integrations/get)
               (post integrations/post))]
      ["/:id" (-> (get integration/get)
                  (post integration/post)
                  (delete integration/delete))]
      ["/:id/key" (post integration-key/post)]
      ["/:id/categories" (->  (get integration-categories/get)
                              (post integration-categories/post))]
      ["/:id/filter/feeds" (get integration-filter-feeds/get)]
      ["/:id/filter/feeds/:feed-id" (->  (get integration-filter-feed/get)
                                         (post integration-filter-feed/post))]
      ["/:id/filter/posts" (get integration-filter-posts/get)]
      ["/:id/filter/posts/:post-id" (-> (get integration-filter-post/get)
                                        (post integration-filter-post/post))]]

     ["/feeds" {:middleware [[mw/apply-auth]]
                :tags #{"feeds"}}

      ["" (-> (get feeds/get)
              (post feeds/post))]
      ["/:id" (-> (get feed/get)
                  (post feed/post)
                  (delete feed/delete))]
      ["/:id/posts" (get posts/get)]
      ["/:id/posts/:post-id" (get post/get)]
      ["/:id/posts/:post-id/prune" (post post-prune/post)]
      ["/:id/categories" (-> (get feed-categories/get)
                             (post feed-categories/post))]]

     ["/analytics" {:tags #{"analytics"}}

      ["/creator" {:middleware [[mw/apply-auth {:required-type :creator}]]}
       ["/general" (get analytics-creator-general/get)]
       ["/deltas" (get analytics-creator-deltas/get)]
       ["/top" (get analytics-creator-top/get)]
       ["/top/average" (get analytics-creator-top-average/get)]]
      ["/distributor" {:middleware [[mw/apply-auth {:required-type :distributor}]]}
       ["/general" (get analytics-distributor-general/get)]
       ["/top" (get analytics-distributor-top/get)]
       ["/top/average" (get analytics-distributor-top-average/get)]]
      ["/bundle" {:middleware [[mw/apply-bundle]]}
       ["/posts/:id/views" (post analytics-bundle-posts-id-views/post)]]
      ["admin" {:middleware [[mw/apply-auth {:required-type :admin}]]}
       ["/general"]
       ["/top"]]]
     ["/bundle" {:middleware [[mw/apply-bundle]]
                 :tags #{"bundles"}}
      ["" (get bundle/get)]
      ["/categories" (get bundle-categories/get)]
      ["/feeds" (post bundle-feeds/post)]
      ["/feeds/:id" (get bundle-feed/get)]
      ["/feeds/:id/posts" (get bundle-feed-posts/get)]
      ["/feeds/:id/posts/:post-id" (get bundle-feed-post/get)]
      ["/posts" (post bundle-posts/post)]
      ["/posts/:id" (get bundle-post/get)]]

     ["/api" {:middleware [[mw/apply-api-key]]
              :tags #{"api"}
              :swagger {:security [{"apiKey" []}]}
              :openapi {:security [{:apiKey []}]}}

      ["/bundle" {:middleware [[mw/apply-bundle]]}
       ["" (get bundle/get)]
       ["/categories" (get bundle-categories/get)]
       ["/feeds" (post bundle-feeds/post)]
       ["/feeds/:id" (get bundle-feed/get)]
       ["/feeds/:id/posts" (get bundle-feed-posts/get)]
       ["/feeds/:id/posts/:post-id" (get bundle-feed-post/get)]
       ["/posts" (post bundle-posts/post)]
       ["/posts/:id" (get bundle-post/get)]]
      ["/categories" (get categories/get)]
      ["/categories/:id" (get category/get)]]

     ["/admin" {:middleware [[mw/apply-auth {:required-type :admin}]]
                :tags #{"admin"}
                :swagger {:security [{"auth" []}]}
                :openapi {:security [{:bearerAuth []}]}}

      ["/business/types" (-> (post business-types/post)
                             (patch business-types/patch)
                             (delete business-types/delete))]
      ["/feeds" (get admin-feeds/get)]
      ["/feeds/:id/approve" (post approve-feed/post)]
      ["/feeds/:id/reject" (post reject-feed/post)]
      ["/jobs" (get jobs/get)]
      ["/jobs/manage/view" (get jobs-view/get)]
      ["/jobs/manage/register" (post jobs/post)]
      ["/jobs/:id" (get job/get)]
      ["/jobs/manage/deregister" (get job-deregister/get)]
      ["/jobs/manage/start" (get job-start/get)]
      ["/jobs/manage/stop" (get job-stop/get)]
      ["/add-admin" (post admin/post)]
      ["/selection-schemas" (-> (get selection-schemas/get)
                                (post selection-schemas/post))]
      ["/selection-schemas/:id" (get selection-schema/get)]
      ["/selection-schemas/providers/:id" (get provider-selection-schemas/get)]
      ["/output-schemas" (-> (get output-schemas/get)
                             (post output-schemas/post))]
      ["/output-schemas/:id" (get output-schema/get)]
      ["/providers" (post providers/post)]
      ["/providers/:id" (-> (post provider/post)
                            (delete provider/delete))]
      ["/ast" (post xml/post)]
      ["/extract-data" (post data/post)]]]

    (rutil/data-map ds store js))
   (ring/routes
    (rutil/swagger-ui-handler)
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
