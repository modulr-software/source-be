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
      (util/auth-token)
      (auth-util/verify-jwt)))

(def unauthorized-response {:status 403
                            :body {:message "Unathorized"}})

(defn wrap-auth [handler]
  (fn [request]
    (if-let [user (validate-request request)]
      (-> request
          (assoc :user user)
          (handler))

      unauthorized-response)))

(comment
  (let [authed-request {:headers {"Authorization"
                                  (str
                                   "Bearer "
                                   (auth-util/sign-jwt {:id 1 :role "admin"}))}}
        unauthed-request {:headers {"Authorization"
                                    (str
                                     "Bearer "
                                     "nonsense-token")}}
        test-handler (-> (fn [request]
                           request)
                         (wrap-auth))]
    (println
     "Is unauthed request rejected"
     (=
      403
      (-> unauthed-request
          (test-handler)
          (:status))))
    (println
     "Is user added to context of authed request"
     (=
      {:id 1 :role "admin"}
      (-> authed-request
          (test-handler)
          (:user))))))
