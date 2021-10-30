# Data types

HBL has two main data types: integers and lists. There are two additional data types that are typically only encountered in Thimble: builtin functions and macros.

## Integers

Integers are unbounded, in theory. (There is a limit to what can be stored in a Scala BigInt, so extremely large values may cause an error.)

In HBL code, there are no integer literals; commonly used integers are available as built-in values, and other integers can be calculated from the builtins using arithmetic functions. In Thimble, integer literals are any string of digits, with negative numbers indicated by a leading minus sign: for example, `42` or `-100`.

An integer's output representation is a string of digits, with negatives indicated by a minus sign.

If an integer is evaluated, the result is the integer itself.

## Lists

Lists can hold an arbitrary number of values. They can hold any type of values, including other lists, and a single list can hold values of different types.

In HBL and in Thimble, a list literal can be obtained by wrapping the desired elements in parentheses and adding a `'` quote character before the opening parenthesis: for example, `'(%2-)` in HBL or `'(10 2 -1)` in Thimble. Only  one quote character is needed, even when lists are nested: for example, `'((0)(1))`. Lists can also be constructed using list functions, particularly `cons`.

The empty list is also referred to as *nil*. It can be obtained by writing `'()`, or by the builtins `?` in HBL and `nil` in Thimble.

A list's output representation is an open parenthesis, followed by the output representations of its elements separated by spaces, followed by a closing parenthesis.

If a nonempty list is evaluated, the result is a function or macro call, with the first element in the list serving as the function or macro and the remaining elements serving as arguments. If nil is evaluated, the result is a list representing the function on the line above the current line.

## Builtin functions and macros

Builtins represent the basic functions and macros that are implemented by the interpreter. They can be called, passed to higher-order functions such as `map`, and placed in lists.

In Thimble, each builtin function or macro can be referred to by its name. There is no way to directly refer to builtins in HBL; instead, each builtin can be called using its [associated value](./hbl-builtins.md).

A builtin function's output representation is the name of the function wrapped in angle brackets: for example, `<odd?>`. A builtin macro's output representation is the name of the macro wrapped in colons: for example, `:quote:`.

If a builtin function or macro is evaluated, the result is the builtin itself. (To call a builtin, it must be placed in a list with its arguments and that list evaluated.)
