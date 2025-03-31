(ns source.db.query
  (:require [clojure.string :as s]))

(def ^:private param-regex #"\?([^\s,]+)")
(defn params
  "Takes a query with variable syntax ?var, and extracts a
   vector of symbols that match the names of vars. return []
   when no vars are present."
  [q]
  (->>
   (re-seq param-regex q)
   (map #(second %))
   (vec)
   (mapv #(symbol %))))

(defn sql
  "Takes a query with variable syntax ?var, and returns a
   valid sql string where the vars are replaced with just
   ?."
  [q]
  (clojure.string/replace q param-regex "?"))

(defn prep-stmt
  "Takes a query with variable syntax ?var, and returns a
   statement of the form [sql & vars], where vars are symbol
   versions of the parameters found in the query. This fn is
   mainly used in the definition of the defquery macro."
  [q]
  (->
   (concat
    [(sql q)]
    (params q))
   (vec)))

(defmacro defquery
  "Defines a fn using defn where the parameters match
   those defined in q. The created function can be invoked
   and executes the sql equivalent of the query provided."
  {:clj-kondo/lint-as 'clojure.core/def
   :clj-kondo/ignore [:uninitialized-var]}
  [fname q dbname]
    `(defn ~fname ~(params q)
         (next.jdbc/execute!
          (source.db.util/conn ~dbname)
          ~(prep-stmt q))
       )
  )

(comment
  (defquery all-users "select * from ?table where id = ?id" "master")
  (defn all-users [table id]
    (next.jdbc/execute!
     (soruce.db.util/conn "master")
     ["select * from users where id = ?" table id]))
  )
