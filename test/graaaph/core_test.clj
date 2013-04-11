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
  (let [ruby-data (parse-ruby-code ruby-code)
        vecs      (for [d ruby-data]
                    (into [] [(:name d) (:type d)]))
        results   (l/run* [q]
                    (l/fresh [list match name names]
                    (l/== list vecs)
                    (l/membero match list)
                    (l/matche [match]
                      ([[name "DEFNNODE"]] (l/== name names)))
                    (l/== q names)))]
     results))

(l/defne dupeo
   "A relation where l is a collection, such that x is removed unless
   it appears more than once in l."
   [l q]
     ([[_] _] l/#u)
     ([[x x] _])
     ([[x . tail] _]
      (l/fresh [qs]
        (l/membero qs tail)
        (dupeo tail qs))))
(l/run* [q]
   (l/fresh [l]
     (l/== l [1 3 3])
     (dupeo l q)))

(l/run* [q]
   (l/conde
     [(l/membero 4 q)

[1 1] [2 3 3 4]
[2 3] [3 4]
[3 4]

(get-duplicate-method-names ruby-code)

;; graaaph.core=> (get-method-names ruby-code)
;; ("awesome" "cool" "awesome")
