(ns source.routes.integrations
  (:require [ring.util.response :as res]
            [source.util :as utils]
            [source.migrate :as migrate]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.db.util :as db.util]
            [source.jobs.handlers :as handlers]
            [source.services.bundle-categories :as bundle-categories]
            [source.services.bundle-content-types :as bundle-content-types]
            [source.services.bundles :as bundles]
            [source.services.categories :as categories]))

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
  {:summary "add an integration"
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
  (let [new-bundle (bundles/create-bundle! ds {:user-id (:id user)
                                               :bundle-metadata (dissoc body :categories :content-types)})
        _ (migrate/migrate-bundle (:id new-bundle) ["up"])]

    (with-open [bundle-ds (db.util/conn :bundle (:id new-bundle))]
      (let [_ (bundle-categories/insert-bundle-categories! bundle-ds {:bundle-id (:id new-bundle)
                                                                      :categories (:categories body)})
            _ (bundle-content-types/insert-bundle-content-types! ds {:bundle-id (:id new-bundle)
                                                                     :content-types (:content-types body)})

            ; service needed
            category-ids (bundle-categories/category-id bundle-ds {:bundle-id (:id new-bundle)})
            id-vec (mapv (fn [{:keys [category-id]}] category-id) category-ids)
            categories-by-bundle (categories/categories ds {:where [:in :id id-vec]})]

        ; service needed
        (->> (jobs/prepare-congest-metadata
              ds
              store
              {:id (handlers/update-bundle-job-id (:id new-bundle))
               :initial-delay 0
               :auto-start true
               :stop-after-fail false,
               :interval (* 1000 60 60 24)
               :recurring? true
               :ds ds
               :args {:bundle-id (:id new-bundle)
                      :categories categories-by-bundle}
               :handler :update-bundle
               :created-at (utils/get-utc-timestamp-string)
               :sleep false})
             (congest/register! js))))

    (res/response new-bundle)))
