(ns source.middleware.core
  (:require [source.middleware.auth.core :as auth]
            [source.middleware.content-type :as content-type]
            [source.config :as conf]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
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

(defn wrap-store [handler store]
  (fn [request]
    (-> request
        (assoc :store store)
        (handler))))

(defn apply-store [app store]
  (-> app
      (wrap-store store)))

(defn process-body [{:keys [body] :as req} t-fn]
  (assoc req
         :body
         (if
          (or (map? body) (vector? body) (seq? body))
           (cske/transform-keys (fn [k]
                                  (if (or (keyword? k) (string? k))
                                    (t-fn k)
                                    k)) body)
           body)))

(defn wrap-case-conversion [handler]
  (fn [request]
    (-> request
        (process-body csk/->kebab-case-keyword)
        (handler)
        (process-body csk/->camelCaseKeyword))))

(defn apply-ds [app ds]
  (-> app
      (wrap-ds ds)))

(defn apply-generic [app & {:keys [ds store]}]
  (-> app
      (apply-ds ds)
      (apply-store store)
      (wrap-case-conversion)
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

(defn apply-bundle [app]
  (-> app
      (auth/wrap-bundle-id)))

