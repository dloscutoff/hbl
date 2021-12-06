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

The function or value on the line above the current function is `prev`. The lines above that are `2prev`, `3prev`, and so on. The function or value on the line below the current function is `next`. The references wrap around: for example, calling `prev` from the first line in the program references the last line in the program.

## Macros

### `branch`

Takes four or more arguments and evaluates the binary tree resulting from the following rearrangement:

- The first argument is the root.
- The remaining arguments are divided into left and right halves. If the number of remaining arguments is odd, the left half is the smaller half.
- If either half contains more than three expressions, apply the branch macro to it recursively.
- If the left half contains only one expression, that expression is used as the left argument (rather than being wrapped in a single-element list).

For instance, `(branch mul inc arg1 dec arg1)` is equivalent to `(mul (inc arg1) (dec arg1))`, and `(branch mul arg1 dec (double arg1))` is equivalent to `(mul arg1 (dec (double arg1)))`.

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

### `get-next`

Takes one optional argument, an integer N, and returns the contents of the Nth line after the current function's line. If N is omitted, returns the contents of the line directly below the current function's line. `(get-next)` is equivalent to the magic value `next`.

### `get-prev`

Takes one optional argument, an integer N, and returns the contents of the Nth line before the current function's line. If N is omitted, returns the contents of the line directly above the current function's line. `(get-prev N)` ie equivalent to the magic value `Nprev` for any nonnegative N, and `(get-prev)` is equivalent to the magic value `prev`.

### `get-this`

Takes no arguments and returns the contents of the current function's line. `(get-this)` is equivalent to the magic value `this`.

### `quote`

Takes one argument and returns it unevaluated. Mainly used to prevent lists from being evaluated as code.

### `recur`

Takes one or more arguments and calls the current function again with those arguments.

## Functions (1 argument)

### `1to`

Takes an integer and returns a list of all integers from 1 up to the argument, inclusive. If the argument is less than 1, returns an empty list.

### `abs`

Takes an integer and returns its absolute value.

### `dec`

Takes an integer and returns the argument minus 1 (decremented).

### `double`

Takes an integer and returns the argument times 2.

### `empty?`

Takes a list; returns 1 if the argument is the empty list, 0 if not.

### `flatten`

Takes a list, possibly nested; returns a new non-nested list containing all the elements of the sublists, with the sublists themselves having first been recursively flattened.

### `flatten-once`

Takes a list, possibly nested; returns a new list containing all the elements of the sublists. Sublists are not recursively flattened; if they contain lists, those lists will appear as lists in the result.

### `head`

Takes a list and returns its first element. If the list is empty, returns nil.

### `inc`

Takes an integer and returns the argument plus 1 (incremented).

### `init`

Takes a list and returns the sublist containing all but the last element. If the list is empty, returns nil.

### `last`

Takes a list and returns its last element. If the list is empty, returns nil.

### `length`

Takes a list and returns the number of elements in it.

### `max`

Takes a list, [flattens](#flatten) it, and returns its maximum element. If the list is empty, returns nil.

### `min`

Takes a list, [flattens](#flatten) it, and returns its minimum element. If the list is empty, returns nil.

### `neg`

Takes an integer and returns the argument negated.

### `odd?`

Takes an integer; returns 1 if the argument is an odd number, 0 if even.

### `product`

Takes a list of integers, [flattens](#flatten) it, and returns its product. If the list is empty, returns 1. If the flattened list contains any elements besides integers, throws an error.

### `reverse`

Takes a list and returns a new list with the same elements in the opposite order.

### `sort`

Takes a list and returns a new list with the same elements sorted in [ascending order](./data-types.md#comparison).

### `sum`

Takes a list of integers, [flattens](#flatten) it, and returns its sum. If the list is empty, returns 0. If the flattened list contains any elements besides integers, throws an error.

### `tail`

Takes a list and returns the sublist containing all but the first element. If the list is empty, returns nil.

### `zero?`

Takes an integer; returns 1 if the argument is zero, 0 if not.

## Functions (2 arguments)

### `append`

Takes a value and a list; returns a new list whose `init` is the given list and whose `last` is the given value.

### `concat`

Takes two lists and concatenates them into a new list containing the elements of the first argument followed by the elements of the second argument.

### `div`

Takes two integers and returns the first divided by the second, rounded toward zero. If the second argument is 0, throws an error.

### `drop`

Takes an integer N and a list; returns the list with the first N elements removed. If the list has fewer than N elements, returns nil.

### `filter`

Takes a 1-argument function and a list; returns a new list consisting of the elements from the second argument that return a truthy value when passed through the function.

### `less?`

Takes two integers; returns 1 if the first argument is less than the second argument, 0 if not.

### `map`

Takes a 1-argument function and a list; returns a new list containing the results of passing each element of the second argument through the function.

### `mod`

Takes two integers and returns the first modulo the second. The sign of the result matches the sign of the second argument. If the second argument is 0, throws an error.

### `mul`

Takes two integers and returns their product.

### `nth`

Takes a list and an integer N; returns the element of the list at index N. Indices start at 1.

### `pow`

Takes two integers and returns the first to the power of the second. If the exponent is negative or the result would be too big, throws an error.

### `range`

Takes two integers and returns a list of all integers from the first argument up to the the second, inclusive. If the first argument is less than the second argument, returns nil.

### `repeat`

Takes a list and an integer N; returns the concatenation of N copies of the argument list. If N is 0 or negative, returns nil.

### `sub`

Takes two integers and returns the first minus the second.

### `drop`

Takes an integer N and a list; returns a list consisting of the first N elements of the argument list. If the list has fewer than N elements, returns the whole list. If N is 0 or negative, returns nil.

### `zip`

Takes two lists and returns a list of two-item lists containing pairs of elements from the two arguments. If the argument lists are of different lengths, the result has the same length as the shorter list.

## Functions (2 or more arguments)

### `add`

Takes either two or three integers and returns their sum.

### `cons`

Takes one or more values and a list; returns a new list with the given values, in order, added to the beginning of the argument list.

### `map-left`

Takes a 2-argument function, a list, and another value; pairs each element of the second argument with the third argument, passes each pair through the function, and returns a new list containing the results.

### `map-right`

Takes a 2-argument function, a value, and a list; pairs the second argument with each element of the third argument, passes each pair through the function, and returns a new list containing the results.
