(ns graaaph.core
  (:import (org.jrubyparser CompatVersion
                            Parser
                            SourcePosition)
           (org.jrubyparser.parser ParserConfiguration)
           (org.jrubyparser.ast Node)
           (org.jrubyparser.rewriter ReWriteVisitor)
           (java.io File
                    StringReader
                    StringWriter)
           (javax.imageio ImageIO))
  (:require [clojure.zip :as z]
            [clojure.core.logic :as l]
            [clojure.core.logic.fd :as fd]
            [rhizome.viz :as v]))

;; =============================================================================
;; Zipper Create Functions

(defn can-have-children? [nodes]
  (instance? Node nodes))

(defn get-children [nodes]
  (.childNodes nodes))

(defn make-node [_ c]
  c)

(defn code-zipper [nodes]
  (z/zipper can-have-children? get-children make-node nodes))

;; =============================================================================
;; JRuby Interface - Ruby Parsing

(defn parse-ruby [ruby-string]
  (let [config (ParserConfiguration. 0 (CompatVersion/RUBY1_9))
        reader (java.io.StringReader. ruby-string)
        parser (Parser.)]
    (.parse parser "" reader config)))

;; =============================================================================
;; Zipper Visitor Functions

(defn tree-visitor [zipper visitor]
  (let [coll '()]
    (loop [loc zipper c coll]
      (if (z/end? loc)
        (reverse c)
        (if-let [visitor-result (visitor loc)]
          (recur (z/next loc) (cons visitor-result c))
          (recur (z/next loc) c))))))

(defn tree-match [zipper matcher]
  (loop [loc zipper]
    (if (z/end? loc)
      []
      (if-let [matcher-result (matcher loc)]
        loc
        (recur (z/next loc))))))

;; =============================================================================
;; AST Data Extraction Helpers

(defn invalid-ast-node? [node]
  "A node is marked invisible by the jruby-parser if it does not contain valid
   data and can be ignored by programs like ours."
  (.isInvisible node))

(defn value-node? [node]
  "Literal nodes have values"
  (and
    (not (instance? org.jrubyparser.ast.ArrayNode node))
    (instance? org.jrubyparser.ast.ILiteralNode node)))

(defn named-node? [node]
  "Named nodes have names (e.g. class variables, ivars, etc.)"
  (instance? org.jrubyparser.ast.INameNode node))

(defn scoping-node? [node]
  (instance? org.jrubyparser.ast.IScopingNode node))

(defn argument-node? [node]
  (instance? org.jrubyparser.ast.IArgumentNode node))

(defn value-node? [node]
  (instance? org.jrubyparser.ast.SValueNode node))

;; =============================================================================
;; AST Data Extraction

(defn get-position-data [node]
  (let [position (.getPosition node)]
    (into {}
      [[:file         (.getFile position)]
       [:start-line   (.getStartLine position)]
       [:end-line     (.getEndLine position)]
       [:start-offset (.getStartOffset position)]
       [:end-offset   (.getEndOffset position)]])))

(defn data-visitor [node-list]
  (let [node (first node-list)
        node-map {}]
    (if (and (not (nil? node))
             (not (invalid-ast-node? node)))
        (into node-map
          [[:type        (-> node .getNodeType str)]
           [:value       (if (value-node? node)
                           (.getValue node))]
           [:name        (if (named-node? node)
                           (.getName node))]]))))

;; [:position    (get-position-data node)]
;; [:class-path  (-> node .getCPath .getName)]
;; [:args        (.getArgs node)]]))

;; =============================================================================
;; Parser interface

(defn ruby-code-zipper [ruby]
  "returns a zipper on the ruby ast"
  (let [parsed (parse-ruby ruby)]
    (code-zipper parsed)))

(defn parse-ruby-code [ruby]
  "transform the code into a seqable clojure map"
  (let [zipped (ruby-code-zipper ruby)]
      (tree-visitor zipped data-visitor)))

;; =============================================================================
;; Code rewriting

(defn zipper-to-source [zipper]
  "Transform Java ast node types back to ruby source"
  (let [writer (StringWriter.)
        node (first zipper)]
    (.accept node (ReWriteVisitor. writer "(string)"))
    writer))

;; =============================================================================
;; Rhizome AST visualization functions

(defn view-ruby-ast [ruby-code]
  (let [zipper (first (ruby-code-zipper ruby-code))]
    (v/view-tree can-have-children? get-children zipper
        :options {:dpi 50}
        :node->descriptor (fn [node] {:shape
                                        (cond
                                          (named-node? node) "rectangle")
                                      :label
                                        (cond
                                          (named-node? node) (str (.getName node) " (" (str (.getNodeType node)) ")")
                                          :else (str (.getNodeType node)))}))))

(defn save-ruby-ast-image [ruby-code filename]
  (let [zipper (first (ruby-code-zipper ruby-code))
        buffer (v/tree->image can-have-children? get-children zipper
                :node->descriptor (fn [n] {:label (str (.getNodeType n))}))]
    (ImageIO/write buffer "png"  (File. filename))))

;; =============================================================================
;; Ruby AST core.logic relations

(l/defne membero
  "A membero with disequality - thanks @webyrd"
  [x l]
  ([_ [x . tail]])
  ([_ [head . tail]]
      (l/!= x head)
      (membero x tail)))

(l/defne not-membero
  "a relation where x is not a member of l"
  [x l]
  ([_ []])
  ([_ [y . r]]
    (l/!= x y)
    (not-membero x r)))

(l/defne rember*o
  "a relation that removes all instances of x in l, resulting in o"
  [x l o]
  ([_ () ()])
  ([_ [x . xs] _]
    (rember*o x xs o))
  ([_ [y . xs] [y . ys]]
    (l/!= x y)
    (rember*o x xs ys)))

(l/defne dupeo
  "a relation that collects all duplicate elements in l exactly once in q"
  [l q]
  ([() ()])
  ([ (head . tail) _ ]
    (l/fresh [new-tail res]
      (l/conde
        [(membero head tail) (rember*o head tail new-tail) (dupeo new-tail res) (l/== q (l/lcons head res))]
        [(not-membero head tail) (dupeo tail q)]))))

(l/defne nodetypeo
  "a relation where q is a collection of nodes which match type ntype"
  [nodes ntype q]
  ([() _ ()])
  ([[head . tail] _ _]
   (l/fresh [res]
     (l/matche [head]
       ([{:type ?type :value _ :name _ }]
         (l/conde
           [(l/== ntype ?type) (nodetypeo tail ntype res) (l/== q (l/lcons head res))]
           [(l/!= ntype ?type) (nodetypeo tail ntype q)]))))))
