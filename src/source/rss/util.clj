(ns source.rss.util)

(defn stack-item
  "Stacks a value at key k from b to a. Stacking refers
   to taking values from b and associating them with a
   while preserving existing values for target keys by
   vectorizing them. E.g. {:tag \"a\"} merged with {:tag \"b\"}
   becomes {:tag [\"a\" \"b\"]}."
  [k a b]
  (let [val (k b) existing (k a)]
    (cond
      (and (nil? a) (nil? b)) nil
      (nil? a) b
      (nil? b) a
      (and (nil? val) (nil? existing)) a
      (nil? val) a
      (nil? existing) (assoc a k val)
      (and (vector? existing)
           (vector? val)) (assoc a k (vec (concat existing val)))
      (vector? existing) (assoc a k (vec (concat existing [val])))
      (vector? val) (assoc a k (vec (concat [existing] val)))
      :else (assoc a k (vec (concat [existing] [val])))
      )))

(comment
  (stack-item :tag nil nil)
  (stack-item :tag nil {:tag "b"})
  (stack-item :tag {:tag "a"} nil)
  (stack-item :tag {:tag nil} {:tag nil})
  (stack-item :tag {:tag "a"} {:tag nil})
  (stack-item :tag {:tag nil} {:tag "b"})
  (stack-item :tag {:tag "a"} {:tag "b"})
  (stack-item :tag {:tag ["a"]} {:tag "b"})
  (stack-item :tag {:tag ["a"]} {:tag ["b"]})
  (stack-item :tag {:tag "a"} {:tag ["b"]})
  (stack-item :tag {:tag "a"} {:tag "b"})
  )

(defn stack [a b]
  "Stacks all keys on objects a and b, vectorizing any common key values
   in the process."
  (cond
    (and (nil? a) (nil? b)) nil
    (nil? a) b
    (nil? b) a
    (and (map? a) (map? b)) (let [ks (vec (keys b))]
                              (reduce (fn [acc k]
                                        (stack-item k acc b))
                                      a ks))
    :else nil))

(comment
  (stack nil nil)
  (stack {:some nil} nil)
  (stack nil {:some nil})
  (stack {:some "a"} {:some "b"})
  (stack {:some ["a"]} {:some nil})
  (stack {:some nil} {:some ["b"]})
  (stack {:some ["a"]} {:some ["b"]})
  (stack {:some ["a"]} {:some "b"})
  )
