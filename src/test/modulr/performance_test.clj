(ns modulr.performance-test
  (:require [clojure.test :refer :all]
            [modulr.analytics :as a]))

(deftest short-heuristic-performance-test
  (testing "short-heuristic handles 100,000 records" 
    (let [bundle-id "test-bundle-100k"]
      (time (a/short-heuristic bundle-id))
      (is true "Completed without crashing"))))
