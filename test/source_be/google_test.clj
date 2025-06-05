(ns source-be.google-test
  (:require [clojure.test :refer :all]))

(deftest yeet-test
  (testing "adding numbers together"
    (with-redefs [])
    (is (= 69 (+ 60 9)))))

(run-all-tests)
(run-tests)
