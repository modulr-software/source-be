(ns source.routes.integration
  (:require [ring.util.response :as res]
            [source.services.interface :as services]
            [source.services.bundles :as bundles]
            [source.workers.integrations :as integrations]))

(defn get
  {:summary "get integration by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:uuid :string]
                           [:user-id :int]
                           [:video :int]
                           [:podcast :int]
                           [:blog :int]
                           [:hash [:maybe :string]]
                           [:content-type-id :int]
                           [:ts-and-cs [:maybe :int]]
                           [:content-types [:vector
                                            [:map
                                             [:id :int]
                                             [:name :string]]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (let [integration (services/bundle ds {:id (:id path-params)})
        content-types (services/content-types-by-bundle ds {:bundle-id (:id path-params)})]
    (res/response (assoc integration :content-types content-types))))

(defn post
  {:summary "update an integration by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]
                :body [:map
                       [:name :string]
                       [:content-types [:vector
                                        [:map
                                         [:id :int]
                                         [:name :string]]]]
                       [:categories [:vector
                                     [:map
                                      [:id :int]
                                      [:name :string]]]]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [js ds store path-params body] :as _request}]
  (integrations/update-integration! ds js store {:bundle-id (:id path-params)
                                                 :bundle-metadata (dissoc body :categories :content-types)
                                                 :categories (:categories body)
                                                 :content-types (:content-types body)})
  (res/response {:message "successfully updated integration"}))

(defn delete
  {:summary "delete the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds js user path-params] :as _request}]
  (let [bundle (bundles/bundle ds {:where [:and
                                           [:= :id (:id path-params)]
                                           [:= :user-id (:id user)]]})]
    (if (some? bundle)
      (do
        (integrations/hard-delete-bundle! ds js (:id path-params))
        (res/response {:message "successfully deleted integration"}))
      (-> (res/response {:message "unauthorized"})
          (res/status 403)))))
