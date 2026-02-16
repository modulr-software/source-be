(ns source.routes.integrations
  (:require [ring.util.response :as res]
            [source.services.bundles :as bundles]
            [source.workers.integrations :as integrations]
            [source.util :as util]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [congest.jobs :as congest]))

(defn get
  {:summary "get all integrations"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:uuid :string]
                            [:user-id :int]
                            [:video :int]
                            [:podcast :int]
                            [:blog :int]
                            [:hash [:maybe :string]]
                            [:content-type-id :int]
                            [:ts-and-cs [:maybe :int]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (res/response (bundles/bundles ds {:where [:= :user-id (:id user)]})))

(defn post
  {:summary "creates an integration and the associated bundle in which content is stored"
   :parameters {:body [:map
                       [:name :string]
                       [:ts-and-cs {:optional true} :int]
                       [:content-types [:vector
                                        [:map
                                         [:id :int]
                                         [:name :string]]]]
                       [:categories [:vector
                                     [:map
                                      [:id :int]
                                      [:name :string]]]]]}
   :responses {201 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:uuid :string]
                           [:user-id :int]
                           [:video :int]
                           [:podcast :int]
                           [:blog :int]
                           [:hash {:optional true} [:maybe :string]]
                           [:content-type-id :int]
                           [:ts-and-cs {:optional true} :int]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [js ds store user body] :as _request}]
  (let [new-bundle (->> {:user-id (:id user)
                         :bundle-metadata (dissoc body :categories :content-types)
                         :categories (:categories body)
                         :content-types (:content-types body)}
                        (integrations/create-integration! ds))
        categories-by-bundle (bundles/categories-in-bundle ds (:id new-bundle))]
    ;TODO: service needed
    (->> (jobs/prepare-congest-metadata
          ds
          store
          {:id (handlers/update-bundle-job-id (:id new-bundle))
           :initial-delay 0
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24)
           :recurring? true
           :args {:bundle-id (:id new-bundle)
                  :categories categories-by-bundle}
           :handler :update-bundle
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))
    (res/response new-bundle)))
