(ns graaaph.core
  (:import (org.jrubyparser CompatVersion
                            Parser
                            SourcePosition)
           (org.jrubyparser.parser ParserConfiguration)
           (org.jrubyparser.ast Node)
           (java.io.StringReader))
  (:require [clojure.zip :as z]
            [clojure.core.logic :as l]))

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
(defn value-node? [node]
  (and
    (not (instance? org.jrubyparser.ast.ArrayNode node))
    (instance? org.jrubyparser.ast.ILiteralNode node)))

;; Named nodes have names (e.g. class variables, ivars, etc.)
(defn named-node? [node]
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
  (let [node (first node-list)]
    (if (and (not (nil? node))
             (not (invalid-ast-node? node)))
        (into {}
          [[:position    (get-position-data node)]
           [:type        (-> node .getNodeType str)]
           [:value       (if (value-node? node)
                           (.getValue node))]
           [:name        (if (named-node? node)
                           (.getName node))]]))))

;; [:name        (.getName  node)]
;; [:class-path  (-> node .getCPath .getName)]
;; [:args        (.getArgs node)]]))

;; =============================================================================
;; Parser interface

;; Get the code as a zipper
(defn zipper [ruby]
  (let [parsed (parse-ruby ruby)]
    (code-zipper parsed)))

;; Get the code as a transformed, seqable clojure map
(defn parse-ruby-code [ruby]
  (let [zipped (zipper ruby)]
      (tree-visitor zipped data-visitor)))
