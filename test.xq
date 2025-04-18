import module namespace test = "org.basex.examples.xquery.functions.TestModule";
let $input := <p xmlns:test="http://test" test:attr="p">TEST</p>
let $f := test:function($input)
let $domf := test:domfunction($input)
return
( '=== The serializations look good.'
, ( $f, $domf ) ! serialize(.)
, '=== The in-scope-prefixes on the <p> element of $domf seem all right.'
, for $p in ( $f, $domf )
  return [ in-scope-prefixes($p) ! [ ., namespace-uri-for-prefix(., $p) ] ]
, '=== But the namespace URI of the test:attr attribute is missing after the DOM transformation.'
, for $p in ( $f, $domf )
  return [ $p/@* ! [ name(.), namespace-uri(.), node-name(.), prefix-from-QName(node-name(.)), namespace-uri-from-QName(node-name(.)) ] ]
)
