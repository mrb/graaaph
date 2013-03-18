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

;; Removes nil from visitor fns
(defn safe-visit [v]
  (fn [x]
    (m/domonad m/maybe-m
       [mx (first x)
        mv v
        :when (not (nil? mx))]
        (mv mx))))

;; A Bizarre but dependable quirk of jruby-parser's AST representation -
;; nodes that match this class don't have the data for the fn below.
(defn invalid-ast-node? [node]
  (= (class node) org.jrubyparser.SourcePosition$1))

;; =============================================================================
;; AST Data Extraction

(defn get-position-data [node]
  (let [position (.getPosition node)]
    (cond
      (invalid-ast-node? position) {}
      :else
      (into {}
        [[:file   (.getFile position)]
         [:start-line   (.getStartLine position)]
         [:end-line     (.getEndLine position)]
         [:start-offset (.getStartOffset position)]
         [:end-offset   (.getEndOffset position)]]))))

(defn data-visitor [node]
  (into {}
   [[:position (get-position-data node)]
    [:type (-> node .getNodeType str)]]))

;; =============================================================================
;; Parser interface - returns map

(defn zipper [ruby]
  (let [parsed (parse-ruby ruby)]
    (code-zipper parsed)))

(defn parse [ruby]
  (let [zipped (zipper ruby)]
      (tree-visitor zipped (safe-visit data-visitor))))
