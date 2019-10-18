# Carillon - Bel Langauge Interpreter

This is a simple interpreter for Paul Graham's [Bel language](http://paulgraham.com/bel.html).

The aim of this implementation is to support bootstrapping the language using the `bel.bel` file.


## Getting started

Download a JAR file from the Releases section of this repository. Run the program with the jar command.

To run the REPL: `jar ./carillon-0.1.0.jar --repl`

To eval a bel file: `jar ./carillon-0.1.0.jar FILE1.bel`

You can also load a file before entering the REPL. First, download the `bel.bel` file from the PG's site. Then run:
`java ./carillon-0.1.0.jar bel.bel --repl`

For development, you will need Java JDK 8 and Maven.


## License

Copyright 2019 Janos Erdos

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.