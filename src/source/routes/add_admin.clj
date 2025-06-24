(ns source.routes.add-admin
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn handler [{:keys [ds body] :as _request}]
  ;;TODO: needs schema validation here
  (let [{:keys [email password confirm-password]} body
        existing-user (services/user ds {:where [:= :email email]})]
    (cond
      (not (= password confirm-password))
      (-> (res/response {:error "passwords do not match"})
          (res/status 401))

      (some? existing-user)
      (-> (res/response {:error "an account for this email already exists!"}))

      :else
      (-> (services/register ds (merge body {:type "admin"}))
          (res/response)))))
