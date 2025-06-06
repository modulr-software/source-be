(ns migrations.001-create-authors-table)

(defn run-up! [context]
  (println "going up"))

(defn run-down! [context]
  (println "going down"))
