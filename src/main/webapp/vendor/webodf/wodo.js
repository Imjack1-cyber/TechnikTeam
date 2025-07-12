/**
 * Copyright (C) 2013-2014 KO GmbH <copyright@kogmbh.com>
 *
 * Modified for TechnikTeam project to be self-locating.
 * This version executes in the global scope to prevent reference errors.
 *
 * @licstart
 * This file is part of WebODF.
 *
 * WebODF is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License (GNU AGPL)
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * WebODF is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with WebODF.  If not, see <http://www.gnu.org/licenses/>.
 * @licend
 *
 * @source: http://www.webodf.org/
 * @source: https://github.com/kogmbh/WebODF/
 */

// This script MUST execute in the global scope, not an IIFE,
// so that the eval'd 'runtime.js' creates a global 'runtime' object.

var scripts = document.getElementsByTagName("script");
var wodoScriptSrc = scripts[scripts.length - 1].src;
var wodoScriptPath = wodoScriptSrc.substring(0, wodoScriptSrc.lastIndexOf('/'));

var xhr = new XMLHttpRequest();
// Use the dynamically found path to build the correct library path
var path = wodoScriptPath + "/lib";
var runtimeFilePath = path + "/runtime.js";
var code;

// Synchronous XHR is part of the original library's design for simplicity.
xhr.open("GET", runtimeFilePath, false);
xhr.send(null);

if (xhr.status !== 200 && xhr.status !== 0) { // status 0 is for file:// protocol access
	console.error("Failed to load WebODF runtime from " + runtimeFilePath);
} else {
	code = xhr.responseText;
	code += "\n//# sourceURL=" + runtimeFilePath;

	// By not using an IIFE, 'var runtime' inside the eval'd code will become a global variable.
	eval(code);

	// Now 'runtime' should be a global object available for configuration.
	if (typeof runtime !== 'undefined') {
		runtime.currentDirectory = function() {
			return path;
		};
		runtime.libraryPaths = function() {
			return [path];
		};

		// Load a class to trigger loading the complete library
		runtime.loadClass('odf.OdfContainer');

		// Flag for telling the editor component that this is run from source
		window.WodoFromSource = true;
	} else {
		console.error("WebODF runtime object was not created after evaluating runtime.js. Check the file content.");
	}
}