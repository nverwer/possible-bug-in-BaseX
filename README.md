# Attribute namespace disappears after Java transformation

This project is meant to demonstrate a possible bug in BaseX.
The problem is a disappearing namespace on an attribute after transforming a document.
It may be a bug in my own Java code, rather than in BaseX.

# Solution

The problem was solved by
* always using a document node when converting from Java to BaseX;
* adding namespace declaration attributes for all namespaces on elements as well as attributes.

Example code is in `TestModule.java` and `test-minimal.xq`.

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

```
<p xmlns:test="http://test" test:attr="p">TEST</p>
```

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

# Configuration

* Windows 10 (and also Linux Mint).
* Java OpenJDK 11 (Temurin-11.0.14.1+1)
* BaseX 11.8 (as downloaded from basex.org)
