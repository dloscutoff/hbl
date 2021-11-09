# HBL Builtins

## `0`

Value: `0` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`this`](./thimble-builtins.md#line-references)\* |
| (int)       | [`1to`](./thimble-builtins.md#1to) |
| (list)      | [`length`](./thimble-builtins.md#length) |
| (int int)   | [`range`](./thimble-builtins.md#range) |
| (int list)  | [`take`](./thimble-builtins.md#take) |

\* Used as a magic value, not called as a 0-argument macro

## `1`

Value: `1` (int)

Callable with:

| Arg types      | Thimble equivalent |
| -------------- | ------------------ |
| (list)         | [`head`](./thimble-builtins.md#head) |
| (int int)      | [`pow`](./thimble-builtins.md#pow) |
| (list int)     | [`nth`](./thimble-builtins.md#nth) |
| (any ... list) | [`cons`](./thimble-builtins.md#cons) |

## `2`

Value: `2` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`2prev`](./thimble-builtins.md#line-references)\* |
| (int)       | [`double`](./thimble-builtins.md#double) |
| (list)      | [`tail`](./thimble-builtins.md#tail) |
| (int list)  | [`drop`](./thimble-builtins.md#drop) |

\* Used as a magic value, not called as a 0-argument macro

## `<`

Value: `3` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`3prev`](./thimble-builtins.md#line-references)\* |
| (int)       | [`neg`](./thimble-builtins.md#neg) |
| (list)      | [`reverse`](./thimble-builtins.md#reverse) |
| (int int)   | [`less?`](./thimble-builtins.md#less) |

\* Used as a magic value, not called as a 0-argument macro

## `+`

Value: `4` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| ()            | [`4prev`](./thimble-builtins.md#line-references)\* |
| (int)         | [`inc`](./thimble-builtins.md#inc) |
| (list)        | [`sum`](./thimble-builtins.md#sum) |
| (int int)     | [`add`](./thimble-builtins.md#add) |
| (list list)   | [`concat`](./thimble-builtins.md#concat) |
| (int int int) | [`add`](./thimble-builtins.md#add) |

\* Used as a magic value, not called as a 0-argument macro

## `*`

Value: `5` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`5prev`](./thimble-builtins.md#line-references)\* |
| (list)      | [`product`](./thimble-builtins.md#product) |
| (int int)   | [`mul`](./thimble-builtins.md#mul) |
| (list int)  | [`repeat`](./thimble-builtins.md#repeat) |
| (any list)  | [`map`](./thimble-builtins.md#map) |

\* Used as a magic value, not called as a 0-argument macro

## `-`

Value: `-1` (int)

Callable with:

| Arg types         | Thimble equivalent |
| ----------------- | ------------------ |
| ()                | [`next`](./thimble-builtins.md#line-references)\* |
| (int)             | [`dec`](./thimble-builtins.md#dec) |
| (list)            | [`flatten`](./thimble-builtins.md#flatten) |
| (int int)         | [`sub`](./thimble-builtins.md#sub) |
| (any any ... any) | [`chain`](./thimble-builtins.md#chain) |

## `/`

Value: `7` (int)

Callable with:

| Arg types         | Thimble equivalent |
| ----------------- | ------------------ |
| (int)             | [`abs`](./thimble-builtins.md#abs) |
| (list)            | [`max`](./thimble-builtins.md#max) |
| (int int)         | [`div`](./thimble-builtins.md#div) |

## `%`

Value: `10` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`argcount`](./thimble-builtins.md#arguments)\* |
| (int)       | [`odd?`](./thimble-builtins.md#odd) |
| (list)      | [`sort`](./thimble-builtins.md#sort) |
| (int int)   | [`mod`](./thimble-builtins.md#mod) |
| (any list)  | [`filter`](./thimble-builtins.md#filter) |

\* Used as a magic value, not called as a 0-argument macro

## `?`

Value: `()` (list)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| ()            | [`arglist`](./thimble-builtins.md#arguments)\* |
| (any)         | [`recur`](./thimble-builtins.md#recur) |
| (any any ...) | [`cond`](./thimble-builtins.md#cond) |

\* Used as a magic value, not called as a 0-argument macro

## `.`

Magic value: first argument to the current function

Thimble equivalent: [`arg1`](./thimble-builtins.md#arguments)

## `,`

Magic value: second argument to the current function

Thimble equivalent: [`arg2`](./thimble-builtins.md#arguments)

## `'0`

Value: `16` (int)

Callable with: TBD

## `'1`

Value: `6` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`arg1`](./thimble-builtins.md#arguments)\* |
| (list)      | [`last`](./thimble-builtins.md#last) |
| (any list)  | [`append`](./thimble-builtins.md#append) |

\* Used as a magic value, not called as a 0-argument macro

## `'2`

Value: `8` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`arg2`](./thimble-builtins.md#arguments)\* |
| (list)      | [`init`](./thimble-builtins.md#init) |

\* Used as a magic value, not called as a 0-argument macro

## `'<`

Value: `9` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`arg3`](./thimble-builtins.md#arguments)\* |

\* Used as a magic value, not called as a 0-argument macro

## `'+`

Value: `26` (int)

Callable with:

| Arg types   | Thimble equivalent |
| ----------- | ------------------ |
| ()          | [`arg4`](./thimble-builtins.md#arguments)\* |

\* Used as a magic value, not called as a 0-argument macro

## `'*`

Value: TBD, currently `32` (int)

Callable with: TBD

## `'-`

Value: TBD, currently `64` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| (list)        | [`flatten-once`](./thimble-builtins.md#flatten-once) |

## `'/`

Value: TBD, currently `20` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| (list)        | [`min`](./thimble-builtins.md#min) |

## `'%`

Value: `100` (int)

Callable with: TBD

## `'?`

Value: TBD, currently `50` (int)

Callable with:

| Arg types     | Thimble equivalent |
| ------------- | ------------------ |
| (int)         | [`zero?`](./thimble-builtins.md#zero) |
| (list)        | [`empty?`](./thimble-builtins.md#empty) |
| (any any ...) | [`recur`](./thimble-builtins.md#recur) |

## `'.`

Magic value: list of arguments to the current function

Thimble equivalent: [`arglist`](./thimble-builtins.md#arguments)

## `',`

Magic value: third argument to the current function

Thimble equivalent: [`arg3`](./thimble-builtins.md#arguments)
