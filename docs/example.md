# Example program

Here is a program (`range10.hbl` in the examples directory) that takes an integer input N and returns a list of numbers from N up through N+10:

    0.(+.%

For example, passing in an argument of 6 will give the following result:

    (6 7 8 9 10 11 12 13 14 15 16)

Using the [HBL codepage](./codepage.md), we can encode each character of the above program in half a byte (one hexadecimal digit). The packed version of our program (`range10.hb`) is therefore three bytes. Here is its hexdump:

    00000000: 0ac4 a8                                  ...

## How does it work?

Let's look at the equivalent in Thimble first (`range10.thbl`):

    '(range arg1 (add arg1 10))

The `'(...)` construct creates a quoted list, which doubles as a function definition in HBL. This function takes an argument `arg1` and constructs the (inclusive) range from `arg1` up through `arg1` plus 10. Because it is the final (and only) function in this program, it is considered the main function; when the program is run, the command-line arguments are passed to this function, and the return value is output.

How do we get from here to the golfed form `0.(+.%`?

In golfed HBL, parentheses have the same meaning as in Thimble. The opening `'(` and all trailing parentheses are added automatically by the parser. The `.` character represents `arg1`. What about the other characters? Each of the first ten characters of the HBL codepage is [associated with a value](./hbl-builtins.md). Each of these values can also be called as a function. The functions are heavily overloaded, doing different things based on the number and type of arguments they are given. In our example, we have one value and two functions:

- `%` is used as a value, 10
- `+` is called as a function with two integer arguments, which is interpreted as add
- `0` is called as a function with one integer argument, which is interpreted as range
