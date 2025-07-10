(ns source.routes.user
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (let [user (->> path-params
                  (services/user ds))]
    (res/response (assoc {} :user (dissoc user :password)))))

(def get-parameters {:path [:map [:id {:title "id"
                                       :description "user id"} :int]]})

(def get-responses {200 {:body [:map
                                [:user
                                 [:map
                                  [:id :int]
                                  [:address {:optional true} :string]
                                  [:profile-image {:optional true} :string]
                                  [:email :string]
                                  [:firstname {:optional true} :string]
                                  [:lastname {:optional true} :string]
                                  [:type [:enum "creator" "distributor" "admin"]]
                                  [:email-verified {:optional true} :int]
                                  [:onboarded {:optional true} :int]
                                  [:mobile {:optional true} :string]]]]}
                    401 {:body [:map [:message :string]]}
                    403 {:body [:map [:message :string]]}})

(defn patch [{:keys [ds body path-params] :as _request}]
  (services/update-user! ds
                         {:id (:id path-params)
                          :values body})
  (res/response {:message "successfully updated user"}))

(def patch-parameters {:path [:map [:id {:title "id"
                                         :description "user id"} :int]]
                       :body [:map
                              [:address {:optional true} :string]
                              [:profile-image {:optional true} :string]
                              [:email :string]
                              [:firstname {:optional true} :string]
                              [:lastname {:optional true} :string]
                              [:type [:enum "creator" "distributor" "admin"]]
                              [:email-verified {:optional true} :int]
                              [:onboarded {:optional true} :int]
                              [:mobile {:optional true} :string]]})

(def patch-responses {200 {:body [:map
                                  [:user
                                   [:map
                                    [:id :int]
                                    [:address {:optional true} :string]
                                    [:profile-image {:optional true} :string]
                                    [:email :string]
                                    [:firstname {:optional true} :string]
                                    [:lastname {:optional true} :string]
                                    [:type [:enum "creator" "distributor" "admin"]]
                                    [:email-verified {:optional true} :int]
                                    [:onboarded {:optional true} :int]
                                    [:mobile {:optional true} :string]]]]}
                      401 {:body [:map [:message :string]]}
                      403 {:body [:map [:message :string]]}})

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master) :path-params {:id 5}})
  (patch {:ds (db/ds :master)
          :path-params {:id 5}
          :body {:firstname "kiigan"
                 :lastname "korinzu"}})
  ())
