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

(defmethod handler :update-feed-post [_]
  (fn [{:keys [args ds store]}]
    (try
      (let [{:keys [feed-id post-id schema-id url]} args
            extracted (services/extract-data store {:schema-id schema-id
                                                    :url url})]
        (services/update-feed! ds {:id feed-id
                                   :data {:updated-at (util/get-utc-timestamp-string)}})
        (services/update-incoming-post! ds {:id post-id
                                            :data extracted}))
      (catch Exception _ :fail))))

