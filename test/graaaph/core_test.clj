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

(for [d (map #(:type %) (parse-ruby-code (:class-variable data)))
      :while (or (== (d "CLASSVARASGNNODE")) (== d "CLASSVARNODE"))] [])

  (into [] d))

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
  (let [ruby-data   (parse-ruby-code ruby-code)
        ast-as-vecs (for [d ruby-data]
                      (into [] [(:name d) (:type d)]))
        results     (l/run* [q]
                      (l/fresh [d n]
                        (l/== ast-as-vecs d)
                        (l/matche [d]
                          ([[name "DEFNNODE"]] (l/== name d)))
                        (dupeo d n)
                        (l/== n q)))]
    results))

(get-duplicate-method-names ruby-code)
