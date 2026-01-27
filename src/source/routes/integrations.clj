(ns source.routes.integrations
  (:require [ring.util.response :as res]
            [source.services.bundles :as bundles]
            [source.workers.integrations :as integrations]))

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
  (->> {:user-id (:id user)
        :bundle-metadata (dissoc body :categories :content-types)
        :categories (:categories body)
        :content-types (:content-types body)}
       (integrations/create-integration! ds js store)
       (res/response)))
