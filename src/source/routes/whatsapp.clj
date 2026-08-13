(ns source.routes.whatsapp
  (:require [ring.util.response :as res]
            [source.workers.whatsapp :as wa]))

(defn session
  {:summary "get the current whatsapp session status"
   :responses {200 {:body [:map
                            [:name :string]
                            [:status :string]]}}}
  [_request]
  (-> (wa/create-client)
      (wa/get-session {})
      (:body)
      (res/response)))

(defn start-session
  {:summary "start the whatsapp session"
   :responses {200 {:body [:map
                           [:name :string]
                           [:status :string]]}}}
  [_request]
  (-> (wa/create-client)
      (wa/start-session {})
      (:body)
      (res/response)))

(defn qr
  {:summary "get the whatsapp session qr code"
   :responses {200 {:body [:map [:qr :string]]}}}
  [_request]
  (-> (wa/create-client)
      (wa/get-qr {:format "raw"})
      (:body)
      (res/response)))
