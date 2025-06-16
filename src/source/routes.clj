(ns source.routes
  (:require [compojure.core :refer [defroutes GET POST PUT]]
            [compojure.route :as route]
            [source.oauth2.google.interface :as google]
            [ring.util.response :as response]
            [source.middleware.auth.core :as auth]
            [source.db.master.users :as users]
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
                                 :type (:type new-user)})
          {:status 200 :body {:message "successfully created user"}})))))

(def users
  (GET "/users" []
    (let [ds (db.util/conn :master)]
      {:status 200
       :body {:users (users/users ds)}})))

(def update-user
  (PUT "/users/:id" req []
    (let [userId (get-in req [:params :id])
          {:keys [onboarded 
                  address 
                  firstname 
                  lastname 
                  mobile]} (:body req)
          ds (db.util/conn :master)]

      (users/update-user! ds {:id userId
                             :cols ["onboarded" 
                                    "address" 
                                    "firstname"
                                    "lastname"
                                    "mobile"]
                             :vals [onboarded
                                    address
                                    firstname
                                    lastname
                                    mobile]})
      {:status 200 
       :body {:message "successfully updated user"}})))

(def google-launch (GET "/oauth2/google" []
                     (response/response (google/auth-uri))))

(def google-redirect (GET "/oauth2/google/callback" req []
                       (let [{:keys [uuid uri]} (:body req)
                             email (google/google-session-user uuid (:params req))
                             ds (db.util/conn :master)
                             user (users/user-by ds {:col "email"
                                                     :val email})]

                         (if (some? user)
                           (let [payload (dissoc user :password)]
                             {:status 200
                              :body (merge payload
                                           (auth/create-session payload))})

                           (do
                             (users/insert-user ds {:email email})
                             (let [new-user (users/user-by ds {:col "email"
                                                               :val email})
                                   payload (dissoc new-user :password)]
                               {:status 200
                                :body (merge payload
                                             (auth/create-session payload))}))))))

(defroutes app
  home
  login
  users
  register
  update-user

  google-launch
  google-redirect
  (route/not-found "Page not found"))

(comment)
