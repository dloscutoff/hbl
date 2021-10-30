# Codepage

HBL's codepage has 16 characters, each representing one nybble (half a byte) of code. In other words, if the packed code is written out in hexadecimal, each codepage character represents one hex digit. The characters have been chosen to be as memorable as possible, both in their connection to the associated hex digits and in their connection to the associated builtins.

## Hex digits and their codepage characters

| Hex | Chr | Mnemonic |
| --- | --- | -------- |
| `0` | `0` | |
| `1` | `1` | |
| `2` | `2` | |
| `3` | `<` | ![The central pointy part of 3 looks like the less than sign](./pix/3.png) |
| `4` | `+` | ![The vertical and horizontal strokes of 4 form a plus sign](./pix/4.png) |
| `5` | `*` | 5-pointed star (in some fonts) |
| `6` | `-` | ![The middle part of 6 could look like a minus sign](./pix/6.png) |
| `7` | `/` | ![The angled stroke of 7 looks like a forward slash](./pix/7.png) |
| `8` | `%` | ![The two circles in the percent sign look like an 8](./pix/8.png) |
| `9` | `?` | ![9 looks like a question mark with the top part curled all the way around](./pix/9.png) |
| `A` | `.` | |
| `B` | `,` | |
| `C` | `(` | ![C looks like an open parenthesis](./pix/C.png) |
| `D` | `)` | ![D looks like a close parenthesis with an added stroke](./pix/D.png) |
| `E` | `'` | **E**scape character |
| `F` | newline | **F**inal character on a line |

## Codepage characters and their builtin values

| Chr | Value | Mnemonic |
| --- | ----- | -------- |
| `0` | `0`   | |
| `1` | `1`   | |
| `2` | `2`   | |
| `<` | `3`   | ![The central pointy part of 3 looks like the less than sign](./pix/3.png) |
| `+` | `4`   | ![The vertical and horizontal strokes of 4 form a plus sign](./pix/4.png) |
| `*` | `5`   | 5-pointed star (in some fonts) |
| `-` | `-1`  | ![Negative 1 uses a minus sign](./pix/-1.png) |
| `/` | `7`   | ![The angled stroke of 7 looks like a forward slash](./pix/7.png) |
| `%` | `10`  | ![The stroke and bottom circle in the percent sign look like a 10](./pix/10.png) |
| `?` | `()`  | Nil is falsey/questionable |
| `.` | arg1  | Represents the argument in jq, for instance |
| `,` | arg2  | Like a period, but extended |
