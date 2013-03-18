(ns graaaph.core-test
  (:use clojure.test
        graaaph.core))

(def data {:simple          "def a;'ok';end"
           :file            (slurp "test/graaaph/testdata.rb")
           :class_variable  "class Dude; @@cool = \"dude\"; def speak; @@cool; end; end"
           :for_loop        "for i in (1..10) do; p i; end"
           :symbol_to_proc  "[1,2,3].map(&:to_s)"
           :simple_map      "[1,2,3].map{|x| x.to_s}"})

(deftest simple-parse-trest
  (testing "Basic def parse"
    (is (= (parse (:simple data))
           '({:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "ROOTNODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "NEWLINENODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "DEFNNODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 4, :end-offset 5}, :type "ARGUMENTNODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 6}, :type "ARGSNODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 11}, :type "NEWLINENODE"}
             {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 10}, :type "STRNODE"})))))
