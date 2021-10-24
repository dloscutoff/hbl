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
import scala.collection.mutable.Stack

class UnbalancedParensException(message: String) extends Exception(message)

case class Token(string: String)

abstract class ParseNode
case class InternalNode(children: List[ParseNode],
                        openParen: String = "(",
                        closeParen: String = ")") extends ParseNode {
  override def toString: String = children.mkString(openParen + " ", " ", " " + closeParen)
}
case class LeafNode(atom: Token) extends ParseNode {
  override def toString: String = s"[${atom.string}]"
}

object Parser {
  val codepage = "012<+*-/%?.,()'\n"

  def parseGolfed(code: String): InternalNode =
    InternalNode(code.split('\n').toList.map(parseGolfedLine))

  def parseGolfedLine(code: String): InternalNode = {
    val tokens = scanGolfed(code)
    var parseTree = parse(tokens, inferParens = true)
    // If we hit an unmatched close-paren, parse the rest and insert
    // the first bit we parsed as its leftmost subtree
    while (!tokens.isEmpty) {
      val leftSubtree = parseTree
      parseTree = parse(tokens, inferParens = true)
      parseTree = InternalNode(leftSubtree +: parseTree.children)
    }
    InternalNode(parseTree.children, openParen = "'(")
  }

  def parseExpanded(code:String): InternalNode = {
    val tokens = scanExpanded(code)
    parse(tokens, inferParens = false)
  }

  def parse(tokens: Stack[Token],
            inferParens: Boolean,
            openParen: String = "(",
            depth: Int = 0): InternalNode = {
    val openParenPattern = "('*\\()".r
    val closeParenPattern = "('*\\))".r
    val children = new ArrayBuffer[ParseNode]
    var closeParen = ")"
    var done = false
    while (!done && !tokens.isEmpty) {
      tokens.pop match {
        case Token(openParenPattern(paren)) =>
          val subtree = parse(tokens, inferParens, paren, depth + 1)
          children.addOne(subtree)
        case Token(closeParenPattern(paren)) =>
          closeParen = paren
          done = true
        case token =>
          children.addOne(LeafNode(token))
      }
    }
    if (!inferParens) {
      if (!done && depth > 0) {
        throw UnbalancedParensException("Missing closing parenthesis")
      } else if (done && depth == 0) {
        throw UnbalancedParensException("Missing open parenthesis")
      }
    }
    return InternalNode(children.toList, openParen, closeParen)
  }

  // In golfed form, each character is a token, except that leading
  // quotes attach to the following character
  def scanGolfed(code: String): Stack[Token] = {
    val tokenPattern = "'*.".r
    Stack.from(tokenPattern.findAllIn(code).map(Token(_)))
  }

  // In expanded form, a token is a parenthesis, possibly with leading
  // quote(s), or any run of characters not containing parens, quotes,
  // or whitespace
  def scanExpanded(code: String): Stack[Token] = {
    val tokenPattern = "'*[()]|[^'()\\s]+".r
    Stack.from(tokenPattern.findAllIn(code).map(Token(_)))
  }

  def fromBytes(codeBytes: Array[Byte]): String = {
    codeBytes.flatMap(byte => {
      val asInt = (byte.toInt + 256) % 256
      List(codepage(asInt / 16), codepage(asInt % 16))
    }).mkString
  }
}
