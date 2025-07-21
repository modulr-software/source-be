(ns source.routes.business
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]
            [source.util :as utils]))

(defn post
  {:summary "insert a business"
   :parameters {:body [:map
                       [:name :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]]}
   :responses {201 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]

  (let [{:keys [data error success]} (utils/validate post body)]
    (if (not success) (-> (res/response error)
                          (res/status 400))

        (do (businesses/insert-business! ds {:values data})
            (res/response {:message "successfully added business"})))))

(defn patch
  {:summary "update a business by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "business id"} :int]]
                :body [:map
                       [:name :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds body path-params] :as _request}]

  (let [{:keys [data error success]} (utils/validate patch body)]
    (if (not success)

      (-> (res/response error)
          (res/status 400))

      (do (businesses/update-business! ds {:id (:id path-params)
                                           :values data})
          (res/response {:message "successfully updated business"})))))

(comment
  (require '[source.db.util :as db.util])
  (post {:ds (db.util/conn :master)
         :body {:name "modulr"
                :url "https://modulr.com"}})
  ())

