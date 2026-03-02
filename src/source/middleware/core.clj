(ns source.middleware.core
  (:require [source.middleware.auth.core :as auth]
            [source.middleware.content-type :as content-type]
            [source.config :as conf]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [ring.util.response :as res]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as ring]
            [ring.middleware.cookies :as cookies]
            [clojure.walk :as walk]
            [source.util :as util]
            [clojure.string :as string]))

(defn wrap-ds [handler ds]
  (fn [request]
    (-> request
        (assoc :ds ds)
        (handler))))

(defn wrap-js
  "attaches the provided job service to the handler's request"
  [handler js]
  (fn [request]
    (-> request
        (assoc :js js)
        (handler))))

(defn apply-ds [app ds]
  (-> app
      (wrap-ds ds)))

(defn apply-js
  "middleware for attaching the job service to the request"
  [app js]
  (-> app
      (wrap-js js)))

(defn wrap-exception-logger [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (println "Unhandled Exception on endpoint URI " (:uri req) ": " e)
        (-> (res/response {:message "Internal Server Error"})
            (res/status 500))))))

(defn- validate-param [request [param-type schema]]
  (let [{:keys [error] :as validated} (-> (cond
                                            (= param-type :body) (:body request)
                                            (= param-type :path) (:path-params request)
                                            (= param-type :query) (:query-params request))
                                          (util/validate schema))]
    (->> (when error
           (str "In " (name param-type) ":\n" error))
         (assoc validated :param-type param-type :error))))

(defn- attach-validations [request validations]
  (reduce (fn [acc {:keys [data param-type]}]
            (cond
              (= param-type :body) (assoc acc :body data)
              (= param-type :path) (assoc acc :path-params data)
              (= param-type :query) (assoc acc :query-params data)
              :else acc)) request validations))

(defn wrap-input-validation [handler openapi-meta]
  (fn [request]
    (let [validations (->> (mapv (partial validate-param request) (:parameters openapi-meta)))
          errors (->> validations
                      (filter #(:error %))
                      (mapv :error))
          request (->> validations
                       (attach-validations request))]
      (if (seq errors)
        (do
          (println "Schema validation failed on endpoint URI" (:uri request) ":" (string/join "\n" errors))
          (-> (res/response {:message (string/join "\n" errors)})
              (res/status 400)))
        (handler request)))))

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

(defn wrap-query [handler]
  (fn [{:keys [query-params] :as request}]
    (-> request
        (assoc :query-params (walk/keywordize-keys query-params))
        (handler))))

(defn apply-generic [app & {:keys [ds js]}]
  (-> app
      (wrap-exception-logger)
      (apply-ds ds)
      (apply-js js)
      (wrap-case-conversion)
      (wrap-query)
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
