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

function runCode() {
    var codeArea = document.getElementById("code");
    var codeFormatSelect = document.getElementById("code-format");
    var argsArea = document.getElementById("args");
    var resultArea = document.getElementById("result");
    var code = codeArea.value;
    var codeFormat = codeFormatSelect.value.toLowerCase();
    var args = argsArea.value ? argsArea.value.split("\n") : [];
    var debug = false;
    var result = runHBL(code, codeFormat, args, debug);
    resultArea.value = result;
}

window.onload = function() {
    document.onkeyup = function(e) {
        if (e.key === "Enter" && e.ctrlKey) {
            runCode();
        }
    };
}
