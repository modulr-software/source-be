(ns source.rss.core
  (:require [source.rss.youtube :as yt]
            [hickory.core :as h]
            [hickory.select :as s]
            [clojure.data.json :as json]))

(defn get-url []
  (->> "https://www.youtube.com/@ThePrimeTimeagen"
       (yt/find-channel-id)
       (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))

(defn get-xml []
  (let [rss-url (get-url)]
    (slurp rss-url)))

(defn get-ast [xml]
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

(defn make-attr-extractor
  "this returns a function that, takes a hickory tree
   and selects the element at tag-path and returns its
   attribute named attr-keyword. returns nil if the attribute
   is not found"
  [tag-path attr-keyword]
  (let [selector (build-child-selector tag-path)]
    (fn extract-attr [htree]
      (-> (s/select selector htree)
          first
          :attrs
          (get attr-keyword)))))

(defn make-content-extractor
  "this returns a function that will take a hickory tree
  and will return the n-th value in the content vector
  for the node found at tag-path"
  [tag-path idx]
  (let [selector (build-child-selector tag-path)]
    (fn extract-content [htree]
      (let [node (first (s/select selector htree))]
        (nth (:content node) idx)))))

(defn collect-paths
  ([root-node] (collect-paths root-node {} []))
  ([root-node acc current-path]))

(defn run-attr-extractors [extractor-map htree]
  (reduce-kv
   (fn [acc _ extractor-fn]
     (conj acc (extractor-fn htree)))
   []
   extractor-map))

(def xml-ast
  (get-ast (get-xml)))

(def other-one
  (get-ast "<items>
    <item id=1>
           <link href=www.youtube.com></link>
           <thing>Thing 1</thing>
    </item>
    <item>Item 2</item>
</items>"))

(def stuff (json/write-str (collect-leaf-paths other-one)))

(spit "test.json" stuff)

(def sample-feed
  "<rss version=\"2.0\" xmlns:yt=\"http://www.youtube.com/xml/schemas/2015\" xmlns:media=\"http://search.yahoo.com/mrss/\">
  <channel>
    <title>ExampleChannel</title>
    <link>https://www.youtube.com/channel/UC1234567890</link>
    <item>
      <title name=\"something\">Video One</title>
      <link>https://www.youtube.com/watch?v=VID1</link>
      <guid isPermaLink=\"false\">yt:video:VID1</guid>
    </item>
    <item>
      <title name=\"something else\">Video Two</title>
      <link>https://www.youtube.com/watch?v=VID2</link>
      <guid isPermaLink=\"false\">yt:video:VID2</guid>
    </item>
  </channel>
</rss>")

(def ANOTHER-ONE
  (get-ast sample-feed))

(def items (-> (s/select (s/tag :items) other-one)
               (first)))
items

(def item (-> (s/select (s/tag :item) items)))
item

(def items (s/select (s/child
                      (s/tag :items)
                      (s/tag :item)) other-one))

(def paths (collect-leaf-paths ANOTHER-ONE))
paths

(def FUCK (second (first paths)))
(FUCK ANOTHER-ONE)

(run-attr-extractors paths ANOTHER-ONE)
; ["https://www.youtube.com/channel/UC1234567890"
;  "yt:video:VID1"
;  "\n      "
;  "http://www.youtube.com/xml/schemas/2015"
;  "false"
;  "\n    "
;  "Video One"
;  "Video One"
;  "yt:video:VID1"
;  "2.0"
;  "\n  "
;  "https://www.youtube.com/watch?v=VID1"
;  "http://search.yahoo.com/mrss/"
;  "\n"
;  "ExampleChannel"
;  "https://www.youtube.com/watch?v=VID1"]
