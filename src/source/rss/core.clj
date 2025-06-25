(ns source.rss.core
  (:require [source.rss.youtube :as yt]
            [hickory.core :as h]
            [hickory.select :as s]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.pprint :as pprint]))

(defn get-ast
  "Constructs a hickory tree representation from an xml string."

  [xml]
  (-> xml h/parse h/as-hickory))

(defn collect-leaf-paths
  "This function does a DFS on a hickory tree and assigns paths to each node relative to the root and
  then returns the ast with paths.
  
  Paths are made up of a sequence of tag names."
  ([root-node]
   (collect-leaf-paths root-node []))
  ([node current-path]
   (if (= (type node) java.lang.String)
     node

     (let [{:keys [tag content]} node
           new-path (if tag
                      (conj current-path tag)
                      current-path)
           content-with-paths (when content
                                (let [nodes (if (> (count content) 1)
                                              (filterv #(not= (type %) java.lang.String) content)
                                              content)]
                                  (mapv #(collect-leaf-paths % new-path) nodes)))]
       (-> node
           (assoc :path new-path)
           (dissoc :content)
           (assoc :content content-with-paths))))))

(defn build-child-selector
  "given a sequence of tag keywords this will return a hickory selector
  that hickory.select/select can use to get the node you want"
  [tags]
  (let [ts (map keyword tags)
        root (first tags)]
    (reduce (fn [sel tag]
              (s/child sel (s/tag tag)))
            (s/tag root)
            (rest ts))))

(defn split
  "splits path segment into [namespace name]"
  [path-segment]
  (let [path-keyword (keyword path-segment)]
    (mapv
     #(keyword %)
     [(namespace path-keyword)
      (name path-keyword)])))

(defn extract-data
  "Recursively extracts data from a hickory xml tree according to the selection schema."
  [schema ast]
  (reduce-kv
   (fn [result field {:keys [type path schema]}]
     (let [[nmspc nm] (split (last path))
           selector-path (if (= :attr nm) (butlast path) path)
           selector (build-child-selector selector-path)]

       (cond
         (= nmspc :attr)
         (assoc result field
                (-> (s/select selector ast)
                    first
                    :attrs
                    (get nm)))

         (= type "string")
         (assoc result field
                (-> (s/select selector ast)
                    first
                    :content
                    first))

         (= type "map")
         (let [child-node (first (s/select selector ast))]
           (assoc result field
                  (extract-data schema child-node)))

         (= type "vector")
         (let [child-nodes (s/select selector ast)]
           (assoc result field
                  (mapv #(extract-data schema %) child-nodes)))

         :else
         result)))
   {}
   schema))
