(ns graaaph.core-examples
  (:use graaaph.core)
  (:require [clojure.core.logic :as l]))

(def data {:simple          "def a;'ok';end"
           :args            "class Dude; def initialize(cool=nil); @cool = cool; end; end; d = Dude.new(:cool);"
           :class-variable  "class Dude; @@cool = \"dude\"; def speak; @@cool; end; end"
           :dup-method      "class Dude; def a; \"first\"; end; def a; \"second\"; end; end"
           :for-loop        "for i in (1..10) do; p i; end"
           :symbol-to_proc  "[1,2,3].map(&:to_s)"
           :simple-map      "[1,2,3].map{|x| x.to_s}"
           :openstruct      "OpenStruct.new"
           :add             "1+1"})

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

(view-ruby-ast ruby-code)

(defn get-duplicate-method-names [ruby-code]
  (let [nodes     (parse-ruby-code ruby-code)
        defnnodes (l/run* [q]
                    (l/fresh [a b c d n e]
                      (l/== a nodes)
                      (nodetypeo a "DEFNNODE" c)
                      (nodenameo c n e)
                      (l/== e q)))]
    defnnodes))

(get-duplicate-method-names ruby-code)

(ruby-code-zipper ruby-code)

(zipper-to-source (ruby-code-zipper ruby-code))

(->
  (ruby-code-zipper (:add data))
  z/down
  z/down
  z/down
  z/node)

(parse-ruby-code (:add data))

(def n (->
  (ruby-code-zipper "1")
  z/next
  z/next
  z/node))

(l/run* [q]
   (l/fresh [n t]
      (l/== n (parse-ruby-code ruby-code))
      (nodenameo n "awesome" q)))

(l/run* [q]
   (l/fresh [n t]
      (l/== n seq-of-maps)))

(l/run* [q]
   (l/fresh [n t]
      (l/== n (parse-ruby-code ruby-code))
      (nodenameo n q t)))

(l/run* [q]
   (l/fresh [n t]
     (l/== n (parse-ruby-code ruby-code))
     (nodetypeo n "DEFNNODE" q)))
