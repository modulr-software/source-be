(ns source.oauth2.google
  (:require [source.config :as conf]
            [clojure.data.json :as json]
            [source.util :as util]
            [clj-oauth2.client :as oauth2]))

(def ^:private *auth-reqs (atom {}))

;; todo: add a protocol implementation of the auth-request-cache (in a different namespace)
;; and import it here and use it in these functions
;; you probably want a set of interface functions (public functions) that
;; are exported functions which use the actual private implementationsS

;; when you write the test, you can test the implementation functions (the ones that start with "-")
;; instead of the public functions

(defn- google-user-email [access-token]
  (let [response (oauth2/get "https://www.googleapis.com/oauth2/v1/userinfo"
                             {:headers {"Authorization" (str "Bearer " access-token)}})]
    (get (json/read-str (:body response) {:key-fn keyword}) :email)))

(defn auth-req [uuid]
  (uuid @*auth-reqs))

(defn auth-uri []
  (let [auth-req (oauth2/make-auth-request (conf/read-value :oauth2 :google))
        uuid (util/uuid)]
    (swap! *auth-reqs assoc uuid auth-req)
    {:uri (:uri auth-req)
     :uuid uuid}))

(defn google-session-user [uuid params]
  (let [result (-> (conf/read-value :oauth2 :google)
                   (oauth2/get-access-token params (auth-req uuid)))]
    (swap! *auth-reqs dissoc uuid)
    (google-user-email (:access-token result))))

(comment
  (util/uuid)
  (auth-uri))
