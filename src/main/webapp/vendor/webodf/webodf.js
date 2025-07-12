/**
 * Copyright (C) 2013-2014 KO GmbH <copyright@kogmbh.com>
 *
 * Modified for TechnikTeam project to be self-locating.
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

/*global XMLHttpRequest, runtime, WodoFromSource: true*/

(function() {
	"use strict";

	// Find the path to this script to locate the 'lib' directory.
	var scripts = document.getElementsByTagName("script");
	var webodfScriptSrc = scripts[scripts.length - 1].src;
	var webodfScriptPath = webodfScriptSrc.substring(0, webodfScriptSrc.lastIndexOf('/'));

	var xhr = new XMLHttpRequest(),
		path = webodfScriptPath + "/lib", // Use the dynamically found path
		runtimeFilePath = path + "/runtime.js",
		code;

	xhr.open("GET", runtimeFilePath, false); // Synchronous XHR, old but part of the library
	xhr.send(null);

	if (xhr.status !== 200 && xhr.status !== 0) { // status 0 for file:// protocol
		console.error("Failed to load WebODF runtime from " + runtimeFilePath);
		return;
	}

	code = xhr.responseText;
	code += "\n//# sourceURL=" + runtimeFilePath;
	/*jslint evil: true*/
	eval(code);
	/*jslint evil: false*/

	// adapt for out-of-sources run
	runtime.currentDirectory = function() {
		return path;
	};
	runtime.libraryPaths = function() {
		return [path];
	};
	// load a class to trigger loading the complete lib
	runtime.loadClass('odf.OdfContainer');

	// flag for telling the editor component that this is run from source
	window.WodoFromSource = true;
}());