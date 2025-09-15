(ns source.routes.me-business
  (:require [source.util :as util]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn post
  {:summary "add business for logged-in user"
   :parameters {:body [:map
                       [:name {:optional true} :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (let [{:keys [data error success]} (util/validate post body)]
    (if (not success)
      (-> (res/response {:message error})
          (res/status 400))

      (let [business (services/insert-business! ds data)]
        (services/update-user! ds {:id (:id user)
                                   :data {:business-id (:id business)}})
        (res/response {:message "successfully added business"})))))
