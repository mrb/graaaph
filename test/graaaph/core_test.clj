(ns graaaph.core-test
  (:use clojure.test
        graaaph.core))

(def test-data {:simple          "def a;'ok';end"
                :args            "class Dude; def initialize(cool=nil); @cool = cool; end; end; d = Dude.new(:cool);"
                :class-variable  "class Dude; @@cool = \"dude\"; def speak; @@cool; end; end"
                :dup-method      "class Dude; def a; \"first\"; end; def a; \"second\"; end; end"
                :for-loop        "for i in (1..10) do; p i; end"
                :symbol-to_proc  "[1,2,3].map(&:to_s)"
                :simple-map      "[1,2,3].map{|x| x.to_s}"
                :openstruct      "OpenStruct.new"
                :add             "1+1"})

(deftest simple-parse
  (is (= '({:type "ROOTNODE", :value nil, :name nil}
           {:type "NEWLINENODE", :value nil, :name nil}
           {:type "DEFNNODE", :value nil, :name "a"}
           {:type "ARGUMENTNODE", :value nil, :name "a"}
           {:type "ARGSNODE", :value nil, :name nil}
           {:type "NEWLINENODE", :value nil, :name nil}
           {:type "STRNODE", :value nil, :name nil})
           (parse-ruby-code (:simple test-data)))))
