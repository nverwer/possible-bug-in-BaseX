# Attribute namespace disappears after Java transformation

This project is meant to demonstrate a possible bug in BaseX.
The problem is a disappearing namespace on an attribute after transforming a document.
It may be a bug in my own Java code, rather than in BaseX.

# Description of the problem

The problem first occurred in a custom XQuery function that I made in Java,
following the [documentation on Java Bindings](https://docs.basex.org/main/Java_Bindings).
To demonstrate the problem, I have tried to simplify the Java code as much as I could,
and turned it into a [GitHub repository](https://github.com/nverwer/possible-bug-in-BaseX).

The custom function does the following:

* Convert the input `FElem` element into a `org.w3c.dom.Element` using `FNode.toJava()`.
* Transform the DOM element, buitding a new `org.w3c.dom.Element`. In the demonstration project, this is the identity function.
* Optionally convert the transformed DOM element into a BaseX `Value` using `JavaCall.toValue`.

The example module `TestModule` defines 2 functions:

* `test:function($input as element()) as element()` makes a Java (DOM) representation of `$input`, and converts this DOM into a BaseX internal representation, which is returned. This works as expected.
* `test:domfunction($input as element()) as element()` makes a Java (DOM) representation of `$input`, does an identity transformation that copies the DOM, and converts the copied DOM into a BaseX internal representation, which is returned. This loses attribute namespaces.

The XQuery `test.xq` demonstrates the problem.
It uses a simple input document

```<p xmlns:test="http://test" test:attr="p">TEST</p>```

The output from `test:function($input)` still has the namespace URI on `@test:attr`,
but the output from `test:domfunction($input)` does not.
In both cases, the namespace declaration `xmlns:test="http://test"` is still present.

# Expected behaviour

The output from `test:domfunction($input)` should still have the namespace URI on the `@test:attr` attribute.

# Steps to reproduce

* Build the `basex-test-1.0.0.jar` file using `maven install`.
* Copy the jar into the `lib/custom` directory of your BaseX installation.
* Start the BaseX GUI.
* Open `test.xq` in the BaseX GUI and run it.
* The output should look like this:

```
=== The serializations look good.
<p xmlns:test="http://test" test:attr="p">TEST</p>
<p xmlns:test="http://test" test:attr="p">TEST</p>
=== The in-scope-prefixes on the <p> element of $domf seem all right.
[(["test","http://test"],["xml","http://www.w3.org/XML/1998/namespace"])]
[(["test","http://test"],["xml","http://www.w3.org/XML/1998/namespace"])]
=== But the namespace URI of the test:attr attribute is missing after the DOM transformation.
[["test:attr","http://test",test:attr,"test","http://test"]]
[["test:attr","",test:attr,"test",""]]
```

# How to solve?

I believe that I used the right way to set the attributes when making a copy of the DOM.
But it is possible that something is wrong there, which prevents BaseX from picking up the namespace of the attribute.
In that case, I hope someone can tell me what I should do differently.

I tried to follow what happens in `Value bxResult = JavaCall.toValue(outputElement, queryContext, null);`
It calls `FElem.build()`, where the `test` namespace is added to the `namespaces` variable, and the `test:attr` attribute has the right QName.
The result in `bxResult` also looks right, see lines 76 and 77 in `TestModule.java`.

But when I get the namespace URI of `@test:attr` in XQuery, it is absent.

One might think that this is a problem with the XQuery functions that deal with namespaces, but I don't think that is the case.
In the program where this issue originally arose, I apply several more XQuery functions to the result of my transformation function,
and the namespace has always disappeared.

# Configuration

* Windows 10 (and also Linux Mint).
* Java OpenJDK 11 (Temurin-11.0.14.1+1)
* BaseX 11.8 (as downloaded from basex.org)
