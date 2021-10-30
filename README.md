# HBL: Half-Byte Lisp

HBL is a Lisp dialect designed for code golf. It uses a codepage with 16 characters, meaning that two characters can be encoded in a single byte. HBL also comes with an ungolfed mode called Thimble.

## Getting started

HBL is implemented in Scala 3. If you have Scala 3 with the sbt build tool installed, you can use the following steps to run HBL code:

- Clone the HBL repository
- At the command line, navigate to the repository's root directory
- Run `sbt`
- Once the sbt server has started up, type `run path/to/hblfile args` at the sbt prompt to run the given HBL program with the given arguments

For example, you can run the Collatz function example program with an input of 7 by typing `run examples/collatz.hbl 7`. To pass a list as an argument, wrap it in quotes: `"(1 2 3)"`.

The format of a code file is determined by its extension:

- `.hb` indicates packed HBL code, a binary file with each byte representing two codepage characters. You may find a hex editor helpful.
- `.hbl` indicates HBL code in plain text format with each codepage character represented by one byte of ASCII.
- `.thbl` indicates Thimble code in plain text format.

### Running with Java

If you have Java installed, you can run HBL directly from a JAR file without any Scala dependencies:

- Download the JAR file from the latest [release](https://github.com/dloscutoff/hbl/releases)
- At the command line, navigate to the directory where the JAR file is
- Run `java -jar hbl-a.b.c.jar path/to/hblfile args` to run the given HBL program with the given arguments

*Thanks to [cgccuser](https://github.com/cgccuser) for figuring out how to package the application in JAR format.*

## Documentation

Documentation for HBL and Thimble can be found in [the docs folder](https://github.com/dloscutoff/hbl/tree/main/docs#readme) of this repository.
