# Attribute namespace disappears after Java transformation

This project is meant to demonstrate a possible bug in BaseX.
The problem is a disappearing namespace on an attribute.
It may be a bug in my own code, rather than in BaseX

# Description of the problem

The problem first occurred in a custom XQuery function that I made in Java,
following the [documentation on Java Bindings](https://docs.basex.org/main/Java_Bindings).
To demonstrate the problem, I have tried to simplify the Java code as much as I could,
and turned it into a [GitHub repository](https://github.com/nverwer/possible-bug-in-BaseX).

The custom function is a higher-order function; it generates another function, which can then be applied to zero or more arguments.
The example module `TestModule` that demonstrates the problem defines 3 functions:

* `test:function($input as element()) as element()` makes a Java (DOM) representation of `$input`, and makes this DOM into a BaseX internal representation, which is returned.
* `test:hofunction() as function($input as element()) as element()` is called without arguments, and returns a function that is the same as `test:function`.
* `test:domfunction() as function($input as element()) as element()` is also called without arguments, and returns a function. The returned function makes a Java (DOM) representation of `$input`, does an identity transformation that copies the DOM, and then makes this copied DOM into a BaseX internal representation, which is returned.

# Expected behaviour

# Steps to reproduce

# How to solve?

# Configuration

