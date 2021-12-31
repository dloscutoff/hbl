/** Half-Byte Lisp interpreter
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

import scala.collection.immutable.{NumericRange, SeqOps}
import scala.collection.mutable

class ArgumentException(message: String) extends Exception(message)
class TopLevelException(message: String) extends Exception(message)
class LineReferenceException(message: String) extends Exception(message)

type HBLAny = BigInt | HBLList | HBLBuiltin

class HBLList(vec: Vector[HBLAny], var lineNumber: Option[Int])
    extends Seq[HBLAny],
      SeqOps[HBLAny, Seq, HBLList] {
  def this(vec: Vector[HBLAny]) = this(vec, None)
  def this(values: HBLAny*) = this(values.toVector)
  def this(range: NumericRange[BigInt]) = this(range.toVector)

  override protected def fromSpecific(coll: IterableOnce[HBLAny]): HBLList =
    HBLList(coll.iterator.to(Vector))
  override protected def newSpecificBuilder: mutable.Builder[HBLAny, HBLList] =
    mutable.ArrayBuffer
      .newBuilder[HBLAny]
      .mapResult(elems => HBLList(elems.to(Vector)))
  override def empty: HBLList = HBLList(Vector.empty)
  override def iterator = vec.iterator
  override def length = vec.length
  override def apply(i: Int): HBLAny = vec(i)
  def repeat(i: Int): HBLList = HBLList(List.fill(i)(this).flatten.toVector)
  override def prepended[T >: HBLAny](item: T): HBLList =
    HBLList(vec.prepended[T](item).asInstanceOf[Vector[HBLAny]])

  override def prependedAll[T >: HBLAny](prefix: IterableOnce[T]): HBLList =
    HBLList(vec.prependedAll[T](prefix).asInstanceOf[Vector[HBLAny]])

  override def appended[T >: HBLAny](item: T): HBLList =
    HBLList(vec.appended[T](item).asInstanceOf[Vector[HBLAny]])

  override def appendedAll[T >: HBLAny](suffix: IterableOnce[T]): HBLList =
    HBLList(vec.appendedAll[T](suffix).asInstanceOf[Vector[HBLAny]])

  def map(fn: HBLAny => HBLAny): HBLList = HBLList(vec.map(fn))
  def zipHBL(that: HBLList): HBLList = HBLList(
    vec.zip(that).map((x, y) => HBLList(x, y))
  )
  def flattenOnce: HBLList = HBLList(vec.flatten {
    case sublist: HBLList => sublist
    case other => HBLList(other)
  })
  def flattenAll: HBLList = HBLList(vec.flatten {
    case sublist: HBLList => sublist.flattenAll
    case other => HBLList(other)
  })
  def sorted: HBLList = HBLList(vec.sortWith(Builtins.isLess))
  override def toString: String = vec.mkString("(", " ", ")")
}

object HBLList {
  def unapplySeq(ls: HBLList): Seq[HBLAny] = ls
}

sealed trait HBLBuiltin

case class HBLFunction(name: String, fn: Seq[HBLAny] => HBLAny)
    extends HBLBuiltin {
  def apply(args: Seq[HBLAny]): HBLAny = {
    try {
      fn(args)
    } catch {
      case matchError: MatchError =>
        throw ArgumentException(
          s"Wrong number or type of arguments for builtin function $name: ${args.mkString(", ")}"
        )
    }
  }
  override def toString: String = s"<$name>"
}

sealed trait HBLMacro extends HBLBuiltin {
  val name: String
  val mac: Seq[HBLAny] => Context ?=> HBLAny
  def apply(args: Seq[HBLAny])(using Context): HBLAny = {
    try {
      mac(args)
    } catch {
      case matchError: MatchError =>
        throw ArgumentException(
          s"Wrong number or type of arguments for builtin macro $name: ${args.mkString(", ")}"
        )
    }
  }
  override def toString: String = s":$name:"
}

case class HBLRewriteMacro(name: String, mac: Seq[HBLAny] => Context ?=> HBLAny)
    extends HBLMacro
case class HBLFinalMacro(name: String, mac: Seq[HBLAny] => Context ?=> HBLAny)
    extends HBLMacro

final class HBLOverloadedBuiltin(
    getMacro: Int => HBLMacro,
    getFunction: Seq[HBLAny] => HBLFunction
) {
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
}

object Builtins {
  ///////////////////////
  // Utility functions //
  ///////////////////////
  def isTruthy(item: HBLAny): Boolean = {
    item match {
      case x: BigInt => x != 0
      case ls: HBLList => ls.nonEmpty
      case fnOrMacro: HBLBuiltin => true
    }
  }

  def isLess(left: HBLAny, right: HBLAny): Boolean = {
    // Builtin < integer < list
    (left, right) match {
      case (_, fn: HBLBuiltin) => false
      case (fn: HBLBuiltin, _) => true
      case (x: BigInt, y: BigInt) => x < y
      case (x: BigInt, ls: HBLList) => true
      case (ls: HBLList, x: BigInt) => false
      case (ls1: HBLList, ls2: HBLList) =>
        if (ls2.isEmpty) {
          false
        } else if (ls1.isEmpty) {
          true
        } else if (ls1.head != ls2.head) {
          isLess(ls1.head, ls2.head)
        } else {
          isLess(ls1.tail, ls2.tail)
        }
      case _ => throw MatchError(left, right)
    }
  }

  def getRelativeProgramLine(
      relativeIndex: Int
  )(using context: Context): HBLAny = {
    val currentIndex = context.lineNumber match {
      case Some(number) => number
      case None => ???
    }
    Interpreter.programLines.length match {
      case programLength if programLength > 0 =>
        val absoluteIndex =
          Utils.mod(currentIndex + relativeIndex, programLength)
        Interpreter.programLines(absoluteIndex)
      case _ =>
        throw LineReferenceException(
          "Cannot reference another line while loading first line"
        )
    }
  }

  def generatePrevMacro(relativeIndex: Int): HBLMacro = {
    HBLFinalMacro(
      s"${relativeIndex}prev",
      args => getRelativeProgramLine(-relativeIndex)
    )
  }

  def branchRestructure(args: Seq[HBLAny]): HBLList = {
    if (args.length < 4) HBLList(args.toVector)
    else {
      val root = args.head
      val splitIndex = (args.length + 1) / 2
      val leftBranch = args.slice(1, splitIndex)
      val rightBranch = args.drop(splitIndex)
      if (leftBranch.length == 1)
        HBLList(root, leftBranch.head, branchRestructure(rightBranch))
      else
        HBLList(
          root,
          branchRestructure(leftBranch),
          branchRestructure(rightBranch)
        )
    }
  }

  ////////////
  // Values //
  ////////////

  val HBLNil = HBLList()

  ////////////
  // Macros //
  ////////////

  // Zero-argument macros
  val getLocals = HBLFinalMacro(
    "get-locals",
    args =>
      context ?=> {
        if (context.fn == None) {
          throw TopLevelException(
            "Cannot access args at top level, only within a function"
          )
        } else if (args.nonEmpty) {
          throw ArgumentException("get-locals does not take any arguments")
        } else {
          HBLList(context.locals.toVector)
        }
      }
  )

  val countLocals = HBLFinalMacro(
    "count-locals",
    args =>
      context ?=>
        if (context.fn == None) {
          throw TopLevelException(
            "Cannot access args at top level, only within a function"
          )
        } else if (args.nonEmpty) {
          throw ArgumentException("count-locals does not take any arguments")
        } else {
          BigInt(context.locals.length)
        }
  )

  val getThisLine = HBLFinalMacro(
    "get-this",
    args =>
      if (args.nonEmpty) {
        throw ArgumentException("get-this does not take any arguments")
      } else {
        getRelativeProgramLine(0)
      }
  )

  // One-argument macros
  val quote = HBLFinalMacro(
    "quote",
    args =>
      context ?=>
        args match {
          case Seq(ls: HBLList) =>
            ls.lineNumber = context.lineNumber
            ls
          case Seq(arg) => arg
        }
  )

  val getLocal = HBLFinalMacro(
    "get-local",
    { args => context ?=>
      val Seq(index: BigInt) = args
      if (context.fn == None) {
        throw TopLevelException(
          s"Cannot access args at top level, only within a function"
        )
      } else if (index < 1) {
        throw ArgumentException(
          s"Argument to get-local must be greater than 0 (not $index)"
        )
      } else if (index > context.locals.length) {
        throw ArgumentException(
          s"Not enough arguments to bind arg$index in user-defined function"
        )
      } else {
        context.locals(Utils.bigIntToInt(index - 1))
      }
    }
  )

  val getPrevLine = HBLFinalMacro(
    "get-prev",
    {
      case Seq(arg: BigInt) => getRelativeProgramLine(-Utils.bigIntToInt(arg))
      case Seq() => getRelativeProgramLine(-1)
    }
  )

  val getNextLine = HBLFinalMacro(
    "get-next",
    {
      case Seq(arg: BigInt) => getRelativeProgramLine(Utils.bigIntToInt(arg))
      case Seq() => getRelativeProgramLine(1)
    }
  )

  // Variadic macros
  val cond = HBLRewriteMacro(
    "cond",
    { args =>
      var Seq(testExpr: HBLAny, trueExpr: HBLAny, moreExprs*) = args
      var testVal = Interpreter.eval(testExpr)
      while (!isTruthy(testVal) && moreExprs.length > 1) {
        testExpr = moreExprs(0)
        trueExpr = moreExprs(1)
        moreExprs = moreExprs.drop(2)
        testVal = Interpreter.eval(testExpr)
      }
      if (isTruthy(testVal)) {
        trueExpr
      } else if (moreExprs.nonEmpty) {
        moreExprs.head
      } else {
        testExpr
      }
    }
  )

  val chain = HBLRewriteMacro("chain", _.reduceRight(HBLList(_, _)))

  val branch = HBLRewriteMacro("branch", branchRestructure(_))

  val recur = HBLFinalMacro(
    "recur",
    { args => context ?=>
      println("Calling recur builtin!!")
      context.fn match {
        case Some(fn: HBLList) =>
          Interpreter.eval(fn)(using
            context.withNewLocals(Interpreter.evalEach(args))
          )
        case None =>
          throw TopLevelException(
            "Cannot use recur at top level, only within a function"
          )
      }
    }
  )

  ///////////////
  // Functions //
  ///////////////

  // One-argument (int) functions
  val inc = HBLFunction("inc", { case Seq(x: BigInt) => x + 1 })
  val dec = HBLFunction("dec", { case Seq(x: BigInt) => x - 1 })
  val double = HBLFunction("double", { case Seq(x: BigInt) => x * 2 })
  val neg = HBLFunction("neg", { case Seq(x: BigInt) => -x })
  val abs = HBLFunction("abs", { case Seq(x: BigInt) => x.abs })
  val oddQ = HBLFunction("odd?", { case Seq(x: BigInt) => x mod 2 })
  val zeroQ =
    HBLFunction("zero?", { case Seq(x: BigInt) => if x == 0 then 1 else 0 })
  val oneTo =
    HBLFunction("1to", { case Seq(x: BigInt) => HBLList(BigInt(1) to x) })

  // One-argument (list) functions
  val head = HBLFunction(
    "head",
    { case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.head }
  )
  val tail = HBLFunction(
    "tail",
    { case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.tail }
  )
  val length =
    HBLFunction("length", { case Seq(ls: HBLList) => BigInt(ls.length) })
  val reverse = HBLFunction("reverse", { case Seq(ls: HBLList) => ls.reverse })
  val sum = HBLFunction(
    "sum",
    {
      case Seq(ls: HBLList) => {
        if (ls.isEmpty) then 0
        else
          ls.flattenAll.reduce((left: HBLAny, right: HBLAny) => {
            (left, right) match {
              case (x: BigInt, y: BigInt) => x + y
              case _ => throw MatchError(left, right)
            }
          })
      }
    }
  )
  val product = HBLFunction(
    "product",
    { case Seq(ls: HBLList) =>
      if (ls.isEmpty) then 1
      else
        ls.flattenAll.reduce {
          case (x: BigInt, y: BigInt) => x * y
          case (left, right) => throw MatchError(left, right)
        }
    }
  )
  val flatten =
    HBLFunction("flatten", { case Seq(ls: HBLList) => ls.flattenAll })
  val max = HBLFunction(
    "max",
    { case Seq(ls: HBLList) =>
      if (ls.isEmpty) then HBLNil
      else
        ls.flattenAll.reduce((left, right) =>
          if (isLess(right, left)) then left else right
        )
    }
  )
  val sort = HBLFunction("sort", { case Seq(ls: HBLList) => ls.sorted })
  val last = HBLFunction(
    "last",
    { case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.last }
  )
  val init = HBLFunction(
    "init",
    { case Seq(ls: HBLList) => if ls.isEmpty then HBLNil else ls.init }
  )
  val flattenOnce =
    HBLFunction("flatten-once", { case Seq(ls: HBLList) => ls.flattenOnce })
  val min = HBLFunction(
    "min",
    {
      case Seq(ls: HBLList) =>
        if (ls.isEmpty) then HBLNil
        else
          ls.flattenAll.reduce((left, right) =>
            if (isLess(right, left)) then right else left
          )

    }
  )
  val emptyQ = HBLFunction(
    "empty?",
    { case Seq(ls: HBLList) => if ls.isEmpty then 1 else 0 }
  )

  // Two-argument (int, int) functions
  val sub = HBLFunction("sub", { case Seq(x: BigInt, y: BigInt) => x - y })
  val mul = HBLFunction("mul", { case Seq(x: BigInt, y: BigInt) => x * y })
  val div = HBLFunction("div", { case Seq(x: BigInt, y: BigInt) => x / y })
  val mod =
    HBLFunction("mod", { case Seq(x: BigInt, y: BigInt) => Utils.mod(x, y) })
  val pow = HBLFunction(
    "pow",
    { case Seq(x: BigInt, y: BigInt) => x.pow(Utils.bigIntToInt(y)) }
  )
  val lessQ = HBLFunction(
    "less?",
    { case Seq(x: BigInt, y: BigInt) => if x < y then 1 else 0 }
  )
  val range =
    HBLFunction("range", { case Seq(x: BigInt, y: BigInt) => HBLList(x to y) })

  // Two-argument (list, int) functions
  val nth = HBLFunction(
    "nth",
    { case Seq(ls: HBLList, x: BigInt) =>
      if x > 0 && x <= ls.length then ls(Utils.bigIntToInt(x - 1)) else HBLNil
    }
  )
  val repeat = HBLFunction(
    "repeat",
    { case Seq(ls: HBLList, x: BigInt) => ls.repeat(Utils.bigIntToInt(x)) }
  )

  // Two-argument (any, list) functions
  val map = HBLFunction(
    "map",
    { case Seq(fn: HBLAny, ls: HBLList) =>
      ls.map(item => Interpreter.callFunction(fn, Seq(item)))
    }
  )
  val filter = HBLFunction(
    "filter",
    { case Seq(fn: HBLAny, ls: HBLList) =>
      ls.filter(item => isTruthy(Interpreter.callFunction(fn, Seq(item))))
    }
  )
  val append = HBLFunction(
    "append",
    { case Seq(item: HBLAny, ls: HBLList) => ls.appended(item) }
  )

  // Two-argument (int, list) functions
  val take = HBLFunction(
    "take",
    { case Seq(x: BigInt, ls: HBLList) => ls.take(Utils.bigIntToInt(x)) }
  )
  val drop = HBLFunction(
    "drop",
    { case Seq(x: BigInt, ls: HBLList) => ls.drop(Utils.bigIntToInt(x)) }
  )

  // Two-argument (list, list) functions
  val zip = HBLFunction(
    "zip",
    { case Seq(ls1: HBLList, ls2: HBLList) => ls1.zipHBL(ls2) }
  )
  val concat = HBLFunction(
    "concat",
    { case Seq(ls1: HBLList, ls2: HBLList) => ls1.appendedAll(ls2) }
  )

  // Three-argument (any, any, list) functions
  val mapRight = HBLFunction(
    "map-right",
    { case Seq(fn: HBLAny, left: HBLAny, ls: HBLList) =>
      ls.map(item => Interpreter.callFunction(fn, Seq(left, item)))
    }
  )

  // Three-argument (any, list, any) functions
  val mapLeft = HBLFunction(
    "map-left",
    { case Seq(fn: HBLAny, ls: HBLList, right: HBLAny) =>
      ls.map(item => Interpreter.callFunction(fn, Seq(item, right)))
    }
  )

  // Three-argument (any, list, list) functions
  val zipWith = HBLFunction(
    "zip-with",
    { case Seq(fn: HBLAny, ls1: HBLList, ls2: HBLList) =>
      HBLList(
        ls1
          .zip(ls2)
          .map((x, y) => Interpreter.callFunction(fn, Seq(x, y)))
          .toVector
      )
    }
  )

  // Variadic functions
  val add = HBLFunction(
    "add",
    {
      case Seq(x: BigInt, y: BigInt) => x + y
      case Seq(x: BigInt, y: BigInt, z: BigInt) => x + y + z
    }
  )

  val cons = HBLFunction(
    "cons",
    {
      case args: Seq[HBLAny] if args.length >= 2 =>
        val (ls: HBLList, items: Seq[HBLAny]) =
          (args.last, args.take(args.length - 1))
        ls.prependedAll(items)
    }
  )
}
