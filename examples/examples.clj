(ns graaaph.core-examples
  (:use graaaph.core))

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
  (let [nodes   (parse-ruby-code ruby-code)
        results (l/run* [q]
                  (l/fresh [a b c d]
                    (l/== a nodes)
                    (nodetypeo a "DEFNNODE" c)
                    (dupeo c d)
                    (l/== q d)))]
        results))

(get-duplicate-method-names ruby-code)

(def o
  (let [writer (StringWriter.)
        node (-> (ruby-code-zipper ruby-code) z/next z/next first)]
    (.accept node (ReWriteVisitor. writer "(string)"))
    writer))
