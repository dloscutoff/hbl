/** Half-Byte Lisp interpreter Copyright (C) 2021 David Loscutoff
  * <https://github.com/dloscutoff>
  *
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */

package hbl

class UnknownParenException(message: String) extends Exception(message)
class TokenException(message: String) extends Exception(message)

object Translator {
  def translateGolfed(parseTree: ParseNode): HBLAny = {
    parseTree match {
      case InternalNode(children, openParen, closeParen) => {
        (openParen, closeParen) match {
          case ("(", ")") => HBLList(children.map(translateGolfed).toVector)
          case ("'(", ")") =>
            HBLList(
              Builtins.quote,
              HBLList(children.map(translateGolfed).toVector)
            )
          case ("(", "')") => ??? // TODO: String literals
          case _ => throw UnknownParenException(s"$openParen $closeParen")
        }
      }
      case LeafNode(Token(atom)) => {
        atom match {
          case codepageValues(value) => value
          case _                     => throw TokenException(s"$atom")
        }
      }
    }
  }

  def translateExpanded(parseTree: ParseNode): HBLAny = {
    parseTree match {
      case InternalNode(children, openParen, closeParen) => {
        (openParen, closeParen) match {
          case ("(", ")") => HBLList(children.map(translateExpanded).toVector)
          case ("'(", ")") =>
            HBLList(
              Builtins.quote,
              HBLList(children.map(translateExpanded).toVector)
            )
          case _ => throw UnknownParenException(s"$openParen $closeParen")
        }
      }
      case LeafNode(Token(atom)) => {
        val intPattern = "-?\\d+".r
        val numberedArgPattern = "arg([1-9])".r
        val prevRefPattern = "(\\d+)prev".r
        atom match {
          case intPattern() => BigInt(atom)
          case numberedArgPattern(argnum) =>
            HBLList(Builtins.getLocal, BigInt(argnum))
          case prevRefPattern(offset) =>
            HBLList(Builtins.getPrevLine, offset.toInt)
          case namedBuiltins(builtin) => builtin
          case _                      => throw TokenException(s"$atom")
        }
      }
    }
  }

  val namedBuiltins: Map[String, HBLAny] = Map(
    // Values
    "nil" -> HBLList(Builtins.quote, Builtins.HBLNil),
    // Magic values
    "arglist" -> HBLList(Builtins.getLocals),
    "argcount" -> HBLList(Builtins.countLocals),
    "prev" -> HBLList(Builtins.getPrevLine),
    "this" -> HBLList(Builtins.getThisLine),
    "next" -> HBLList(Builtins.getNextLine),
    // Macros
    "quote" -> Builtins.quote,
    "get-local" -> Builtins.getLocal,
    "get-locals" -> Builtins.getLocals,
    "count-locals" -> Builtins.countLocals,
    "get-prev" -> Builtins.getPrevLine,
    "get-this" -> Builtins.getThisLine,
    "get-next" -> Builtins.getNextLine,
    "cond" -> Builtins.cond,
    "chain" -> Builtins.chain,
    "branch" -> Builtins.branch,
    "recur" -> Builtins.recur,
    // Functions
    "inc" -> Builtins.inc,
    "dec" -> Builtins.dec,
    "double" -> Builtins.double,
    "neg" -> Builtins.neg,
    "abs" -> Builtins.abs,
    "odd?" -> Builtins.oddQ,
    "zero?" -> Builtins.zeroQ,
    "1to" -> Builtins.oneTo,
    "head" -> Builtins.head,
    "tail" -> Builtins.tail,
    "length" -> Builtins.length,
    "reverse" -> Builtins.reverse,
    "sum" -> Builtins.sum,
    "product" -> Builtins.product,
    "flatten" -> Builtins.flatten,
    "max" -> Builtins.max,
    "sort" -> Builtins.sort,
    "last" -> Builtins.last,
    "init" -> Builtins.init,
    "flatten-once" -> Builtins.flattenOnce,
    "min" -> Builtins.min,
    "empty?" -> Builtins.emptyQ,
    "add" -> Builtins.add,
    "sub" -> Builtins.sub,
    "mul" -> Builtins.mul,
    "div" -> Builtins.div,
    "mod" -> Builtins.mod,
    "pow" -> Builtins.pow,
    "less?" -> Builtins.lessQ,
    "range" -> Builtins.range,
    "nth" -> Builtins.nth,
    "repeat" -> Builtins.repeat,
    "map" -> Builtins.map,
    "filter" -> Builtins.filter,
    "append" -> Builtins.append,
    "take" -> Builtins.take,
    "drop" -> Builtins.drop,
    "zip" -> Builtins.zip,
    "concat" -> Builtins.concat,
    "zip-with" -> Builtins.zipWith,
    "map-left" -> Builtins.mapLeft,
    "map-right" -> Builtins.mapRight,
    "cons" -> Builtins.cons
  )

