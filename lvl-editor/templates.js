/*
 * templates.js — the starter gallery.
 *
 * Each template returns a fresh grid (so the editor can mutate it freely).
 * Built on top of LvlFormat helpers rather than hand-typed 24×20 strings, so a
 * miscounted row is impossible.
 */
(function (root, factory) {
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = factory(require('./format.js'));   // Node (tests)
  } else {
    root.LvlTemplates = factory(root.LvlFormat);         // browser
  }
})(typeof self !== 'undefined' ? self : this, function (F) {
  'use strict';

  // Blank bordered shell — the default starting point.
  function blankShell() {
    return F.borderedShell();
  }

  // A wide-open room: start top-left, goal bottom-right.
  function openRoom() {
    var g = F.borderedShell();
    g[2][2] = F.START;
    g[F.ROWS - 3][F.COLS - 3] = F.GOAL;
    return g;
  }

  // A straight corridor of coins from start to goal.
  function coinTrail() {
    var g = F.borderedShell();
    var row = Math.floor(F.ROWS / 2);
    g[row][2] = F.START;
    for (var c = 4; c <= F.COLS - 5; c++) { g[row][c] = F.COIN; }
    g[row][F.COLS - 3] = F.GOAL;
    return g;
  }

  // Four rooms split by a cross wall with gaps; a coin in each room.
  function fourRooms() {
    var g = F.borderedShell();
    var midR = Math.floor(F.ROWS / 2);
    var midC = Math.floor(F.COLS / 2);
    for (var c = 1; c < F.COLS - 1; c++) { g[midR][c] = F.WALL; }
    for (var r = 1; r < F.ROWS - 1; r++) { g[r][midC] = F.WALL; }
    // Knock a doorway through each wall arm so the rooms connect.
    g[midR][Math.floor(midC / 2)] = F.FLOOR;
    g[midR][midC + Math.floor((F.COLS - midC) / 2)] = F.FLOOR;
    g[Math.floor(midR / 2)][midC] = F.FLOOR;
    g[midR + Math.floor((F.ROWS - midR) / 2)][midC] = F.FLOOR;
    // Start in the top-left room, goal in the bottom-right room, a coin per room.
    g[2][2] = F.START;
    g[F.ROWS - 3][F.COLS - 3] = F.GOAL;
    g[2][F.COLS - 3] = F.COIN;
    g[F.ROWS - 3][2] = F.COIN;
    return g;
  }

  // Ordered list shown in the gallery UI.
  var GALLERY = [
    { id: 'blank', label: 'Blank shell', build: blankShell },
    { id: 'open', label: 'Open room', build: openRoom },
    { id: 'coins', label: 'Coin trail', build: coinTrail },
    { id: 'rooms', label: 'Four rooms', build: fourRooms }
  ];

  return { GALLERY: GALLERY };
});
