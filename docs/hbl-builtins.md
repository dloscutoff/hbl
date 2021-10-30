# HBL Builtins

## `0`

Value: `0` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `this`\* |
| (int)       | `1to` |
| (list)      | `length` |
| (int int)   | `range` |
| (int list)  | `take` |

\* Used as a magic value, not called as a 0-argument macro

## `1`

Value: `1` (int)

Callable with:

| Arg types      | Thimble equivalent |
| -------------- | ------------------ |
| (list)         | `head` |
| (int int)      | `pow` |
| (list int)     | `nth` |
| (any ... list) | `cons` |

## `2`

Value: `2` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `2prev`\* |
| (int)       | `double` |
| (list)      | `tail` |
| (int list)  | `drop` |

\* Used as a magic value, not called as a 0-argument macro

## `<`

Value: `3` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `3prev`\* |
| (int)       | `neg` |
| (list)      | `reverse` |
| (int int)   | `less?` |

\* Used as a magic value, not called as a 0-argument macro

## `+`

Value: `4` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| ()            | `4prev`\* |
| (int)         | `inc` |
| (int int)     | `add` |
| (list list)   | `concat` |
| (int int int) | `add` |

\* Used as a magic value, not called as a 0-argument macro

## `*`

Value: `5` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `5prev`\* |
| (int int)   | `mul` |
| (list int)  | `repeat` |
| (any list)  | `map` |

\* Used as a magic value, not called as a 0-argument macro

## `-`

Value: `-1` (int)

Callable with:

| Arg types         | Thimble equivalent |
| ----------------- | ------------------ |
| (int)             | `dec` |
| (int int)         | `sub` |
| (any any ... any) | `chain` |

## `/`

Value: `7` (int)

Callable with:

| Arg types         | Thimble equivalent |
| ----------------- | ------------------ |
| (int)             | `abs` |
| (int int)         | `div` |

## `%`

Value: `10` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `argcount`\* |
| (int)       | `odd?` |
| (int int)   | `mod` |
| (any list)  | `filter` |

\* Used as a magic value, not called as a 0-argument macro

## `?`

Value: `()` (list)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| ()            | `arglist`\* |
| (any)         | `recur` |
| (any any ...) | `cond` |

\* Used as a magic value, not called as a 0-argument macro

## `.`

Magic value: first argument to the current function

Thimble equivalent: `arg1`

## `,`

Magic value: second argument to the current function

Thimble equivalent: `arg2`

## `'0`

Value: `16` (int)

Callable with: TBD

## `'1`

Value: `6` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `arg1`\* |
| (list)      | `last` |
| (any list)  | `append` |

\* Used as a magic value, not called as a 0-argument macro

## `'2`

Value: `8` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `arg2`\* |
| (list)      | `init` |

\* Used as a magic value, not called as a 0-argument macro

## `'<`

Value: `9` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `arg3`\* |

\* Used as a magic value, not called as a 0-argument macro

## `'+`

Value: `26` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | `arg4`\* |

\* Used as a magic value, not called as a 0-argument macro

## `'*`

Value: TBD

Callable with: TBD

## `'-`

Value: TBD

Callable with: TBD

## `'/`

Value: TBD

Callable with: TBD

## `'%`

Value: `100` (int)

Callable with: TBD

## `'?`

Value: TBD, currently `50` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| (int)         | `zero?` |
| (list)        | `empty?` |
| (any any ...) | `recur` |

## `'.`

Magic value: list of arguments to the current function

Thimble equivalent: `arglist`

## `',`

Magic value: third argument to the current function

Thimble equivalent: `arg3`
