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

import scala.collection.mutable.ArrayBuffer

class NotCallableException(message: String) extends Exception(message)
class MissingOverloadException(message: String) extends Exception(message)

//case class Context(lines: Seq[HBLAny], lineNumber: Option[Int], fn: Option[HBLList], locals: Seq[HBLAny]) {
case class Context(lineNumber: Option[Int], fn: Option[HBLList], locals: Seq[HBLAny]) {
  def withNewLocals(newLocals: Seq[HBLAny]): Context =
    Context(lineNumber, fn, newLocals)
}

object Context {
  def apply(): Context = Context(None, None, Seq())
  def apply(lineNumber: Int): Context = Context(Some(lineNumber), None, Seq())
  def apply(lineNumber: Int, fn: HBLList, locals: Seq[HBLAny]): Context =
    Context(Some(lineNumber), Some(fn), locals)
}

object Interpreter {
  val programLines: ArrayBuffer[HBLAny] = new ArrayBuffer

  def loadGolfedProgram(parseTree: InternalNode): Unit = {
    val InternalNode(parsedLines, _, _) = parseTree
    for ((line, lineNumber) <- parsedLines.zipWithIndex) {
      given Context = Context(lineNumber)
      programLines.append(eval(Translator.translateGolfed(line)))
    }
  }

  def loadExpandedProgram(parseTree: InternalNode): Unit = {
    val InternalNode(parsedLines, _, _) = parseTree
    for ((line, lineNumber) <- parsedLines.zipWithIndex) {
      given Context = Context(lineNumber)
      programLines.append(eval(Translator.translateExpanded(line)))
    }
  }

  def runProgram(argVals: Seq[HBLAny] = List()): Option[HBLAny] = {
    if (!programLines.isEmpty) {
      programLines.last match {
        case mainFn: HBLList => {
          given Context = Context(programLines.length - 1, mainFn, argVals)
          Some(eval(programLines.last))
        }
        case _ => Some(programLines.last)
      }
    } else None
  }

  def eval(expr: HBLAny)(using context: Context): HBLAny = {
    //println(s" Eval: $expr")
    expr match {
      // An s-expression is some kind of function or macro call, where the
      // head of the expression is the function/macro and the tail is the
      // list of arguments
      case HBLList(exprHead: HBLAny, args*) => {
        val headVal = eval(exprHead)
        // See if the head is a macro of some kind
        val macroVal: Option[HBLMacro] = headVal match {
          // A literal value that's overloaded as a macro with an
          // arity matching the current number of arguments
          // TODO: warn when trying to do this in expanded mode?
          case Translator.overloads(overloadedBuiltin) =>
            args.length match {
              case overloadedBuiltin(builtinMacro) => Some(builtinMacro)
              case _ => None
            }
          // Or a direct reference to a builtin macro
          case builtinMacro: HBLMacro => Some(builtinMacro)
          case _ => None
        }
        if (macroVal.isDefined) {
          // This is a macro call
          // Rewrite the expression and re-evaluate if appropriate
          macroVal.get match {
            case Builtins.recur => {
              // Recursive call: evaluate the same function again in the context
              // of the new arguments
              context.fn match {
                case Some(currentFunc: HBLList) => {
                  given Context = context.withNewLocals(evalEach(args)(using context))
                  eval(currentFunc)
                }
                case None => throw TopLevelException("Cannot use recur at top level, only within a function")
              }
            }
            // A final macro just does a rewrite and returns the result
            case finalMacro: HBLFinalMacro => finalMacro(args, context)
            // A rewrite macro does a rewrite and then evaluates the result again
            case rewriteMacro: HBLRewriteMacro => eval(rewriteMacro(args, context))
          }
        } else {
          // This is a function call
          val argVals = evalEach(args)
          // The head should be a function of some kind
          headVal match {
            // A literal value that's overloaded as a function for argtypes
            // matching the types of the current arguments
            // TODO: warn when trying to do this in expanded mode?
            case Translator.overloads(overloadedBuiltin) =>
              argVals match {
                case overloadedBuiltin(builtinFunction) => builtinFunction(argVals)
                case _ => throw MissingOverloadException(s"$headVal with args ${argVals.mkString(", ")}")
              }
            // A direct reference to a builtin function
            case builtinFunction: HBLFunction => builtinFunction(argVals)
            // A list representing a user-defined function
            case userFunction: HBLList => {
              given Context = Context(userFunction.lineNumber, Some(userFunction), argVals)
              eval(userFunction)
            }
            // Any other value is not a callable value
            case other => throw NotCallableException(s"$other")
          }
        }
      }
      // Evaluating an empty list is a reference to the previous line
      case HBLList() => Builtins.getPrevLine(Seq(), context)
      // All other values (integers, builtins) evaluate to themselves 
      case value: HBLAny => value
    }
  }
  
  def evalEach(args: Seq[HBLAny])(using context: Context): Seq[HBLAny] = args.map(arg => eval(arg))

  def quoteEach(args: Seq[HBLAny]): Seq[HBLAny] = args.map(arg => HBLList(Builtins.quote, arg)) 
  
  def callFunction(fn: HBLAny, argVals: Seq[HBLAny]): HBLAny = {
    fn match {
      // A list representing a user-defined function
      case userFunction: HBLList if !userFunction.isEmpty => {
        given Context = Context(userFunction.lineNumber, Some(userFunction), argVals)
        eval(userFunction)
      }
      // A direct reference to a builtin function or a literal value
      // that's overloaded as a function
      case builtin => {
        given Context = Context()
        // The arguments have already been evaluated, so quote each of them
        // to prevent them from being evaluated twice
        eval(HBLList((builtin +: quoteEach(argVals)).toVector))
      }
    }
  }
}
