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
  (l/== q (parse-ruby-code ruby-code)))

;; nodeattro now works on *one* node
(l/run* [q]
  (l/fresh [node]
    (l/== node (first (parse-ruby-code ruby-code)))
    (nodeattro node :name q)))

;; get only the names w/ mapo
(l/run* [q]
  (l/fresh [nodes]
    (l/== nodes (parse-ruby-code ruby-code))
    (mapo nodes #(nodeattro %1 :name %2) q)))

;; filter out nodes w/o names
(l/run* [q]
  (l/fresh [nodes]
    (l/== nodes (parse-ruby-code ruby-code))
    (filtero nodes
      (fn [node]
        (l/fresh [value]
          (nodeattro node :name value)
          (l/!= value nil)))
      q)))

;; extract names
(l/run* [q]
  (l/fresh [nodes nodes']
    (l/== nodes (parse-ruby-code ruby-code))
    (filtero nodes
      (fn [node]
        (l/fresh [value]
          (nodeattro node :name value)
          (l/!= value nil)))
      nodes')
    (mapo nodes' #(nodeattro %1 :name %2) q)))

