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

class FileReadException(message: String) extends Exception(message)

object FileReader {
  def readCodeFromFile(filename: String): (String, FileFormat) = {
    val extensionPattern = ".*\\.(\\w+)".r
    val format = filename match {
      case extensionPattern("hb") => FileFormat.Raw
      case extensionPattern("hbl") => FileFormat.ASCII
      case extensionPattern("thbl") => FileFormat.Thimble
      case _ => FileFormat.ASCII  // TODO: How to handle unrecognized file format?
    }
    try {
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
    } catch {
      case fileNotFoundException: (java.io.FileNotFoundException | java.nio.file.NoSuchFileException) =>
        throw FileReadException(s"File $filename not found.")
    }
  }
}
