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

enum FileFormat {
  case Raw
  case ASCII
  case Thimble
}

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length >= 1) {
      val debug = false
      val filename = args.head
      val programArgs = args.tail
      val (code, format) =
        try {
          FileReader.readCodeFromFile(filename)
        } catch {
          case fileReadException: FileReadException =>
            println(fileReadException.getMessage)
            return
        }
      run(code, format, programArgs, debug).foreach(println)
    }
  }

  def run(
      code: String,
      format: FileFormat,
      args: Array[String],
      debug: Boolean = false
  ): Option[HBLAny] = {
    processArgs(args) match {
      case Some(argVals) => {
        if (debug) {
          println(s"> Arguments: ${argVals.mkString(", ")}")
        }
        try {
          format match {
            case FileFormat.Raw | FileFormat.ASCII => {
              val parseTree = Parser.parseGolfed(code)
              if (debug) {
                println("> Parsing from golfed format:")
                println(parseTree)
              }
              Interpreter.loadGolfedProgram(parseTree)
            }
            case FileFormat.Thimble => {
              val parseTree = Parser.parseExpanded(code)
              if (debug) {
                println("> Parsing from ungolfed/Thimble format:")
                println(parseTree)
              }
              Interpreter.loadExpandedProgram(parseTree)
            }
          }
          if (debug) {
            println(s"> Found ${Interpreter.programLines.length} definitions:")
            Interpreter.programLines.foreach(println)
            println("> Executing...")
            println("-".repeat(75))
          }
          return Interpreter.runProgram(argVals)
        } catch {
          case unbalancedException: UnbalancedParensException =>
            println(s"Parsing error: ${unbalancedException.getMessage}")
            println(
              "Unbalanced parentheses are not allowed in Thimble expressions"
            )
          case parenException: UnknownParenException =>
            println(
              s"Unrecognized parenthesis combination: ${parenException.getMessage}"
            )
          case tokenException: TokenException =>
            println(s"Unrecognized symbol: ${tokenException.getMessage}")
          case missingOverloadException: MissingOverloadException =>
            println(
              s"Missing overload for ${missingOverloadException.getMessage}"
            )
          case notCallableException: NotCallableException =>
            println(
              s"Non-callable value ${notCallableException.getMessage} cannot be the head of an expression"
            )
          case argumentException: ArgumentException =>
            println(argumentException.getMessage)
          case topLevelException: TopLevelException =>
            println(topLevelException.getMessage)
          case lineReferenceException: LineReferenceException =>
            println(lineReferenceException.getMessage)
          case arithmeticException: ArithmeticException =>
            println(s"Arithmetic error: ${arithmeticException.getMessage}")
          case stackOverflowError: StackOverflowError =>
            println(
              "Stack overflow (possibly your program isn't using tail recursion?)"
            )
        }
        None
      }
      case None => None
    }
  }

  def processArgs(commandLineArgs: Array[String]): Option[Seq[HBLAny]] = {
    try {
      return Some(
        commandLineArgs
          .map(commandLineArg => {
            val InternalNode(parsedArg, _, _) =
              Parser.parseExpanded(commandLineArg)
            parsedArg match {
              case List(parsedExpr) => Translator.translateExpanded(parsedExpr)
              case _ => throw ArgumentException(s"$commandLineArg")
            }
          })
          .toIndexedSeq
      )
    } catch {
      case argumentException: ArgumentException =>
        println(
          s"Incorrectly formatted expression in program argument: ${argumentException.getMessage}"
        )
      case unbalancedException: UnbalancedParensException =>
        println(
          s"Error while parsing program arguments: ${unbalancedException.getMessage}"
        )
      case parenException: UnknownParenException =>
        println(
          s"Unrecognized parenthesis combination in program argument: ${parenException.getMessage}"
        )
      case tokenException: TokenException =>
        println(
          s"Unrecognized symbol in program argument: ${tokenException.getMessage}"
        )
    }
    None
  }
}