  val codepageValues: Map[String, HBLAny] = Map(
    "0" -> BigInt(0),
    "1" -> BigInt(1),
    "2" -> BigInt(2),
    "<" -> BigInt(3),
    "+" -> BigInt(4),
    "*" -> BigInt(5),
    "-" -> BigInt(-1),
    "/" -> BigInt(7),
    "%" -> BigInt(10),
    "?" -> HBLList(Builtins.quote, Builtins.HBLNil),
    // Magic value: argument 1
    "." -> HBLList(Builtins.getLocal, BigInt(1)),
    // Magic value: argument 2
    "," -> HBLList(Builtins.getLocal, BigInt(2)),
    "'0" -> BigInt(16),
    "'1" -> BigInt(6),
    "'2" -> BigInt(8),
    "'<" -> BigInt(9),
    "'+" -> BigInt(26),
    "'*" -> BigInt(32), // TODO: Probably pick a different value
    "'-" -> BigInt(64), // TODO: Probably pick a different value
    "'/" -> BigInt(20), // TODO: Probably pick a different value
    "'%" -> BigInt(100),
    "'?" -> BigInt(50), // TODO: Probably pick a different value
    // Magic value: argument list
    "'." -> HBLList(Builtins.getLocals),
    // Magic value: argument 3
    "'," -> HBLList(Builtins.getLocal, BigInt(3))
  )

