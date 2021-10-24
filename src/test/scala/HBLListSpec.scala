/**
 * Half-Byte Lisp interpreter test suite
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

import hbl.{HBLAny, HBLFinalMacro, HBLFunction, HBLList}
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.ArrayBuffer

class HBLListSpec extends AnyFlatSpec {
  "An HBLList" should "be initialized from a Vector or from varargs" in {
    val lsFromVector = HBLList(Vector(BigInt(-1), BigInt(0), BigInt(1)))
    val lsFromArgs = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assert(lsFromVector === lsFromArgs)
  }

  it should "be indexable using an Int" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assert(ls(0) === BigInt(-1))
    assert(ls(1) === BigInt(0))
    assert(ls(2) === BigInt(1))
  }

  it should "throw IndexOutOfBoundsException when the index is out of bounds" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assertThrows[IndexOutOfBoundsException] {ls(3)}
    assertThrows[IndexOutOfBoundsException] {ls(-1)}
  }

  it should "be a sequence type" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assert(ls === Seq(BigInt(-1), BigInt(0), BigInt(1)))
    for ((rangeItem, lsItem) <- (-1 to 1).zip(ls)) {
      assert(BigInt(rangeItem) === lsItem)
    }
  }

  it should "contain values of type HBLAny" in {
    val lsBigInt = HBLList(BigInt("12345678901234567890"))
    val lsHBLList = HBLList(HBLList(), HBLList())
    val lsHBLFunction = HBLList(HBLFunction("f", args => BigInt(0)))
    val lsHBLMacro = HBLList(HBLFinalMacro("m", (args, context) => BigInt(0)))
    assert(lsBigInt(0) match {
      case i: HBLAny => true
    })
    assert(lsHBLList(0) match {
      case l: HBLAny => true
    })
    assert(lsHBLFunction(0) match {
      case f: HBLAny => true
    })
    assert(lsHBLMacro(0) match {
      case m: HBLAny => true
    })
  }

  it should "support basic list operations" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1), BigInt(2))
    assert(ls.head === BigInt(-1))
    assert(ls.last === BigInt(2))
    assert(ls.tail === HBLList(BigInt(0), BigInt(1), BigInt(2)))
    assert(ls.init === HBLList(BigInt(-1), BigInt(0), BigInt(1)))
    assert(ls.take(2) === HBLList(BigInt(-1), BigInt(0)))
    assert(ls.drop(2) === HBLList(BigInt(1), BigInt(2)))
    assert(ls.take(10) === ls)
    assert(ls.drop(10) === HBLList())
    assert(ls.length === 4)
    assert(ls.reverse === HBLList(BigInt(2), BigInt(1), BigInt(0), BigInt(-1)))
    assert(ls.prepended(HBLList(BigInt(8))) === HBLList(HBLList(BigInt(8)), BigInt(-1), BigInt(0), BigInt(1), BigInt(2)))
    assert(ls.prependedAll(HBLList(BigInt(8))) === HBLList(BigInt(8), BigInt(-1), BigInt(0), BigInt(1), BigInt(2)))
    assert(ls.appended(HBLList(BigInt(8))) === HBLList(BigInt(-1), BigInt(0), BigInt(1), BigInt(2), HBLList(BigInt(8))))
    assert(ls.appendedAll(HBLList(BigInt(8))) === HBLList(BigInt(-1), BigInt(0), BigInt(1), BigInt(2), BigInt(8)))
  }

  it should "support map and filter" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assert(ls.map(x => x match {case n: BigInt => n + 1; case _ => x}) === HBLList(BigInt(0), BigInt(1), BigInt(2)))
    assert(ls.filter(x => x match {case n: BigInt => n != 0; case _ => false}) === HBLList(BigInt(-1), BigInt(1)))
    assert(ls.map(x => x.toString) === Seq("-1", "0", "1"))
  }

  it should "have a string format wrapped in parentheses, with items separated by spaces" in {
    val ls0 = HBLList()
    val ls1 = HBLList(BigInt(1))
    val ls2 = HBLList(BigInt(1), BigInt(2))
    val ls3 = HBLList(BigInt(-1), BigInt(0), HBLList(BigInt(1), BigInt(2)), HBLList(HBLList()))
    assert(ls0.toString === "()")
    assert(ls1.toString === "(1)")
    assert(ls2.toString === "(1 2)")
    assert(ls3.toString === "(-1 0 (1 2) (()))")
  }

  it should "have an extractor object to match its contents" in {
    val ls = HBLList(BigInt(-1), BigInt(0), BigInt(1))
    assert(ls match {
      case HBLList(x: BigInt, y: BigInt, z: BigInt) => true
      case _ => false
    })
  }

  "An empty HBLList" should "throw NoSuchElementException or IndexOutOfBoundsException when an element is referenced" in {
    val lsEmpty = HBLList()
    assertThrows[NoSuchElementException] {lsEmpty.head}
    assertThrows[NoSuchElementException] {lsEmpty.last}
    assertThrows[IndexOutOfBoundsException] {lsEmpty(0)}
  }

  it should "throw UnsupportedOperationException when a sublist is referenced" in {
    val lsEmpty = HBLList()
    assertThrows[UnsupportedOperationException] {lsEmpty.tail}
    assertThrows[UnsupportedOperationException] {lsEmpty.init}
  }
}
