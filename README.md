# graaaph

graaaph is a Clojure library designed to make it easy to consume, manipulate, and analyze JRuby's AST representation of Ruby code from Clojure.

## Usage

```clojure
(parse "def a;'ok';end")

;;({:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "ROOTNODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "NEWLINENODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 0, :end-offset 14}, :type "DEFNNODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 4, :end-offset 5}, :type "ARGUMENTNODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 6}, :type "ARGSNODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 11}, :type "NEWLINENODE"}
   {:position {:file "", :start-line 0, :end-line 0, :start-offset 6, :end-offset 10}, :type "STRNODE"})
```

## Tests

`lein test`

## License

Copyright © 2013 Michael R. Bernstein

Distributed under the Eclipse Public License, the same as Clojure.