  val overloads = Map(
    BigInt(-1) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.getNextLine
        case arity: Int if arity >= 3 => Builtins.chain
      },
      {
        case Seq(x: BigInt)            => Builtins.dec
        case Seq(ls: HBLList)          => Builtins.flatten
        case Seq(x: BigInt, y: BigInt) => Builtins.sub
      }
    ),
    BigInt(0) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.getThisLine
      },
      {
        case Seq(x: BigInt)              => Builtins.oneTo
        case Seq(ls: HBLList)            => Builtins.length
        case Seq(x: BigInt, y: BigInt)   => Builtins.range
        case Seq(x: BigInt, ls: HBLList) => Builtins.take
      }
    ),
    BigInt(1) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList)                             => Builtins.head
        case Seq(x: BigInt, y: BigInt)                    => Builtins.pow
        case Seq(ls: HBLList, x: BigInt)                  => Builtins.nth
        case Seq(any: HBLAny, ls: HBLList)                => Builtins.cons
        case Seq(any1: HBLAny, any2: HBLAny, ls: HBLList) => Builtins.cons
        case Seq(any1: HBLAny, any2: HBLAny, any3: HBLAny, ls: HBLList) =>
          Builtins.cons
      }
    ),
    BigInt(2) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.generatePrevMacro(2)
        case arity: Int if arity >= 4 => Builtins.branch
      },
      {
        case Seq(x: BigInt)                              => Builtins.double
        case Seq(ls: HBLList)                            => Builtins.tail
        case Seq(x: BigInt, ls: HBLList)                 => Builtins.drop
        case Seq(ls1: HBLList, ls2: HBLList)             => Builtins.zip
        case Seq(fn: HBLAny, ls1: HBLList, ls2: HBLList) => Builtins.zipWith
      }
    ),
    BigInt(3) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.generatePrevMacro(3)
      },
      {
        case Seq(x: BigInt)                            => Builtins.neg
        case Seq(ls: HBLList)                          => Builtins.reverse
        case Seq(x: BigInt, y: BigInt)                 => Builtins.lessQ
        case Seq(fn: HBLAny, ls: HBLList, any: HBLAny) => Builtins.mapLeft
      }
    ),
    BigInt(4) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.generatePrevMacro(4)
      },
      {
        case Seq(x: BigInt)                       => Builtins.inc
        case Seq(ls: HBLList)                     => Builtins.sum
        case Seq(x: BigInt, y: BigInt)            => Builtins.add
        case Seq(ls1: HBLList, ls2: HBLList)      => Builtins.concat
        case Seq(x: BigInt, y: BigInt, z: BigInt) => Builtins.add
      }
    ),
    BigInt(5) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.generatePrevMacro(5)
      },
      {
        case Seq(ls: HBLList)                          => Builtins.product
        case Seq(x: BigInt, y: BigInt)                 => Builtins.mul
        case Seq(ls: HBLList, x: BigInt)               => Builtins.repeat
        case Seq(fn: HBLAny, ls: HBLList)              => Builtins.map
        case Seq(fn: HBLAny, any: HBLAny, ls: HBLList) => Builtins.mapRight
      }
    ),
    BigInt(6) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList)              => Builtins.last
        case Seq(any: HBLAny, ls: HBLList) => Builtins.append
      }
    ),
    BigInt(7) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(x: BigInt)            => Builtins.abs
        case Seq(ls: HBLList)          => Builtins.max
        case Seq(x: BigInt, y: BigInt) => Builtins.div
        //case Seq(fn: HBLAny, ls: HBLList) => Builtins.reduceLeft
        //case Seq(fn: HBLAny, any: HBLAny, ls: HBLList) => Builtins.foldLeft
      }
    ),
    BigInt(8) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      { case Seq(ls: HBLList) =>
        Builtins.init
      }
    ),
    BigInt(10) -> HBLOverloadedBuiltin(
      {
        case arity if arity == 0 => Builtins.countLocals
      },
      {
        case Seq(x: BigInt)               => Builtins.oddQ
        case Seq(ls: HBLList)             => Builtins.sort
        case Seq(x: BigInt, y: BigInt)    => Builtins.mod
        case Seq(fn: HBLAny, ls: HBLList) => Builtins.filter
      }
    ),
    BigInt(20) -> HBLOverloadedBuiltin( // TODO: Probably pick a different value
      // No macros yet
      arity => throw MatchError(arity),
      { case Seq(ls: HBLList) =>
        Builtins.min
      }
    ),
    BigInt(50) -> HBLOverloadedBuiltin( // TODO: Probably pick a different value
      {
        case arity: Int if arity >= 2 => Builtins.recur
      },
      {
        case Seq(x: BigInt)   => Builtins.zeroQ
        case Seq(ls: HBLList) => Builtins.emptyQ
      }
    ),
    BigInt(64) -> HBLOverloadedBuiltin( // TODO: Probably pick a different value
      // No macros yet
      arity => throw MatchError(arity),
      { case Seq(ls: HBLList) =>
        Builtins.flattenOnce
      }
    ),
    Builtins.HBLNil -> HBLOverloadedBuiltin(
      {
        case arity if arity == 0 => Builtins.getLocals
        case arity if arity == 1 => Builtins.recur
        case arity if arity > 1  => Builtins.cond
      },
      // No functions
      args => throw MatchError(args)
    )
  )
}
