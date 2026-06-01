/*
 * gen-samples.js — emit sample levels for the interop check (task 5.2).
 *
 * Run with:  node lvl-editor/test/gen-samples.js
 *
 * Writes, into lvl-editor/samples/, for each sample:
 *   <id>.lvl   — the raw .lvl document (the game's raw path)
 *   <id>.b64   — the unwrapped base64 share-code (the game's base64 path)
 *
 * Both forms are then fed through the game's real Java loader by
 * test/InteropCheck.java to prove the editor's output is accepted.
 */
'use strict';

var fs = require('fs');
var path = require('path');
var F = require('../format.js');
var T = require('../templates.js');

var outDir = path.join(__dirname, '..', 'samples');
fs.mkdirSync(outDir, { recursive: true });

function emit(id, grid, meta) {
  var lvl = F.serialize(grid, meta);
  var b64 = F.toBase64(lvl);
  fs.writeFileSync(path.join(outDir, id + '.lvl'), lvl, 'utf8');
  fs.writeFileSync(path.join(outDir, id + '.b64'), b64, 'utf8');
  console.log('wrote ' + id + '.lvl (' + lvl.length + ' chars) and ' + id + '.b64');
}

// One named level from a template, one grid-only (no metadata) level.
emit('named', T.GALLERY.find(function (t) { return t.id === 'coins'; }).build(),
  { name: 'Coin Trail', author: 'Editor' });

emit('grid-only', T.GALLERY.find(function (t) { return t.id === 'rooms'; }).build(),
  {});

console.log('done');
