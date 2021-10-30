/**
 * Half-Byte Lisp interpreter
 * Copyright (C) 2021 David Loscutoff <https://github.com/dloscutoff>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package hbl

import scala.collection.immutable.NumericRange

class ArgumentException(message: String) extends Exception(message)
class TopLevelException(message: String) extends Exception(message)
class LineReferenceException(message: String) extends Exception(message)

type HBLAny = BigInt | HBLList | HBLBuiltin

class HBLList(vec: Vector[HBLAny], var lineNumber: Option[Int]) extends Seq[HBLAny] {
  def this(vec: Vector[HBLAny]) = this(vec, None)
  def this(values: HBLAny*) = this(values.toVector)
  def this(range: NumericRange[BigInt]) = this(range.toVector)
  override def apply(i: Int): HBLAny = vec(i)
  override def iterator: Iterator[HBLAny] = vec.iterator
  override def length: Int = vec.length
  override def tail: HBLList = HBLList(vec.tail)
  override def init: HBLList = HBLList(vec.init)
  override def reverse: HBLList = HBLList(vec.reverse)
  def repeat(i: Int): HBLList = HBLList(List.fill(i)(this).flatten.toVector)
  override def prepended[T >: HBLAny](item: T): HBLList = {
    HBLList(vec.prepended[T](item).asInstanceOf[Vector[HBLAny]])
  }
  override def prependedAll[T >: HBLAny](prefix: IterableOnce[T]): HBLList = {
    HBLList(vec.prependedAll[T](prefix).asInstanceOf[Vector[HBLAny]])
  }
  override def appended[T >: HBLAny](item: T): HBLList = {
    HBLList(vec.appended[T](item).asInstanceOf[Vector[HBLAny]])
  }
  override def appendedAll[T >: HBLAny](suffix: IterableOnce[T]): HBLList = {
    HBLList(vec.appendedAll[T](suffix).asInstanceOf[Vector[HBLAny]])
  }
  override def take(num: Int): HBLList = HBLList(vec.take(num))
  override def drop(num: Int): HBLList = HBLList(vec.drop(num))
  override def map[T](fn: HBLAny => T): Seq[T] = vec.map(fn)
  override def filter(fn: HBLAny => Boolean): HBLList = HBLList(vec.filter(fn))
  override def reduce[T >: HBLAny](op: (T, T) => T): T = vec.reduce(op)
  def map(fn: HBLAny => HBLAny): HBLList = HBLList(vec.map(fn))
  def flattenOnce: HBLList = HBLList(vec.flatten(element => {
    element match {
      case sublist: HBLList => sublist
      case other => HBLList(other)
    }
  }))
  def flattenAll: HBLList = HBLList(vec.flatten(element => {
    element match {
      case sublist: HBLList => sublist.flattenAll
      case other => HBLList(other)
    }
  }))
  override def toString: String = vec.mkString("(", " ", ")")
}

object HBLList {
  def unapplySeq(ls: HBLList): Option[Seq[HBLAny]] = Some(ls)
}

trait HBLBuiltin

case class HBLFunction(name: String, fn: Seq[HBLAny] => HBLAny) extends HBLBuiltin {
  def apply(args: Seq[HBLAny]): HBLAny = {
    try {
      fn(args)
    } catch {
      case matchError: MatchError => throw ArgumentException(
        s"Wrong number or type of arguments for builtin function $name: ${args.mkString(", ")}")
    }
  }
  override def toString: String = s"<$name>"
}

trait HBLMacro extends HBLBuiltin {
  val name: String
  val mac: (Seq[HBLAny], Context) => HBLAny
  def apply(args: Seq[HBLAny], context: Context): HBLAny = {
    try {
      mac(args, context)
    } catch {
      case matchError: MatchError => throw ArgumentException(
        s"Wrong number or type of arguments for builtin macro $name: ${args.mkString(", ")}")
    }
  }
  override def toString: String = s":$name:"
}

case class HBLRewriteMacro(name: String, mac: (Seq[HBLAny], Context) => HBLAny) extends HBLMacro
case class HBLFinalMacro(name: String, mac: (Seq[HBLAny], Context) => HBLAny) extends HBLMacro

class HBLOverloadedBuiltin(getMacro: Int => HBLMacro, getFunction: Seq[HBLAny] => HBLFunction) {
  // Given an Int, unapply returns a macro if this builtin has a macro overload at that arity
  def unapply(arity: Int): Option[HBLMacro] = {
    try {
      Some(getMacro(arity))
    } catch {
      case matchError: MatchError => None
    }
  }
  // Given a Seq[HBLAny] arglist, unapply returns a function if this builtin as a function
  // overload matching those argtypes
  def unapply(args: Seq[HBLAny]): Option[HBLFunction] = {
    try {
      Some(getFunction(args))
    } catch {
      case matchError: MatchError => None
    }
  }
  //override def toString: String = s"<fn $name>"  // TODO: how to display these?
}

object Builtins {
  ///////////////////////
  // Utility functions //
  ///////////////////////
  def bigIntToInt(x: BigInt): Int = {
    if (x > Int.MaxValue)
      Int.MaxValue
    else if (x < Int.MinValue)
      Int.MinValue
    else
      x.toInt
  }

  def isTruthy(item: HBLAny): Boolean = {
    item match {
      case x: BigInt => x != 0
      case ls: HBLList => !ls.isEmpty
      case fnOrMacro: HBLBuiltin => true
    }
  }

  // Scala's % and BigInt.mod are both wrong. Fight me.
  def correctMod(x: Int, y: Int): Int = ((x % y) + y) % y
  def correctMod(x: BigInt, y: BigInt): BigInt = ((x % y) + y) % y

  def getRelativeProgramLine(relativeIndex: Int, context: Context): HBLAny = {
    val currentIndex = context.lineNumber match {
      case Some(number) => number
      case None => ???
    }
    Interpreter.programLines.length match {
      case programLength if programLength > 0 => {
        val absoluteIndex = correctMod(currentIndex + relativeIndex, programLength)
        Interpreter.programLines(absoluteIndex)
      }
      case _ => throw LineReferenceException("Cannot reference another line while loading first line")
    }
  }

  def generatePrevMacro(relativeIndex: Int): HBLMacro = {
    HBLFinalMacro(s"${relativeIndex}prev", (args: Seq[HBLAny], context: Context) => {
      getRelativeProgramLine(-relativeIndex, context)
    })
  }

  ////////////
  // Values //
  ////////////

  val HBLNil = HBLList()

  ////////////
  // Macros //
  ////////////

  // Zero-argument macros
  val getLocals = HBLFinalMacro("get-locals", (args: Seq[HBLAny], context: Context) => {
    if (context.fn == None) {
      throw TopLevelException("Cannot access args at top level, only within a function")
    } else if (!args.isEmpty) {
      throw ArgumentException("get-locals does not take any arguments")
    } else {
      HBLList(context.locals.toVector)
    }
  })

  val countLocals = HBLFinalMacro("count-locals", (args: Seq[HBLAny], context: Context) => {
    if (context.fn == None) {
      throw TopLevelException("Cannot access args at top level, only within a function")
    } else if (!args.isEmpty) {
      throw ArgumentException("count-locals does not take any arguments")
    } else {
      BigInt(context.locals.length)
    }
  })

  val getThisLine = HBLFinalMacro("get-this", (args: Seq[HBLAny], context: Context) => {
    if (!args.isEmpty) {
      throw ArgumentException("get-this does not take any arguments")
    } else {
      getRelativeProgramLine(0, context)
    }
  })

  // One-argument macros
  val quote = HBLFinalMacro("quote", (args: Seq[HBLAny], context: Context) => {
    val Seq(arg) = args
    arg match {
      case ls: HBLList => {
        ls.lineNumber = context.lineNumber
        ls
      }
      case _ => arg
    }
  })

  val getLocal = HBLFinalMacro("get-local", (args: Seq[HBLAny], context: Context) => {
    val Seq(index: BigInt) = args
    if (context.fn == None) {
      throw TopLevelException(s"Cannot access args at top level, only within a function")
    } else if (index < 1) {
      throw ArgumentException(s"Argument to get-local must be greater than 0 (not $index)")
    } else if (index > context.locals.length) {
      throw ArgumentException(s"Not enough arguments to bind arg$index in user-defined function")
    } else {
      context.locals(bigIntToInt(index - 1))
    }
  })

  val getPrevLine = HBLFinalMacro("get-prev", (args: Seq[HBLAny], context: Context) => {
    getRelativeProgramLine(args match {
      case Seq(arg: BigInt) => -bigIntToInt(arg)
      case Seq() => -1
    }, context)
  })

  // Variadic macros
  val cond = HBLRewriteMacro("cond", (args: Seq[HBLAny], context: Context) => {
    var Seq(testExpr: HBLAny, trueExpr: HBLAny, moreExprs*) = args
    var testVal = Interpreter.eval(testExpr)(using context)
    while (!isTruthy(testVal) && moreExprs.length > 1) {
      testExpr = moreExprs(0)
      trueExpr = moreExprs(1)
      moreExprs = moreExprs.drop(2)
      testVal = Interpreter.eval(testExpr)(using context)
    }
    if (isTruthy(testVal)) {
      trueExpr
    } else if (!moreExprs.isEmpty) {
      moreExprs.head
    } else {
      testExpr
    }
  })

  val chain = HBLRewriteMacro("chain", (args: Seq[HBLAny], context: Context) => {
    args.reduceRight(HBLList(_, _))
  })

  val recur = HBLFinalMacro("recur", (args: Seq[HBLAny], context: Context) => {
    println("Calling recur builtin!!")
    context.fn match {
      case Some(fn: HBLList) => {
        given Context = context.withNewLocals(Interpreter.evalEach(args)(using context))
        Interpreter.eval(fn)
      }
      case None => throw TopLevelException("Cannot use recur at top level, only within a function")
    }
  })

  ///////////////
  // Functions //
  ///////////////

  // One-argument (int) functions
  val inc = HBLFunction("inc", {case Seq(x: BigInt) => x + 1})
  val dec = HBLFunction("dec", {case Seq(x: BigInt) => x - 1})
  val double = HBLFunction("double", {case Seq(x: BigInt) => x * 2})
  val neg = HBLFunction("neg", {case Seq(x: BigInt) => -x})
  val abs = HBLFunction("abs", {case Seq(x: BigInt) => x.abs})
  val oddQ = HBLFunction("odd?", {case Seq(x: BigInt) => x mod 2})
  val zeroQ = HBLFunction("zero?", {case Seq(x:BigInt) => if x == 0 then 1 else 0})
  val oneTo = HBLFunction("1to", {case Seq(x: BigInt) => HBLList(BigInt(1) to x)})

  // One-argument (list) functions
  val head = HBLFunction("head", {case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.head})
  val tail = HBLFunction("tail", {case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.tail})
  val length = HBLFunction("length", {case Seq(ls: HBLList) => BigInt(ls.length)})
  val reverse = HBLFunction("reverse", {case Seq(ls: HBLList) => ls.reverse})
  val sum = HBLFunction("sum", {case Seq(ls: HBLList) => {
    if (ls.isEmpty) then 0 else ls.flattenAll.reduce((left: HBLAny, right: HBLAny) => {
      (left, right) match {
        case (x: BigInt, y: BigInt) => x + y
        case _ => throw MatchError(left, right)
      }
    })
  }})
  val product = HBLFunction("product", {case Seq(ls: HBLList) => {
    if (ls.isEmpty) then 1 else ls.flattenAll.reduce((left: HBLAny, right: HBLAny) => {
      (left, right) match {
        case (x: BigInt, y: BigInt) => x * y
        case _ => throw MatchError(left, right)
      }
    })
  }})
  val flatten = HBLFunction("flatten", {case Seq(ls: HBLList) => ls.flattenAll})
  val last = HBLFunction("last", {case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.last})
  val init = HBLFunction("init", {case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.init})
  val flattenOnce = HBLFunction("flatten-once", {case Seq(ls: HBLList) => ls.flattenOnce})
  val emptyQ = HBLFunction("empty?", {case Seq(ls: HBLList) => if ls.isEmpty then 1 else 0})

  // Two-argument (int, int) functions
  val sub = HBLFunction("sub", {case Seq(x: BigInt, y: BigInt) => x - y})
  val mul = HBLFunction("mul", {case Seq(x: BigInt, y: BigInt) => x * y})
  val div = HBLFunction("div", {case Seq(x: BigInt, y: BigInt) => x / y})
  val mod = HBLFunction("mod", {case Seq(x: BigInt, y: BigInt) => correctMod(x, y)})
  val pow = HBLFunction("pow", {case Seq(x: BigInt, y: BigInt) => x.pow(bigIntToInt(y))})
  val lessQ = HBLFunction("less?", {case Seq(x: BigInt, y: BigInt) => if x < y then 1 else 0})
  val range = HBLFunction("range", {case Seq(x: BigInt, y: BigInt) => HBLList(x to y)})

  // Two-argument (list, int) functions
  val nth = HBLFunction("nth", {
    case Seq(ls: HBLList, x: BigInt) =>
      if x > 0 && x <= ls.length then ls(bigIntToInt(x - 1)) else HBLNil
  })
  val repeat = HBLFunction("repeat", {case Seq(ls: HBLList, x: BigInt) => ls.repeat(bigIntToInt(x))})

  // Two-argument (any, list) functions
  val map = HBLFunction("map", {
    case Seq(fn: HBLAny, ls: HBLList) =>
      ls.map(item => Interpreter.callFunction(fn, Seq(item)))
  })
  val filter = HBLFunction("filter", {
    case Seq(fn: HBLAny, ls: HBLList) => // TODO
      ls.filter(item => isTruthy(Interpreter.callFunction(fn, Seq(item))))
  })
  val append = HBLFunction("append", {case Seq(item: HBLAny, ls: HBLList) => ls.appended(item)})

  // Two-argument (int, list) functions
  val take = HBLFunction("take", {case Seq(x: BigInt, ls: HBLList) => ls.take(bigIntToInt(x))})
  val drop = HBLFunction("drop", {case Seq(x: BigInt, ls: HBLList) => ls.drop(bigIntToInt(x))})

  // Two-argument (list, list) functions
  val concat = HBLFunction("concat", {case Seq(ls1: HBLList, ls2: HBLList) => ls1.appendedAll(ls2)})

  // Variadic functions
  val add = HBLFunction("add", {
    case Seq(x: BigInt, y: BigInt) => x + y
    case Seq(x: BigInt, y: BigInt, z: BigInt) => x + y + z
  })

  val cons = HBLFunction("cons", {
    case args: Seq[HBLAny] if args.length >= 2 =>
      val (ls: HBLList, items: Seq[HBLAny]) = (args.last, args.take(args.length - 1))
      ls.prependedAll(items)
  })

  // Overloaded builtins for the golfed version
  val overloadedBuiltins = Map(
    BigInt(-1) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity >= 3 => Builtins.chain
      },
      {
        case Seq(x: BigInt) => Builtins.dec
        case Seq(ls: HBLList) => Builtins.flatten
        case Seq(x: BigInt, y: BigInt) => Builtins.sub
      }
    ),
    BigInt(0) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => Builtins.getThisLine
      },
      {
        case Seq(x: BigInt) => Builtins.oneTo
        case Seq(ls: HBLList) => Builtins.length
        case Seq(x: BigInt, y: BigInt) => Builtins.range
        case Seq(x: BigInt, ls: HBLList) => Builtins.take
      }
    ),
    BigInt(1) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList) => Builtins.head
        case Seq(x: BigInt, y: BigInt) => Builtins.pow
        case Seq(ls: HBLList, x: BigInt) => Builtins.nth
        case Seq(any: HBLAny, ls: HBLList) => Builtins.cons
        case Seq(any1: HBLAny, any2: HBLAny, ls: HBLList) => Builtins.cons
        case Seq(any1: HBLAny, any2: HBLAny, any3: HBLAny, ls: HBLList) => Builtins.cons
      }
    ),
    BigInt(2) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => generatePrevMacro(2)
      },
      {
        case Seq(x: BigInt) => Builtins.double
        case Seq(ls: HBLList) => Builtins.tail
        case Seq(any: BigInt, ls: HBLList) => Builtins.drop
        //case Seq(ls1: HBLList, ls2: HBLList) => Builtins.zip
        //case Seq(any: HBLAny, ls1: HBLList, ls2: HBLList) => Builtins.zipWith
      }
    ),
    BigInt(3) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => generatePrevMacro(3)
      },
      {
        case Seq(x: BigInt) => Builtins.neg
        case Seq(ls: HBLList) => Builtins.reverse
        case Seq(x: BigInt, y: BigInt) => Builtins.lessQ
        //case Seq(any1: HBLAny, ls: HBLList, any2: HBLAny) => Builtins.mapLeft
      }
    ),
    BigInt(4) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => generatePrevMacro(4)
      },
      {
        case Seq(x: BigInt) => Builtins.inc
        case Seq(ls: HBLList) => Builtins.sum
        case Seq(x: BigInt, y: BigInt) => Builtins.add
        case Seq(ls1: HBLList, ls2: HBLList) => Builtins.concat
        case Seq(x: BigInt, y: BigInt, z: BigInt) => Builtins.add
        //case Seq(any1: HBLAny, ls: HBLList, any2: HBLAny) => Builtins.mapLeft
      }
    ),
    BigInt(5) -> HBLOverloadedBuiltin(
      {
        case arity: Int if arity == 0 => generatePrevMacro(5)
      },
      {
        case Seq(ls: HBLList) => Builtins.product
        case Seq(x: BigInt, y: BigInt) => Builtins.mul
        case Seq(ls: HBLList, x: BigInt) => Builtins.repeat
        case Seq(any: HBLAny, ls: HBLList) => Builtins.map
        //case Seq(any1: HBLAny, any2: HBLAny, ls: HBLList) => Builtins.mapRight
      }
    ),
    BigInt(6) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList) => Builtins.last
        case Seq(any: HBLAny, ls: HBLList) => Builtins.append
      }
    ),
    BigInt(7) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(x: BigInt) => Builtins.abs
        //case Seq(ls: HBLList) => Builtins.min
        case Seq(x: BigInt, y: BigInt) => Builtins.div
        //case Seq(any: HBLAny, ls: HBLList) => Builtins.reduceLeft
        //case Seq(any1: HBLAny, any2: HBLAny, ls: HBLList) => Builtins.foldLeft
      }
    ),
    BigInt(8) -> HBLOverloadedBuiltin(
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList) => Builtins.init
      }
    ),
    BigInt(10) -> HBLOverloadedBuiltin(
      {
        case arity if arity == 0 => Builtins.countLocals
      },
      {
        case Seq(x: BigInt) => Builtins.oddQ
        case Seq(x: BigInt, y: BigInt) => Builtins.mod
        case Seq(any: HBLAny, ls: HBLList) => Builtins.filter
      }
    ),
    BigInt(50) -> HBLOverloadedBuiltin(  // TODO: Probably pick a different value
      {
        case arity: Int if arity >= 2 => Builtins.recur
      },
      {
        case Seq(x: BigInt) => Builtins.zeroQ
        case Seq(ls: HBLList) => Builtins.emptyQ
      }
    ),
    BigInt(64) -> HBLOverloadedBuiltin(  // TODO: Probably pick a different value
      // No macros yet
      arity => throw MatchError(arity),
      {
        case Seq(ls: HBLList) => Builtins.flattenOnce
      }
    ),
    HBLNil -> HBLOverloadedBuiltin(
      {
        case arity if arity == 0 => Builtins.getLocals
        case arity if arity == 1 => Builtins.recur
        case arity if arity > 1 => Builtins.cond
      },
      // No functions
      args => throw MatchError(args)
    ),
  )
}
