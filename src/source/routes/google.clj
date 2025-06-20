(ns source.routes.google
  (:require [source.oauth2.google.interface :as google]
            [ring.util.response :as response]
            [source.middleware.auth.core :as auth]
            [source.db.master.users :as db.users]
            [source.db.util :as db.util]))

(defn google-launch [_req]
  (response/response (google/auth-uri)))

(defn google-redirect [req]
  (let [{:keys [uuid _uri]} (:body req)
        email (google/google-session-user uuid (:params req))
        ds (db.util/conn :master)
        user (db.users/user-by ds {:col "email"
                                   :val email})]

    (if (some? user)
      (let [payload (dissoc user :password)]
        {:status 200
         :body (merge payload
                      (auth/create-session payload))})

      (do
        (db.users/insert-user ds {:email email})
        (let [new-user (db.users/user-by ds {:col "email"
                                             :val email})
              payload (dissoc new-user :password)]
          {:status 200
           :body (merge payload
                        (auth/create-session payload))})))))
