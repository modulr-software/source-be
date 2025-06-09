(ns source.rss.core
  (:require [source.rss.youtube :as yt]
            [hickory.core :as h]
            [hickory.select :as s]))

(defn get-url []
  (->> "https://www.youtube.com/@ThePrimeTimeagen"
       (yt/find-channel-id)
       (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))

(defn get-xml []
  (let [rss-url (get-url)]
    (println rss-url)
    (slurp rss-url)))

(defn get-ast [xml]
  (-> xml h/parse h/as-hickory))

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

(defn collect-leaf-paths
  "this function does a DFS on a hickory tree and generates a map where the key is the value
  and the value is the selector that will return that value when it is run on a hickory tree"
  ([root-node]
   (collect-leaf-paths root-node [] (array-map)))
  ([node current-path acc]
   (let [{:keys [tag attrs content]} node
         new-path (if tag
                    (conj current-path tag)
                    current-path)
         acc-with-attrs (reduce (fn [result-map [attr-name attr-val]]
                                  (assoc result-map
                                         attr-val
                                         (make-attr-extractor new-path (keyword attr-name))))
                                acc
                                attrs)
         acc-with-content (reduce-kv (fn [result-map idx slot]
                                       (cond
                                         (string? slot)
                                         (assoc result-map
                                                slot
                                                (make-content-extractor new-path idx))

                                         (map? slot)
                                         (collect-leaf-paths slot new-path result-map)

                                         :else
                                         result-map))
                                     acc-with-attrs
                                     content)]
     acc-with-content)))

(defn run-attr-extractors [extractor-map htree]
  (reduce-kv
   (fn [acc _ extractor-fn]
     (conj acc (extractor-fn htree)))
   []
   extractor-map))

(def xml-ast
  (get-ast (get-xml)))

(def other-one
  (get-ast "<rss version=\"2.0\">
  <channel lang=\"en\">
    <title id=\"2\">Mini Feed</title>
    <item id=\"1\">
      <title>Post One</title>
      <link>https://example.com/2</link>
    </item>
  </channel>
</rss>"))

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

(s/select (s/child
           (s/tag :channel)
           (s/tag :item)
           (s/attr :name)) ANOTHER-ONE)

(def paths (collect-leaf-paths ANOTHER-ONE))
paths
; {"https://www.youtube.com/channel/UC1234567890"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "yt:video:VID2"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "\n      "
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "http://www.youtube.com/xml/schemas/2015"
;  #function[source.rss.core/make-attr-extractor/extract-attr--11136],
;  "false"
;  #function[source.rss.core/make-attr-extractor/extract-attr--11136],
;  "\n    "
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "Video One"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "Video Two"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "yt:video:VID1"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "2.0"
;  #function[source.rss.core/make-attr-extractor/extract-attr--11136],
;  "\n  "
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "https://www.youtube.com/watch?v=VID1"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "http://search.yahoo.com/mrss/"
;  #function[source.rss.core/make-attr-extractor/extract-attr--11136],
;  "\n"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "ExampleChannel"
;  #function[source.rss.core/make-content-extractor/extract-content--11139],
;  "https://www.youtube.com/watch?v=VID2"
;  #function[source.rss.core/make-content-extractor/extract-content--11139]}
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
