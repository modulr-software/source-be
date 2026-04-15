(ns source.services.jobs
  (:require [source.db.interface :as db]))

(defn insert-job! [ds {:keys [data ret] :as opts}]
  (->> {:tname :jobs
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-job! [ds {:keys [id data where] :as opts}]
  (->> {:tname :jobs
        :values data
        :where (if (some? id) [:= :id id] where)}
       (merge opts)
       (db/update! ds)))

(defn delete-job! [ds {:keys [id where] :as opts}]
  (->> {:tname :jobs
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn jobs
  ([ds] (jobs ds {}))
  ([ds opts]
   (->> {:tname :jobs
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn job [ds {:keys [id where] :as opts}]
  (->> {:tname :jobs
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn insert-job-metadata! [ds {:keys [data ret] :as opts}]
  (->> {:tname :job-metadata
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-job-metadata! [ds {:keys [id data where] :as opts}]
  (->> {:tname :job-metadata
        :values data
        :where (if (some? id) [:= :id id] where)}
       (merge opts)
       (db/update! ds)))

(defn delete-job-metadata! [ds {:keys [id where] :as opts}]
  (->> {:tname :job-metadata
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn job-metadata [ds {:keys [id where] :as opts}]
  (->> {:tname :job-metadata
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
