(ns source.rss.core
  (:require [hickory.core :as h]
            [hickory.select :as s]))

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
                                (let [nodes (if (> (count content) 1) ;; filter out erroneous string children
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
  (let [ts (mapv #(last %) tags)
        root (first ts)]
    (reduce (fn [sel tag]
              (s/child sel (s/tag tag)))
            (s/tag root)
            (rest ts))))

(defn split
  "splits path segment into [namespace name]
  i.e. \"attr/attr-name\" becomes [:attr :attr-name]"
  [path-segment]
  (let [path-keyword (keyword path-segment)]
    (mapv
     #(keyword %)
     [(namespace path-keyword)
      (name path-keyword)])))

(defn leaf?
  "Returns true if :seg-type from namespaced keyword path segment's type is content or attribute."
  [kw-path-seg]
  (let [seg-type (first kw-path-seg)]
    (or (= seg-type :content) (= seg-type :attr))))

(defn extract-leaf
  "Get's the content from a leaf node using the leaf path segment."
  [node kw-path-seg]
  (let [[seg-type seg-val] kw-path-seg]
    (if (= seg-type :attr)
      (-> (:attrs node)
          (get seg-val))

      (-> (:content node)
          (get (Integer/parseInt (name seg-val)))))))

(defn extract-data
  "Recursively extracts data from a hickory xml tree according to the input selection schema.

  The input selection schema contains paths to each field in the schema. These paths are relative to their parent node
  with any number of tag/tag-name segments and end with a attr/attribute-name or content/n-th segment if the field is a string."
  [schema ast]
  (reduce-kv
   (fn [result field {:keys [type path schema]}]
     (let [keyword-path (mapv #(split %) path)
           is-leaf? (leaf? (last keyword-path))
           selector-path (if is-leaf? (butlast keyword-path) keyword-path)
           selector (build-child-selector selector-path)]

       (cond
         (= type "map")
         (let [child-node (first (s/select selector ast))]
           (assoc result field
                  (extract-data schema child-node)))

         (= type "vector")
         (let [child-nodes (s/select selector ast)]
           (assoc result field
                  (mapv #(extract-data schema %) child-nodes)))

         is-leaf?
         (assoc result field
                (-> (s/select selector ast)
                    first
                    (extract-leaf (last keyword-path))))

         :else
         result)))
   {}
   schema))

(comment
  (let [ast (get-ast (slurp "https://www.youtube.com/feeds/videos.xml?channel_id=UCWI-ohtRu8eEeDj93hmUsUQ"))
        schema {:title
                {:type "string",
                 :required true,
                 :path ["html" "body" "feed" "title" "content/0"]},
                :url
                {:type "string",
                 :required true,
                 :path ["html" "body" "feed" "author" "uri" "content/0"]},
                :posts
                {:type "vector",
                 :required true,
                 :schema
                 {:title {:type "string", :required true, :path ["title" "content/0"]},
                  :stream-url
                  {:type "string", :required true, :path ["link" "attr/href"]},
                  :description
                  {:type "string",
                   :required false,
                   :path ["media:group" "media:description" "content/0"]},
                  :posted-at {:type "string", :required false, :path ["published" "content/0"]}},
                 :path ["html" "body" "feed" "entry"]}}]
    (extract-data schema ast)))
