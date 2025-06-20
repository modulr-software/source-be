(ns source.middleware.auth.core
  (:require
   [source.middleware.auth.util :as util]
   [source.db.util :as db.util]
   [source.db.master.users :as users]))

(defn create-session [user]
  (let [payload {:id (:id user)}]
    {:access-token (util/sign-jwt payload)
     :refresh-token (util/sign-jwt payload)}))

(defn validate-request [request]
  (-> request
      (util/auth-token)
      (util/verify-jwt)))

(def unauthorized-response {:status 403
                            :body {:message "Unauthorized"}})

(defn wrap-auth [handler]
  (fn [request]
    (if-let [user (validate-request request)]
      (-> request
          (assoc :user user)
          (handler))

      unauthorized-response)))

(defn user-type
  "takes in a user type keyword and converts it to a string. throws if invalid user type"
  [user-type]
  (cond
    (= user-type :admin) "admin"
    (= user-type :creator) "creator"
    (= user-type :distributor) "distributor"
    :else (throw (Exception. "invalid user type"))))

(defn wrap-type-validation [handler required-type]
  (fn [request]
    (let [ds (db.util/conn :master)
          required-type (user-type required-type)
          user-type (->> {:id (get-in request [:user :id])}
                         (users/user ds)
                         (:type))]
      (if (= user-type required-type)
        (handler request)
        unauthorized-response))))

(comment
  (let [authed-request {:headers {"Authorization"
                                  (str
                                   "Bearer "
                                   (util/sign-jwt {:id 1 :role "admin"}))}}
        unauthed-request {:headers {"Authorization"
                                    (str
                                     "Bearer "
                                     "nonsense-token")}}
        test-handler (-> (fn [request]
                           request)
                         (wrap-auth))]
    (println "Is unauthed request rejected")
    (assert (=
             403
             (-> unauthed-request
                 (test-handler)
                 (:status))))
    (println "Tests passed")
    (println
     "Is user added to context of authed request")
    (assert (=
             {:id 1 :role "admin"}
             (-> authed-request
                 (test-handler)
                 (:user))))
    (println "Test passed")))
