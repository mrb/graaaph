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
                end")

(defn get-duplicate-method-names [ruby-code]
  (let [ruby-data   (parse-ruby-code ruby-code)
        ast-as-vecs (for [d ruby-data]
                      (into [] [(:name d) (:type d)]))
        results     (seq (into #{}
                      (l/run* [q]
                        (l/fresh [all-nodes matched-nodes name match dupe-nodes results]
                          (l/== all-nodes ast-as-vecs)
                          (dupeo all-nodes dupe-nodes)
                          (l/matche [dupe-nodes]
                            ([[name "DEFNNODE"]] (l/== name matched-nodes)))
                          (l/==  matched-nodes q)))))]
    results))

(get-duplicate-method-names ruby-code)
