(ns source.routes.google-launch
  (:require [source.oauth2.google.interface :as google]
            [ring.util.response :as response]))

(defn get [_req]
  (response/response (google/auth-uri)))

