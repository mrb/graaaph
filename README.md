# graaaph

graaaph is a Clojure library designed to make it easy to consume, manipulate, and analyze JRuby's AST representation of Ruby code from Clojure.

My goal is to use various Clojure tools to analyze and transform Ruby AST, while making the Java interop as transparent as possible.

## Usage

Get code as a map, with a lot of data:

```clojure
(use 'graaaph.core)

(parse "def a;'ok';end")

;;({:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "ROOTNODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "NEWLINENODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "DEFNNODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 4, :end-offset 5}, :type "ARGUMENTNODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 6}, :type "ARGSNODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 11}, :type "NEWLINENODE"}
;; {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 10}, :type "STRNODE"})
```

Or as a zipper, but still tasting like Java:

```clojure
(use 'graaaph.core)

(-> (zipper "1")
     z/next
     z/next
     z/node
     .getValue) ;; From the jruby-parser API
;; 1
```

## Tests

`lein test`

## License

Copyright © 2013 Michael R. Bernstein

Distributed under the Eclipse Public License, the same as Clojure.
