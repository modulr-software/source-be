(ns source.middleware.auth.core
  (:require [source.middleware.auth.util :as util]
            [source.db.util :as db.util]
            [ring.util.response :as res]
            [source.services.users :as users]))

(defn create-session [user]
  (let [payload {:id (:id user)
                 :type (:type user)}]
    {:access-token (util/sign-jwt payload)
     :refresh-token (util/sign-jwt payload)}))

(defn validate-request [request]
  (-> request
      (util/auth-token)
      (util/verify-jwt)))

(defn wrap-auth [handler]
  (fn [request]
    (if-let [user (validate-request request)]
      (-> request
          (assoc :user user)
          (handler))
      (->
       (res/response {:message "Unauthorized"})
       (res/status 401)))))

(defn wrap-auth-user-type 
  "returns an unauthorized response if the user's type is not the required user type (provider | distributor | admin)"
  [handler & {:keys [required-type]}]
  (fn [request]
    (let [ds (db.util/conn :master)
          user-type (get-in request [:user :type])
          expected-type (->> {:id (get-in request [:user :id])}
                         (users/user ds)
                         (:type))]
      (cond
        (not (some? required-type)) (handler request)
        (and (= user-type (name :admin)) (= user-type expected-type)) (handler request)
        :else (->
               (res/response {:message "Unauthorized"})
               (res/status 403))))))

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
             401
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
