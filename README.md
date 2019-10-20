# Carillon - Bel Langauge Interpreter

This is a simple interpreter for Paul Graham's [Bel language](http://paulgraham.com/bel.html).

The aim of this implementation is to support bootstrapping the language using its own source code which is written in Bel.


```
$ java -jar carillon-0.1.0.jar bel.bel --repl

BEL REPL. Press ^D to exit.

> (cons 'a 'b '(c d e))
(a b c d e)

> (cons \h "ello")
"hello"

> (2 '(a b c))
b

> (set w '(a (b c) d (e f)))
(a (b c) d (e f))

> (find pair w)
(b c)

> (pop (find pair w))
b

> w
(a (c) d (e f))

> (dedup:sort < "abracadabra")
```
(it freezes at the last expression for some reason...)

## Status

Reading expression and most core parts work. Try running the [original Bel examples](https://sep.yimg.com/ty/cdn/paulgraham/belexamples.txt?t=1570993483&).

Some parts are work in progress, such as: continuations, threading, streams. Performance is just horrible at the moment.

Should you have any questions or ideas, please feel free to open an Issue.

## Getting started

Download a JAR file from the Releases section of this repository. Run the program with the jar command.

To run the REPL: `jar ./carillon-0.1.0.jar --repl`

To eval a bel file: `jar ./carillon-0.1.0.jar FILE1.bel`

You can also load a file before entering the REPL.
To evaluate the Bel interpreter, download the `bel.bel` file from PG's site. Then run:
`java ./carillon-0.1.0.jar bel.bel --repl`

For development, you will need Java JDK 8 and Maven.


## License

Copyright 2019 Janos Erdos

_Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:_

_The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software._

_THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE._