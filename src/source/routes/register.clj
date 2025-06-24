(ns source.routes.register
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn handler [{:keys [ds user] :as _Request}]
  ;;todo: needs scehma validation here
  (let [{:keys [email password confirm-password]} user
        existing-user (services/user ds {:where [:= :email email]})]
    (cond
      (not (= password confirm-password))
      (-> (res/response {:error "passwords do not match"})
          (res/status 401))

      (some? existing-user)
      (-> (res/response {:error "an account for this email already exists!"}))

      :else
      (-> (services/register ds user)
          (res/response)))))
