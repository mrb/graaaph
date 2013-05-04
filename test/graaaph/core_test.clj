(ns graaaph.core-test
  (:use clojure.test
        graaaph.core))

(def data {:simple          "def a;'ok';end"
           :file            (slurp "test/graaaph/testdata.rb")
           :args            "class Dude; def initialize(cool=nil); @cool = cool; end; end; d = Dude.new(:cool);"
           :class-variable  "class Dude; @@cool = \"dude\"; def speak; @@cool; end; end"
           :dup-method      "class Dude; def a; \"first\"; end; def a; \"second\"; end; end"
           :for-loop        "for i in (1..10) do; p i; end"
           :symbol-to_proc  "[1,2,3].map(&:to_s)"
           :simple-map      "[1,2,3].map{|x| x.to_s}"
           :openstruct      "OpenStruct.new"
           :add             "1+1"})


(parse-ruby-code (:dup-method data))

(def ruby-code "class Dude
                  def awesome
                    'first awesome'
                  end
                  #
                  def cool
                    'not awesome'
                  end
                  #
                  def awesome
                    'second awesome'
                  end
                  #
                  def bro
                  end
                  #
                  def bro
                  end
                end")

(defn get-duplicate-method-names [ruby-code]
  (let [ruby-data (parse-ruby-code ruby-code)
        ast-as-list (for [d ruby-data
                           :when (and (seq (:name d))
                                      (= "DEFNNODE" (:type d)))]
                      [(:name d) (:type d)])
        results     (l/run* [q]
                      (l/fresh [ls dupes]
                        (l/== ls ast-as-list)
                        (dupeo ls dupes)
                        (l/== dupes q)))]
    results))

(get-duplicate-method-names ruby-code)
(parse-ruby-code ruby-code)

(def ruby-data [{:a 1 :b 2} {:a 1 :b 3}])
  (l/run* [q]
    (l/fresh [a b n]
      (l/== a ruby-data)
      (l/matche [a]
        ([[_ . {:a _ :b n}]] (l/== n b))
        ([[{:a _ :b n} . _]] (l/== n b)))
      (l/== b q)))

{:position {:file "" :start-line 0 :end-line 18 :start-offset 0 :end-offset 466} :type "ROOTNODE" :value nil :name nil}
