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

## Example program

Here is a program (`range10.hbl` in the examples directory) that takes an integer input N and returns a list of numbers from N up through N+10:

    0.(+.%

For example, passing in an argument of 6 will give the following result:

    (6 7 8 9 10 11 12 13 14 15 16)

Using the HBL codepage, we can encode each character of the above program in half a byte (one hexadecimal digit).

| Hex | Chr |
| --- | --- |
| `0` | `0` |
| `1` | `1` |
| `2` | `2` |
| `3` | `<` |
| `4` | `+` |
| `5` | `*` |
| `6` | `-` |
| `7` | `/` |
| `8` | `%` |
| `9` | `?` |
| `a` | `.` |
| `b` | `,` |
| `c` | `(` |
| `d` | `)` |
| `e` | `'` |
| `f` | newline |

The packed version of our program (`range10.hb`) is therefore three bytes. Here is its hexdump:

    00000000: 0ac4 a8                                  ...

How does this example program work? Let's look at the equivalent in Thimble first (`range10.thbl`):

    '(range arg1 (add arg1 10))

The `'(...)` construct creates a quoted list, which doubles as a function definition in HBL. This function takes an argument `arg1` and constructs the (inclusive) range from `arg1` up through `arg1` plus 10. Because it is the final (and only) function in this program, it is considered the main function; when the program is run, the command-line arguments are passed to this function, and the return value is output.

How do we get from here to the golfed form `0.(+.%`?

In golfed HBL, parentheses have the same meaning as in Thimble. The opening `'(` and all trailing parentheses are added automatically by the parser. The `.` character represents `arg1`. What about the other characters? Each of the first ten characters of the HBL codepage is associated with a value:

| Chr | Value |
| --- | ----- |
| `0` | 0 |
| `1` | 1 |
| `2` | 2 |
| `<` | 3 |
| `+` | 4 |
| `*` | 5 |
| `-` | -1 |
| `/` | 7 |
| `%` | 10 |
| `?` | nil |

Each of these values can also be called as a function. The functions are heavily overloaded, doing different things based on the number and type of arguments they are given. In our example, we have one value and two functions:

- `%` is used as a value, 10
- `+` is called as a function with two integer arguments, which is interpreted as add
- `0` is called as a function with one integer argument, which is interpreted as range
