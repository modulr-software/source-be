(ns source.routes.integrations
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.util :as utils]
            [source.migrate :as migrate]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.db.util :as db.util]))

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
  (res/response (services/bundles ds {:where [:= :user-id (:id user)]})))

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
  (let [new-bundle (services/insert-bundle! ds {:data (merge
                                                       (dissoc body :categories :content-types)
                                                       {:user-id (:id user)
                                                        :content-type-id 1 ; temporarily assign garbo id
                                                        :uuid (utils/uuid)})
                                                :ret :1})
        bundle-categories (mapv (fn [{:keys [id]}]
                                  {:bundle-id (:id new-bundle)
                                   :category-id id}) (:categories body))
        bundle-content-types (mapv (fn [{:keys [id]}]
                                     {:bundle-id (:id new-bundle)
                                      :content-type-id id}) (:content-types body))

        _ (migrate/migrate-bundle (:id new-bundle) ["up"])
        ; insert bundle categories
        bundle-ds (db.util/conn :bundle (:id new-bundle))

        _ (services/insert-bundle-category! bundle-ds {:data bundle-categories})
        _ (services/insert-bundle-content-types! ds {:data bundle-content-types})

        category-ids (services/category-id-by-bundle bundle-ds {:bundle-id (:id new-bundle)})
        id-vec (mapv (fn [{:keys [category-id]}] category-id) category-ids)
        categories-by-bundle (services/categories ds {:where [:in :id id-vec]})]

    (->> (jobs/prepare-congest-metadata
          ds
          store
          {:id (str "bundle_" (:id new-bundle))
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
         (congest/register! js))

    (res/response new-bundle)))
