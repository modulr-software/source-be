(ns source.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [source.users :as users]
            [source.auth :as auth]
            [source.password :as pw]))

(def home (GET "/" []
            {:status 200
                       :body {:value "bye bitch"}
                       :cookies {"test-session" {:value "test"}}}))

(def login
  (POST "/login" request
    (clojure.pprint/pprint request)
    (let [body (:body request)
          user (users/get-user-by-username (:username :body))
          hashed-password (get-in user [:password])
          password (get-in body [:password])]
      
      (clojure.pprint/pprint user)

    (cond
      (not (some? user))
      {:status 401 :body {:message "Invalid username or password!"}}

      (not (pw/verify-password password hashed-password))
      {:status 401
       :body {:message "Invalid username or password!"}}

      :else
      (let [payload (dissoc user :password)]
        {:status 200
         :body (merge
                {:user payload}
                (auth/create-session payload))}))

      )))

(login [{:username "test" :password "test"}])

(def register
  (POST "/register" request
    (println "in register")
    (users/create-user (:body request))))

(def users
  (GET "/users" []
    {:status 200
     :body {:users (users/get-users)}}))


(defroutes app
  home
  login
  users
  register
  (route/not-found "Page not found"))
