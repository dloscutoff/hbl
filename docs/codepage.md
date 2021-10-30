# Codepage

HBL's codepage has 16 characters, each representing one nybble (half a byte) of code. In other words, if the packed code is written out in hexadecimal, each codepage character represents one hex digit. The characters have been chosen to be as memorable as possible, both in their connection to the associated hex digits and in their connection to the associated builtins.

## Hex digits and their codepage characters

| Hex | Chr | Mnemonic |
| --- | --- | -------- |
| `0` | `0` | |
| `1` | `1` | |
| `2` | `2` | |
| `3` | `<` | <img src="./pix/3.png" alt="The central pointy part of 3 looks like the less than sign" width="50" height="50" /> |
| `4` | `+` | <img src="./pix/4.png" alt="The vertical and horizontal strokes of 4 form a plus sign" width="50" height="50" /> |
| `5` | `*` | 5-pointed star (in some fonts) |
| `6` | `-` | <img src="./pix/6.png" alt="The middle part of 6 could look like a minus sign" width="50" height="50" /> |
| `7` | `/` | <img src="./pix/7.png" alt="The angled stroke of 7 looks like a forward slash" width="50" height="50" /> |
| `8` | `%` | <img src="./pix/8.png" alt="The two circles in the percent sign look like an 8" width="50" height="50" /> |
| `9` | `?` | <img src="./pix/9.png" alt="9 looks like a question mark with the top part curled all the way around" width="50" height="50" /> |
| `A` | `.` | |
| `B` | `,` | |
| `C` | `(` | <img src="./pix/C.png" alt="C looks like an open parenthesis" width="50" height="50" /> |
| `D` | `)` | <img src="./pix/D.png" alt="D looks like a close parenthesis with an added stroke" width="50" height="50" /> |
| `E` | `'` | **E**scape character |
| `F` | newline | **F**inal character on a line |

## Codepage characters and their builtin values

| Chr | Value | Mnemonic |
| --- | ----- | -------- |
| `0` | `0`   | |
| `1` | `1`   | |
| `2` | `2`   | |
| `<` | `3`   | <img src="./pix/3.png" alt="The central pointy part of 3 looks like the less than sign" width="50" height="50" /> |
| `+` | `4`   | <img src="./pix/4.png" alt="The vertical and horizontal strokes of 4 form a plus sign" width="50" height="50" /> |
| `*` | `5`   | 5-pointed star (in some fonts) |
| `-` | `-1`  | <img src="./pix/-1.png" alt="Negative 1 uses a minus sign" width="50" height="50" /> |
| `/` | `7`   | <img src="./pix/7.png" alt="The angled stroke of 7 looks like a forward slash" width="50" height="50" /> |
| `%` | `10`  | <img src="./pix/10.png" alt="The stroke and bottom circle in the percent sign look like a 10" width="50" height="50" /> |
| `?` | `()`  | Nil is falsey/questionable |
| `.` | arg1  | Represents the argument in jq, for instance |
| `,` | arg2  | Like a period, but extended |
