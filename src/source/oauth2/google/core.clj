(ns source.oauth2.google.core
  (:require [source.config :as conf]
            [source.util :as util]
            [source.cache :as cache]
            [clojure.data.json :as json]
            [clj-oauth2.client :as oauth2]))

;; TODO: add a protocol implementation of the auth-request-cache (in a different namespace)
;; and import it here and use it in these functions
;; you probably want a set of interface functions (public functions) that
;; are exported functions which use the actual private implementationsS

;; when you write the test, you can test the implementation functions (the ones that start with "-")
;; instead of the public functions

(defn auth-request [access-token]
  {:headers {"Authorization" (str "Bearer " access-token)}})

(defn user-info [auth-request]
  (->
    (oauth2/get
       "https://www.googleapis.com/oauth2/v1/userinfo" 
       auth-request)
    (:body)))

(defn google-user-email [access-token]
    (-> access-token
        (auth-request)
        (user-info)
        (json/read-str {:key-fn keyword})
        (get  :email)))

(defn -auth-uri [auth-reqs-service]
  (let [req (cache/add-item auth-reqs-service
                            (oauth2/make-auth-request
                             (conf/read-value :oauth2 :google)))]
    {:uri (get-in req [:item :uri])
     :uuid (:uuid req)}))

(defn -google-session-user [auth-reqs-service uuid params]
  (let [result (-> (conf/read-value :oauth2 :google)
                   (oauth2/get-access-token
                    params
                    (cache/get-item auth-reqs-service uuid)))]
    (cache/remove-item auth-reqs-service uuid)
    (google-user-email (:access-token result))))

(comment
  (def uuid (util/uuid)))

