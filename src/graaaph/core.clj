(ns graaaph.core
  (:import (org.jrubyparser CompatVersion
                            Parser
                            SourcePosition)
           (org.jrubyparser.parser ParserConfiguration)
           (org.jrubyparser.ast Node)
           (java.io.StringReader))
  (:require [clojure.zip :as z]
            [clojure.algo.monads :as m
              :only (domonad maybe-m)]))

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

;; A node is marked "invisible" by the jruby-parser if it does not contain valid
;; data and can be ignored by programs like ours.
(defn invalid-ast-node? [node]
  (.isInvisible node))

;; Literal nodes have values
(defn literal-node? [node]
  (instance? org.jrubyparser.ast.ILiteralNode node))

;; Named nodes have names (e.g. class variables, ivars, etc.)
(defn named-node? [node]
  (instance? org.jrubyparser.ast.INameNode node))

;; Removes invalid AST ndoes and nil from visitor fns
(defn safe-visit [v]
  (fn [x]
    (m/domonad m/maybe-m
       [mx (first x)
        mv v
        :when (and (not (invalid-ast-node? mx))
                   (not (nil? mx)))]
        (mv mx))))

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

(defn data-visitor [node]
  (into {}
   [[:position (get-position-data node)]
    [:type     (-> node .getNodeType str)]
    [:value    (cond (literal-node? node) (.getValue node) :else "")]
    [:name     (cond (named-node?   node) (.getName  node) :else "")]]))

;; =============================================================================
;; Parser interface

;; Get the code as a zipper
(defn zipper [ruby]
  (let [parsed (parse-ruby ruby)]
    (code-zipper parsed)))

;; Get the code as a transformed, seqable clojure map
(defn parse [ruby]
  (let [zipped (zipper ruby)]
      (tree-visitor zipped (safe-visit data-visitor))))
