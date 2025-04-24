import module namespace test = "org.basex.examples.xquery.functions.TestModule";
let $e := test:element()
let $d := test:document()/*
for $x in ( $e, $d )
return
  [ $x , $x/@*/name() , $x/@*/namespace-uri() , $x/@*/node-name() , $x/@*/node-name()!namespace-uri-from-QName(.) ]