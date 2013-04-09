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


(parse (:dup-method data))

(defn get-name [name coll]
  (for [d coll
        :when (and
                (not (nil? (:name d)))
                (= (:name d) name))]
      d))
(get-name "a" (parse (:dup-method data)))

(group-by identity (parse (:dup-method data)))

(let [ruby-data (parse "class Dude
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
                       end")]
  (->>
    ruby-data
    (filter
      #(and
         (not (nil? (:name %)))
         (= "DEFNNODE" (:type %))))
    (group-by :name)
    (filter #(> (count (second %)) 1))))

    (filter #(> (count (second %)) 1))))

(let [filtered-list (for [dat ruby-data] {:type (:type dat) :name (:name dat)})
      names (distinct (map #(:name %) filtered-list))
      types (distinct (map #(:type %) filtered-list))]
  (l/run* [q]
    (l/fresh [list name type vals]
      (l/== list filtered-list)
      (l/membero name names)
      (l/membero type types)
      (l/membero vals list)
      (l/== {:type "DEFNNODE" :name name} vals)
      (l/== q vals))))

(let [filtered-list (for [dat ruby-data] [(:type dat) (:name dat)])]
  (l/run* [q]
    (l/fresh [list node e f]
      (l/== list filtered-list)
      (l/membero q list)
      (l/firsto q "DEFNNODE"))))

(let [ruby-data (parse "class Dude
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
      results (l/run* [q]
                  (l/fresh [list node e f]
                  (l/== list ruby-data)
                  (l/membero q list)
                  (l/matche [e]
                    ([{:position _ :name f :type "DEFNNODE" :value _}] (l/== e q)))))]
      (->>
        results
        (group-by :name)))
