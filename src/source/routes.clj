(ns source.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [source.middleware.auth.core :as auth]
            [source.db.master.users :as users]
            [source.db.master.connection :as con]
            [source.db.util :as db.util]
            [source.password :as pw]))

;; TODO
;; These endpoints will be refactored to use the updated "get-ds" function which takes in a db name
;; Will also make use of utility to generate db-name if the db-name is not static

(def home (GET "/" []
            {:status 200
             :body {:value "bye bitch"}
             :cookies {"test-session" {:value "test"}}}))

(def login
  (POST "/login" request
    (let [ds (db.util/conn :master)
          user (users/user-by
                ds
                {:col "email"
                 :val (get-in request [:body :email])})
          password (get-in request [:body :password])]
      (cond
        (not (some? user))
        {:status 401 :body {:message "Invalid username or password!"}}

        (not (pw/verify-password password (:password user)))
        {:status 401
         :body {:message "Invalid username or password!"}}

        :else
        (let [payload (dissoc user :password)]
          {:status 200
           :body (merge
                  {:user payload}
                  (auth/create-session payload))})))))

(def register
  (POST "/register" request
    (let [ds (db.util/conn :master)
          user (users/user-by
                ds
                {:col "email"
                 :val (get-in request [:body :email])})
          {:keys [password confirm-password]} (:body request)]
      (cond
        (not (= password confirm-password))
        {:status 400 :body {:message "passwords do not match!"}}

        (some? user)
        {:status 400 :body {:message "an account for this email already exists!"}}

        :else
        (let [new-user (get-in request [:body])]
          (users/insert-user ds {:email (:email new-user)
                                 :password (pw/hash-password password)
                                 :sector-id 1
                                 :firstname (:firstname new-user)
                                 :lastname (:lastname new-user)
                                 :business-name nil
                                 :type (:type new-user)})
          {:status 200 :body {:message "successfully created user"}})))))

(def users
  (GET "/users" []
    (let [ds (db.util/conn "master")]
      {:status 200
       :body {:users (users/users ds)}})))

(defroutes app
  home
  login
  users
  register
  (route/not-found "Page not found"))
