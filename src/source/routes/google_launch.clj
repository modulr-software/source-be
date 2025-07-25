(ns source.routes.google-launch
  (:require [source.oauth2.google.interface :as google]
            [ring.util.response :as response]))

(defn get
  {:summary "begins google federated login flow"
   :responses {200 {:body [:map
                           [:uuid :string]
                           [:uri :string]]}}}

  [_req]
  (response/response (google/auth-uri)))
