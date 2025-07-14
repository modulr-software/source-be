(ns source.routes.google-user
  (:require [source.oauth2.google.interface :as google]
            [source.middleware.auth.core :as auth]
            [source.services.users :as users]
            [source.db.util :as db.util]
            [ring.util.response :as res]))

(defn get [req]
  (let [{:keys [uuid _uri]} (:body req)
        email (google/google-session-user uuid (:params req))
        ds (db.util/conn :master)
        user (users/user ds {:where [:= :email email]})
        user-type (get-in req [:cookies "user_type" :value])]

    (if (some? user)
      (let [payload (dissoc user :password)
            session (auth/create-session payload)]
        (res/response (merge {:user payload} session)))
      (do
        (users/insert-user! ds {:data {:email email
                                       :type user-type}})
        (let [new-user (users/user ds {:where [:= :email email]})
              payload (dissoc new-user :password)
              session (auth/create-session payload)]
          (res/response (merge {:user payload} session)))))))
