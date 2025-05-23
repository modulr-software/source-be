(ns source.prandom.core)

(defn seeded-shuffle
  "Generates a pseudo randomly shuffled list of length ```length```
  within range 0, ```length``` - 1 using seed.
  
  The seed can be any clojure datatype.

  Note seeds that are vectors, lists or lazy sequences will are not unique.
  If the content of any of these are the same the seed will be the same.
  
  i.e. seed '(1 1 0) and [1 1 0] will be the same."
  [length seed]
  (let [alist (java.util.ArrayList. (range length))
        rng (java.util.Random. (hash seed))]
    (java.util.Collections/shuffle alist rng)
    (vec alist)))

(comment
  (do
    (println "Generated shuffles are equal when length and seed are the same")
    (assert (= (seeded-shuffle 10 "aseed") (seeded-shuffle 10 "aseed")))
    (println "Test passed")
    (println "Generated shuffles are not equal when the seed is different even when length is the same")
    (assert (not= (seeded-shuffle 10 1) (seeded-shuffle 10 "1")))
    (assert (not= (seeded-shuffle 10 2) (seeded-shuffle 10 3)))
    (assert (not= (seeded-shuffle 10 "aseed") (seeded-shuffle 10 "anotherseed")))
    (println "Test passed")
    (println "Generated shuffles are not the same when the seed is the same and length is different")
    (assert (not= (seeded-shuffle 10 "aseed") (seeded-shuffle 20 "aseed")))
    (println "Test passed")))
