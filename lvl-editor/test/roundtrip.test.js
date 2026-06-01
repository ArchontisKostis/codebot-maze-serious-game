/*
 * roundtrip.test.js — verifies the .lvl format logic with no browser.
 *
 * Run with:  node lvl-editor/test/roundtrip.test.js
 *
 * Covers task 4.4 (export → import round-trip reproduces canvas + metadata) and
 * the malformed-input cases from the import spec. format.js is pure logic, so it
 * loads directly under Node (btoa/atob and TextEncoder/TextDecoder are globals).
 */
'use strict';

var F = require('../format.js');

var passed = 0;
var failed = 0;

function check(name, cond) {
  if (cond) { passed++; console.log('  ok   ' + name); }
  else { failed++; console.log('  FAIL ' + name); }
}

function gridsEqual(a, b) {
  if (a.length !== b.length) { return false; }
  for (var r = 0; r < a.length; r++) {
    if (a[r].length !== b[r].length) { return false; }
    for (var c = 0; c < a[r].length; c++) {
      if (a[r][c] !== b[r][c]) { return false; }
    }
  }
  return true;
}

function throws(fn) {
  try { fn(); return false; } catch (e) { return true; }
}

// ── A representative authored level ──────────────────────────────────────────
function sampleGrid() {
  var g = F.borderedShell();
  g[2][2] = F.START;
  g[2][F.COLS - 3] = F.GOAL;
  g[5][5] = F.COIN;
  g[5][10] = F.COIN;
  g[10][12] = F.WALL;
  g[10][13] = F.WALL;
  return g;
}

console.log('round-trip:');

// 1. .lvl text round-trip with metadata
(function () {
  var grid = sampleGrid();
  var meta = { name: 'Test Level', author: 'Archontis' };
  var lvl = F.serialize(grid, meta);
  var back = F.load(lvl);
  check('raw .lvl preserves grid', gridsEqual(grid, back.grid));
  check('raw .lvl preserves name', back.name === 'Test Level');
  check('raw .lvl preserves author', back.author === 'Archontis');
})();

// 2. base64 round-trip
(function () {
  var grid = sampleGrid();
  var meta = { name: 'Σ Level', author: 'Κώστας' };   // non-ASCII → exercises UTF-8 path
  var lvl = F.serialize(grid, meta);
  var b64 = F.toBase64(lvl);
  check('base64 is single line', b64.indexOf('\n') < 0 && b64.indexOf('\r') < 0);
  var back = F.load(b64);
  check('base64 preserves grid', gridsEqual(grid, back.grid));
  check('base64 preserves unicode name', back.name === 'Σ Level');
  check('base64 preserves unicode author', back.author === 'Κώστας');
})();

// 3. grid-only document (no metadata)
(function () {
  var grid = sampleGrid();
  var lvl = F.serialize(grid, {});
  check('blank meta → no header', lvl.indexOf('---') < 0 && lvl.indexOf('name:') < 0);
  var back = F.load(lvl);
  check('grid-only round-trips', gridsEqual(grid, back.grid));
  check('grid-only has null name', back.name === null || back.name === undefined);
})();

console.log('malformed input (canvas must be left unchanged → load throws):');

check('invalid base64 rejected', throws(function () { F.load('not base64 @@@@'); }));
check('too many rows rejected', throws(function () {
  var rows = [];
  for (var i = 0; i < 25; i++) { rows.push('#'.repeat(24)); }
  F.load(rows.join('\n'));
}));
check('unknown char rejected', throws(function () {
  F.load('---\n' + 'X'.repeat(24) + '\n' + '#'.repeat(24));
}));
check('empty input rejected', throws(function () { F.load('   '); }));
check('multiple starts rejected', throws(function () {
  F.load('SS......................\n' + ('.'.repeat(23)) + 'G');
}));

console.log('');
console.log(passed + ' passed, ' + failed + ' failed');
if (failed > 0) { process.exit(1); }
