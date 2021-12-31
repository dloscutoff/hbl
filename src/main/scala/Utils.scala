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

object Utils {
  def bigIntToInt(x: BigInt): Int = {
    if (x > Int.MaxValue)
      Int.MaxValue
    else if (x < Int.MinValue)
      Int.MinValue
    else
      x.toInt
  }

  // Scala's % and BigInt.mod are both wrong. Fight me.
  def mod(x: Int, y: Int): Int = ((x % y) + y) % y
  def mod(x: BigInt, y: BigInt): BigInt = ((x % y) + y) % y
}
