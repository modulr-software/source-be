(ns source.middleware.core
  (:require [source.middleware.auth.core :as auth]
            [source.middleware.content-type :as content-type]
            [source.config :as conf]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as ring]
            [ring.middleware.cookies :as cookies]))

(defn wrap-ds [handler ds]
  (fn [request]
    (-> request
        (assoc :ds ds)
        (handler))))

(defn apply-ds [app ds]
  (-> app
      (wrap-ds ds)))

(defn apply-generic [app & {:keys [ds]}]
  (-> app
      (apply-ds ds)
      (content-type/wrap-content-type)
      (wrap-cors :access-control-allow-origin [(re-pattern (conf/read-value :cors-origin))]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-params)
      (wrap-defaults (assoc site-defaults :session false :security {:anti-forgery false}))
      (ring/wrap-json-response)
      (ring/wrap-json-body {:keywords? true})
      (cookies/wrap-cookies)))

(defn apply-auth [app & {:keys [required-type]}]
  (-> app
      (auth/wrap-auth-user-type {:required-type required-type})
      (auth/wrap-auth)))

