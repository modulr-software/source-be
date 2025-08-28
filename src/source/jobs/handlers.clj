(ns source.jobs.handlers
  (:require [source.services.interface :as services]
            [source.util :as util]))

(defmulti handler
  (fn [opts]
    (keyword (:handler opts))))

(defmethod handler :default [opts]
  (throw (IllegalArgumentException.
          (str "Handler with name " (:handler opts) " does not exist."))))

(defmethod handler :test [_]
  (fn [{:keys [args]}]
    (println "hello" (get args :name) args)))

(defmethod handler :update-feed-posts [_]
  (fn [{:keys [args ds store]}]
    (try
      (let [{:keys [feed-id creator-id content-type-id provider-id url]} args
            selection-schemas (->> [:= :provider-id provider-id]
                                   (assoc {} :where)
                                   (services/selection-schemas ds))
            latest-ss (->> selection-schemas
                           (reduce (fn [acc {:keys [id]}]
                                     (conj acc id)) [])
                           (apply max -1))
            extracted (services/extract-data store {:schema-id latest-ss
                                                    :url url})
            extracted-posts (get-in extracted [:feed :posts])
            extended-posts (mapv (fn [post]
                                   (merge post
                                          {:feed-id feed-id
                                           :creator-id creator-id
                                           :content-type-id content-type-id})) extracted-posts)
            existing-posts (services/incoming-posts ds {:where [:= :creator-id creator-id]})]
        (services/update-feed! ds {:id feed-id
                                   :data {:title (get-in extracted [:feed :title])
                                          :updated-at (util/get-utc-timestamp-string)}})
        (run!
         (fn [post]
           (if (some #(= (:post-id post) (:post-id %)) existing-posts)
             (services/update-incoming-post! ds {:where [:= :post-id (:post-id post)]
                                                 :data post})
             (services/insert-incoming-post! ds {:data post})))
         extended-posts))
      (catch Exception _ :fail))))

