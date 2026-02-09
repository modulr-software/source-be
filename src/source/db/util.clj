(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn db-path [dbname]
  (let [db-dir (conf/read-value :database :dir)]
    (str
     db-dir
     (when (not (= (last db-dir) \/))
       "/")
     dbname)))

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(defn get-connection [ds]
  (let [conn (-> ds
                 (jdbc/get-connection))]
    (try (jdbc/execute! conn ["CREATE DOMAIN DATETIME TEXT"]) (catch Exception _))
    (jdbc/with-options conn {:builder-fn rs/as-unqualified-lower-maps})
    conn))

(defn- -conn [dbname]
  (-> {:dbtype (conf/read-value :database :type)
       :jdbcUrl (str "jdbc:" (conf/read-value :database :url) "/" dbname)
       :maximum-pool-size 10}))

(defn conn
  ([]
   (conn :master))
  ([db-type]
   (assert (= db-type :master))
   (-conn (db-name db-type)))
  ([db-type id]
   (assert (or (= db-type :bundle) (= db-type :creator)))
   (-conn (db-name db-type id))))

(defn tname
  ([tname id]
   {:tname (->> (str (name tname) "-" id)
                (keyword))})
  ([data-map tname id]
   (->> (str (name tname) "-" id)
        (keyword)
        (assoc data-map :tname))))

(defn tnames [tnames id]
  (mapv #(tname % id) tnames))

(comment
  (def ds (conn :bundle 1))
  (jdbc/execute! ds ["DELETE FROM providers;"])
  ())
