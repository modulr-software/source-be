(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [pg.core :as pg]))

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
  {:connection-uri (str (conf/read-value :database :url) ":5432/" dbname)})

(defn conn
  ([]
   (conn :master))
  ([db-type]
   (assert (or (= db-type :master) (= db-type :migrate)))
   (-conn (db-name db-type))))

(defn tname
  ([tname id]
   {:tname (->> (str (name tname) "-" id)
                (keyword))})
  ([data-map tname id]
   (->> (str (name tname) "-" id)
        (keyword)
        (assoc data-map :tname))))

(defn tnames [tnames id]
  (mapv #(:tname (tname % id)) tnames))

(defn conn-env 
  "Creates a connection to the master database for the given environment. There must be a connection string in config for the given environment."
  [env]
  {:connection-uri (str (conf/read-value :database env) "/master")})

(defmacro with-env 
  "This macro creates a let binding structure associating a custom binding with a database connection based with the given environment as a keyword.
  e.g. (with-env [ds :staging] (hon/find ds {:tname :users}))"
  [args & body]
  `(let [~(first args) (conn-env ~(last args))]
     ~(cons 'do body)))

(comment
  (def q "SELECT * FROM events")

  (macroexpand '(with-env [ds :staging] (println ds)))

  (with-env [ds :staging]
    (time (pg/with-conn [conn ds]
            (pg/query conn q)))
    #_(hon/find ds {:tname :users}))

  (time (pg/with-conn [conn {:connection-uri "postgresql://postgres:postgres@localhost:5432/master?ssl=false"}]
          (pg/query conn q)))

  (time (jdbc/execute! {:dbtype "postgresql"
                        :jdbcUrl (str "jdbc:" (conf/read-value :database :url) ":5432/master")} [q]))

  ())
