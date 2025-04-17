import module namespace test = "org.basex.examples.xquery.functions.TestModule";
let $doc :=
<root xmlns:test="http://test" test:attr="root">
  <p test:attr="p">TEST</p>
</root>
let $f := test:function($doc/*)
let $domf := test:domfunction($doc/*)
let $hof := test:hofunction()($doc/*)
let $domhof := test:domhofunction()($doc/*)
return
( '=== The serializations look good.'
, ( $f, $domf, $hof, $domhof ) ! serialize(.)
, '=== But the namespace URI on the test:attr attribute is missing in $domf and $domhof.'
, for $p in ( $f, $domf, $hof, $domhof )
  return $p ! [ @*/name(),  @*/namespace-uri() ]
, '=== The in-scope-prefixes on the <p> element of $domf seem all right.'
, for $p in $domf
  return in-scope-prefixes($p) ! [ ., namespace-uri-for-prefix(., $p) ]
, '=== But the namespace URI of the test:attr attribute is missing.'
, for $a in $domf/@*
  return [ node-name($a), prefix-from-QName(node-name($a)), namespace-uri-from-QName(node-name($a)) ]
)
