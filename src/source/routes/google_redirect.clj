(ns source.routes.google-redirect
  (:require [source.oauth2.google.interface :as google]
            [source.middleware.auth.core :as auth]
            [source.services.users :as users]
            [source.db.util :as db.util]))

(defn get [req]
  (let [{:keys [uuid _uri]} (:body req)
        email (google/google-session-user uuid (:params req))
        ds (db.util/conn :master)
        user (users/user ds {:where [:= :email email]})
        user-type (get-in req [:cookies "user_type" :value])]

    (if (some? user)
      (let [payload (dissoc user :password)]
        {:status 200
         :body (merge payload
                      (auth/create-session payload))})

      (do
        (users/insert-user! ds {:email email
                                :type user-type})
        (let [new-user (users/user ds {:where [:= :email email]})
              payload (dissoc new-user :password)]
          {:status 200
           :body (merge payload
                        (auth/create-session payload))})))))
