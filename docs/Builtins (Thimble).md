# Thimble builtins

## Literals and constants

### Integers

Any run of digits, optionally preceded by a minus sign, is an integer literal.

### `nil`

`nil` is a falsey value, currently equal to the empty list.

## Magic values

### Arguments

The first nine arguments to the current function are `arg1`, `arg2`, ... , `arg9` respectively.

The list of all arguments passed to the current function is `arglist`.

The number of arguments passed to the current function is `argcount`.

### Line references

The main function on the same line as the current function is `this`.

> **Note:** `this` may be different from the current function! If you call a nested function, the current function (the one that gets called by `recur`) is the nested function, but `this` is the outer function.

The function or value on the line above the current function is `prev`. The lines above that are `2prev`, `3prev`, and so on. The references wrap around: for example, calling `prev` from the first line in the program references the last line in the program.

## Macros

### `chain`

Takes three or more arguments: a series of functions (or macros) and a value. Applies the rightmost function to the value, then the next function to that result, and so on. All functions must be able to take one argument. For instance, `(chain inc double length arg1)` is equivalent to `(inc (double (length arg1)))`.

### `cond`

Takes two or more arguments. Short-circuiting conditional.

With an odd number of arguments, `cond` is equivalent to an if-elseif-else construct. For example, evaluation of `(cond a b c d e)` procedes as follows:

1. Evaluate `a` and test if it is truthy. If so, return `b`.
2. If not, evaluate `c` and test if it is truthy. If so, return `d`. If not, return `e`.

With an even number of arguments, `cond` behaves the same way, except that it uses the last conditional expression as the "else" case. For example, when evaluating `(cond a b c d)`:

1. Evaluate `a` and test if it is truthy. If so, return `b`.
2. If not, evaluate `c` and test if it is truthy. If so, return `d`. If not, return `c`.

### `count-locals`

Takes no arguments and returns the number of arguments passed to the current function. `(count-locals)` is equivalent to the magic value `argcount`.

### `get-local`

Takes one argument, a positive integer N, and returns the Nth argument passed to the current function. `(get-local N)` is equivalent to the magic value `argN` for any N between 1 and 9.

### `get-locals`

Takes no arguments and returns the list of all arguments passed to the current function. `(get-locals)` is equivalent to the magic value `arglist`.

### `get-prev`

Takes one optional argument, an integer N, and returns the contents of the Nth line before the current function's line. If N is omitted, returns the contents of the line directly above the current function's line. `(get-prev N)` ie equivalent to the magic value `Nprev` for any nonnegative N, and `(get-prev)` is equivalent to the magic value `prev`.

### `get-this`

Takes no arguments and returns the contents of the current function's line. `(get-this)` is equivalent to the magic value `this`.

### `quote`

Takes one argument and returns it unevaluated. Mainly used to prevent lists from being evaluated as code.

### `recur`

Takes one or more arguments and calls the current function again with those arguments.

## Functions (1 argument)

## Functions (2 arguments)

## Functions (3 or more arguments)
