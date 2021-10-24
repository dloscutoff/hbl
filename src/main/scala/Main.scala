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

import scala.io.Source
import java.nio.file.{Files, Paths}

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
      try {
        val argVals = processArgs(args.tail)
        if (debug) {
          println(s"> Arguments: ${argVals.mkString(", ")}")
        }
        val (code, format) = readCodeFromFile(filename)
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
        Interpreter.runProgram(argVals)
      } catch {
        case fileNotFoundException: (java.io.FileNotFoundException | java.nio.file.NoSuchFileException) =>
          println(s"File $filename not found.")
        case unbalancedException: UnbalancedParensException =>
          println(s"Parsing error: ${unbalancedException.getMessage}")
          println("Unbalanced parentheses are not allowed in Thimble expressions")
        case parenException: UnknownParenException =>
          println(s"Unrecognized parenthesis combination: ${parenException.getMessage}")
        case tokenException: TokenException =>
          println(s"Unrecognized symbol: ${tokenException.getMessage}")
        case missingOverloadException: MissingOverloadException =>
          println(s"Missing overload for ${missingOverloadException.getMessage}")
        case notCallableException: NotCallableException =>
          println(s"Non-callable value ${notCallableException.getMessage} cannot be the head of an expression")
        case argumentException: ArgumentException =>
          println(argumentException.getMessage)
        case topLevelException: TopLevelException =>
          println(topLevelException.getMessage)
        case lineReferenceException: LineReferenceException =>
          println(lineReferenceException.getMessage)
        case arithmeticException: ArithmeticException =>
          println(s"Arithmetic error: ${arithmeticException.getMessage}")
        case stackOverflowError: StackOverflowError =>
          println("Stack overflow (possibly your program isn't using tail recursion?)")
      }
    }
  }

  def processArgs(commandLineArgs: Array[String]): Seq[HBLAny] = {
    for (commandLineArg <- commandLineArgs.toList) yield {
      val InternalNode(parsedArg, _, _) = Parser.parseExpanded(commandLineArg)
      parsedArg match {
        case List(parsedExpr) => Interpreter.translateExpanded(parsedExpr)
        case _ => throw ArgumentException(s"Incorrectly formatted expression in command-line argument: $commandLineArg")
      }
    }
  }

  def readCodeFromFile(filename: String): (String, FileFormat) = {
    val extensionPattern = ".*\\.(\\w+)".r
    val format = filename match {
      case extensionPattern("hb") => FileFormat.Raw
      case extensionPattern("hbl") => FileFormat.ASCII
      case extensionPattern("thbl") => FileFormat.Thimble
      case _ => FileFormat.ASCII  // TODO: How to handle unrecognized file format?
    }
    val code = if (format == FileFormat.Raw) {
      val inFile = Paths.get(filename)
      val byteArray = Files.readAllBytes(inFile)
      Parser.fromBytes(byteArray)
    } else {
      val inFile = Source.fromFile(filename)
      try {
        inFile.mkString
      } finally inFile.close()
    }
    (code, format)
  }
}
