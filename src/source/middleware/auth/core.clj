(ns source.middleware.auth.core
  (:require
   [source.util :as util]
   [source.middleware.auth.util :as auth-util]))

(defn create-session [user]
  (let [payload {:id (:id user)
                 :role (:role user)}]
    {:access-token (auth-util/sign-jwt payload)
     :refresh-token (auth-util/sign-jwt payload)}))

(defn validate-request [request]
  (-> request
      (util/request->auth-token)
      (auth-util/verify-jwt)))

(def unauthorized-response {:status 403
                            :body {:message "Unathorized"}})

(defn wrap-auth [handler]
  (fn [request]
    (if-let [user (validate-request request)]
      (-> request
          (assoc :user user)
          (handler))

      (handler request))))

(comment
  (validate-request {:headers {"Authorization" (str "Bearer " (auth-util/sign-jwt {:user "someone"}))}})
  (def test-request {:headers {"Content-Type" "application/json"
                               "Authorization" (str "Bearer " (auth-util/sign-jwt {:user "someone"}))}
                     :body {:data  "some super secret message"}})
  (defn test-handler [request]
    (println "User: " (:user request))
    {:status 200 :body {:message "this is a massage "}})

  (let [wrapped-handler (wrap-auth test-handler)]
    (wrapped-handler test-request)))
