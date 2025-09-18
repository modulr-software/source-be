(ns source.routes.integrations
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.util :as utils]
            [source.migrate :as migrate]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]))

(defn get
  {:summary "get all integrations"
   :responses {200 {:body [:vector
                           [:map
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

  [{:keys [ds] :as _request}]
  (res/response (services/bundles ds)))

(defn post
  {:summary "add an integration"
   :parameters {:body [:map
                       [:name :string]
                       [:content-type-id :int]
                       [:ts-and-cs {:optional true} :int]
                       [:categories [:vector
                                     [:map
                                      [:id :int]
                                      [:name :string]]]]]}
   :responses {201 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [js ds store user body] :as _request}]
  (let [new-bundle (services/insert-bundle! ds {:data (merge
                                                       (dissoc body :categories)
                                                       {:user-id (:id user)
                                                        :uuid (utils/uuid)})
                                                :ret :1})
        update-data (reduce (fn [acc {:keys [id]}]
                              (conj acc {:bundle-id (:id new-bundle)
                                         :category-id id})) [] (:categories body))
        ; insert bundle categories
        _ (services/insert-bundle-category! ds {:data update-data})
        categories-by-bundle (services/categories-by-bundle ds {:bundle-id (:id new-bundle)})]

    (migrate/migrate-bundle (:id new-bundle) ["up"])

    (->> (jobs/prepare-congest-metadata
          ds
          store
          {:id (str "bundle_" (:id new-bundle))
           :initial-delay #_(* 1000 60 60 24) 0
           :auto-start true
           :stop-after-fail false,
           :interval #_(* 1000 60 60 24) (* 1000 60)
           :recurring? true
           :ds ds
           :args {:bundle-id (:id new-bundle)
                  :categories categories-by-bundle}
           :handler :update-bundle
           :created-at (utils/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))

    (res/response {:message "successfully added integration"})))

