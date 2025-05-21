(ns source.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [source.auth :as auth]
            [source.db.master.users :as users]
            [source.db.master.connection :as con]
            [source.password :as pw]))

(def home (GET "/" []
            {:status 200
             :body {:value "bye bitch"}
             :cookies {"test-session" {:value "test"}}}))

(def login
  (POST "/login" request
    (let [user (users/user-by
                con/ds
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
    (let [user (users/user-by
                con/ds
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
          (users/insert-user con/ds {:email (:email new-user)
                                     :password (pw/hash-password password)
                                     :sector-id 1
                                     :firstname (:firstname new-user)
                                     :lastname (:lastname new-user)
                                     :business-name nil
                                     :type (:type new-user)})
          {:status 200 :body {:message "successfully created user"}})))))

(def users
  (GET "/users" []
    {:status 200
     :body {:users (users/users con/ds)}}))

(defroutes app
  home
  login
  users
  register
  (route/not-found "Page not found"))
