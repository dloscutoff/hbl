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
    var codeArea = document.getElementById("code"),
        codeFormatSelect = document.getElementById("code-format"),
        argsArea = document.getElementById("args"),
        resultArea = document.getElementById("result");
    var code = codeArea.value,
        codeFormat = codeFormatSelect.value.toLowerCase(),
        args = argsArea.value ? argsArea.value.split("\n") : [],
        debug = false;
    var result = runHBL(code, codeFormat, args, debug);
    resultArea.value = result;
}

function urlDecode(value) {
    try {
        return atob(value.replaceAll("_", "="));
    } catch (error) {
        console.log("Error while decoding " + value);
        console.log(error);
        return "";
    }
}

function urlEncode(value) {
    try {
        return btoa(value).replaceAll("=", "_");
    } catch (error) {
        console.log("Error while encoding " + value);
        console.log(error);
        return "";
    }
}

function generateURL() {
    var codeArea = document.getElementById("code"),
        codeFormatSelect = document.getElementById("code-format"),
        argsArea = document.getElementById("args");
    var params = [
        ["f", codeFormatSelect.selectedIndex],
        ["p", urlEncode(codeArea.value)],
        ["a", urlEncode(argsArea.value)],
    ].filter(([key, value]) => value !== "").map(pair => pair.join("="));
    var queryString = "?" + params.join("&");
    return location.origin + location.pathname + queryString;
}

function loadURL() {
    var codeArea = document.getElementById("code"),
        codeFormatSelect = document.getElementById("code-format"),
        argsArea = document.getElementById("args");
    var queryString = location.search.slice(1);
    
    for (const param of queryString.split("&")) {
        var key;
        var value;
        [key, value] = param.split("=");
        if (key === "f") {
            codeFormatSelect.selectedIndex = value;
            if (codeFormatSelect.selectedIndex === -1) {
                // Not a valid selection; select the default format instead
                codeFormatSelect.selectedIndex = 0;
            }
        } else if (key === "p") {
            codeArea.value = urlDecode(value);
        } else if (key === "a") {
            argsArea.value = urlDecode(value);
        }
    }
}

function copyPermalink() {
    var permalink = generateURL();
    // Copy the permalink to the clipboard, and then (whether the copy succeeded or not)
    // go to the permalink URL
    navigator.clipboard.writeText(permalink).then(
        () => location.assign(permalink),
        () => location.assign(permalink)
    );
}

window.onload = function() {
    document.onkeyup = function(e) {
        if (e.key === "Enter" && e.ctrlKey) {
            runCode();
        }
    };
    loadURL();
}
