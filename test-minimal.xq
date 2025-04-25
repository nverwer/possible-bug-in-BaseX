import module namespace test = "org.basex.examples.xquery.functions.TestModule";
let $e := test:element()
for $x in ( $e, $e/* )
return
( $x
, [ $x/@*/name() , $x/@*/namespace-uri() , $x/@*/node-name() , $x/@*/node-name()!namespace-uri-from-QName(.) ]
, [ $x/name(), $x/namespace-uri() ]
, '---'
)