(ns graaaph.core
  (:import (org.jrubyparser Parser
                            CompatVersion)
           (org.jrubyparser.parser ParserConfiguration)
           (org.jrubyparser.ast Node)
           (java.io.StringReader))
  (:require [clojure.zip :as z]
            [clojure.core.logic :as l]
            [clojure.algo.monads :as m
              :only (domonad maybe-m)]))

(defn parse-ruby [ruby-string]
  (let [config (ParserConfiguration. 0 (CompatVersion/RUBY1_9))
        reader (java.io.StringReader. ruby-string)
        parser (Parser.)]
    (.parse parser "" reader config)))

(defn can-have-children? [nodes]
  (instance? Node nodes))

(defn code-zipper [nodes]
  (z/zipper can-have-children? get-children make-node nodes))

(defn make-node [_ c]
  c)

(defn tree-match [zipper matcher]
  (loop [loc zipper]
    (if (z/end? loc)
      []
      (if-let [matcher-result (matcher loc)]
        loc
        (recur (z/next loc))))))

(defn tree-visitor [zipper visitor]
  (let [coll '()]
    (loop [loc zipper]
      (if (z/end? loc)
        coll
        (if-let [visitor-result (visitor loc)]
          (do
            (if-let
              [good-node (z/node loc)]
                (do
                  (println good-node)))
            (recur (z/next loc)))))))) ;; visitor-result

(tree-visitor zipped-code #(-> % .))

(defn make-matcher [nodetype]
  (fn [node]
    (let [znode (z/node node)]
      (and
        (not (nil? znode))
        (= (str (.getNodeType znode))
           nodetype)))))

(def newline-matcher (make-matcher "NEWLINENODE"))

(def fornode-matcher (make-matcher "FORNODE"))

(tree-match zipped-code newline-matcher)

(tree-match zipped-code fornode-matcher)

(defn get-location [node]
  (-> node
      .getPosition))

(defn tree-match-with-position [zipper matcher]
  (-> (tree-match zipper matcher)
      z/node
      .getPosition))

(tree-match-with-position zipped-code fornode-matcher)

(tree-to-vec zipped-code)

(def data (slurp "test/graaaph/testdata.rb"))

(def data "class Dude; @@cool = \"dude\"; def speak; @@cool; end; end ;")

; class Dude
;   @@cool = "dude"
;
;   def speak
;     @@cool
;   end
; end
;
; (RootNode,
;   (NewlineNode,
;     (ClassNode,
;       (Colon2ImplicitNode:Dude),
;       (BlockNode,
;         (NewlineNode,
;           (ClassVarAsgnNode:@@cool, (StrNode))),
;         (NewlineNode,
;           (DefnNode:speak,
;             (ArgumentNode:speak),
;             (ArgsNode),
;             (NewlineNode,
;               (ClassVarNode:@@cool))))))))>

(def data "for i in (1..10) do; p i; end")

; Find Class Variables
; Find for loops

(def c (parse-ruby data))

(def zipped-code (code-zipper c))

(def flattenize
     (fn [tree]
       (letfn [(flatten-zipper [so-far zipper]
                 (println so-far)
                 (cond (z/end? zipper)
                       so-far
                       ;(z/branch? zipper)
                       ;(flatten-zipper so-far (z/next zipper))
                       :else
                       (flatten-zipper (cons (z/node zipper) so-far)
                                       (z/next zipper))))]
         (reverse (flatten-zipper '() tree)))))

(def f (flattenize zipped-code))

(filter #(not (= '() %)) (map (fn [n]
       (cond (nil? n)
         '()
         :else
         (-> n
             .getNodeType
             str))) f))

(defn make-matcher [nodetype]
  (fn [node]
    (let [znode (z/node node)]
      (and
        (not (nil? znode))
        (= (str (.getNodeType znode))
           nodetype)))))

(defn matcho [nodetype node q]
  (let [znode (z/node node)]
    (and
      (not (nil? znode))
      (= (str (.getNodeType znode))
         nodetype))))

(defn tree-match [zipper matcher]
  (loop [loc zipper]
    (if (z/end? loc)
      []
      (if-let [matcher-result (matcher loc)]
        loc
        (recur (z/next loc))))))

(defn treematchero [zipper matcher q]
  (loop [loc zipp

(l/defne orderpreservedo [x y l]
  ([_ _ [x . r]] (l/membero y r))
  ([_ _ [_ . r]] (orderpreservedo x y r)))

(l/run* [q]
   (l/fresh [l]
     (l/== l (l/lvars 5))
     (orderpreservedo :a :b l)
     (l/== q l)))
