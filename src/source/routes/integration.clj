(ns source.routes.integration
  (:require [ring.util.response :as res]
            [source.services.interface :as services]
            [source.db.util :as db.util]
            [congest.jobs :as congest]
            [source.util :as utils]
            [source.jobs.core :as jobs]
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

  [{:keys [js ds store path-params body] :as _request}]
  (let [_ (services/update-bundle! ds {:id (:id path-params)
                                       :data (dissoc body :categories :content-types)})

        bundle-categories (mapv (fn [{:keys [id]}]
                                  {:bundle-id (:id path-params)
                                   :category-id id}) (:categories body))
        bundle-content-types (mapv (fn [{:keys [id]}]
                                     {:bundle-id (:id path-params)
                                      :content-type-id id}) (:content-types body))

        job-id (str "bundle_" (:id path-params))

        ; update bundle categories
        bundle-ds (db.util/conn :bundle (:id path-params))
        _ (services/delete-bundle-category! bundle-ds {:where [:= :bundle-id (:id path-params)]})
        _ (services/insert-bundle-category! bundle-ds {:data bundle-categories})

        ; update bundle content types
        _ (services/delete-bundle-content-types! ds {:where [:= :bundle-id (:id path-params)]})
        _ (services/insert-bundle-content-types! ds {:data bundle-content-types})

        category-ids (services/category-id-by-bundle bundle-ds {:bundle-id (:id path-params)})
        id-vec (mapv (fn [{:keys [category-id]}] category-id) category-ids)
        categories-by-bundle (services/categories ds {:where [:in :id id-vec]})]

    (congest/deregister! js job-id)
    (->> (jobs/prepare-congest-metadata
          ds
          store
          {:id job-id
           :initial-delay (* 1000 60 60 24)
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24)
           :recurring? true
           :ds ds
           :args {:bundle-id (:id path-params)
                  :categories categories-by-bundle}
           :handler :update-bundle
           :created-at (utils/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))

    (res/response {:message "successfully updated integration"})))
