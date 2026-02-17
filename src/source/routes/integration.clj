(ns source.routes.integration
  (:require [ring.util.response :as res]
            [source.services.interface :as services]
            [source.services.bundles :as bundles]
            [source.workers.integrations :as integrations]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.util :as util]
            [source.jobs.handlers :as handlers]))

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

  [{:keys [js ds path-params body] :as _request}]
  (let [bundle-id (:id path-params)
        job-id (str "bundle_" bundle-id)
        categories-by-bundle (bundles/categories-in-bundle ds bundle-id)]
    (integrations/update-integration! ds {:bundle-id bundle-id
                                          :bundle-metadata (dissoc body :categories :content-types)
                                          :categories (:categories body)
                                          :content-types (:content-types body)})
    ;TODO: service needed
    (congest/deregister! js job-id)
    (->> (jobs/prepare-congest-metadata
          ds
          {:id job-id
           :initial-delay (* 1000 60 60 24)
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24)
           :recurring? true
           :ds ds
           :args {:bundle-id bundle-id
                  :categories categories-by-bundle}
           :handler :update-bundle
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js)))
  (res/response {:message "successfully updated integration"}))

(defn delete
  {:summary "delete the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds js user path-params] :as _request}]
  (let [bundle-id (:id path-params)
        bundle (bundles/bundle ds {:where [:and
                                           [:= :id bundle-id]
                                           [:= :user-id (:id user)]]})
        job-id (handlers/update-bundle-job-id bundle-id)]
    (if (some? bundle)
      (do
        (integrations/hard-delete-bundle! ds js job-id bundle-id)
        (res/response {:message "successfully deleted integration"}))
      (-> (res/response {:message "unauthorized"})
          (res/status 403)))))
