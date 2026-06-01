/*
 * format.js — the `.lvl` level-document format.
 *
 * This is the JS mirror of the game's Java loader (LevelDocumentParser +
 * AsciiTileMapParser). It is a HARD CONTRACT: if this drifts from the Java
 * side, share-codes silently break. See ../lvl-format.md for the frozen grammar.
 *
 * Pure logic, no DOM — so the same file runs in the browser (window.LvlFormat)
 * and under Node (module.exports) for the round-trip tests.
 */
(function (root, factory) {
  var api = factory();
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = api;          // Node (tests)
  } else {
    root.LvlFormat = api;          // browser
  }
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  // ── Constants (must match GameAreaConfig / AsciiTileMapParser) ──────────────
  var COLS = 24;
  var ROWS = 20;
  var SEPARATOR = '---';

  var WALL = '#';
  var FLOOR = '.';
  var START = 'S';
  var GOAL = 'G';
  var COIN = 'C';

  // ── Grid helpers ────────────────────────────────────────────────────────────

  /** A fresh ROWS×COLS grid of floor cells. grid[row][col] holds one char. */
  function blankFloorGrid() {
    var g = [];
    for (var r = 0; r < ROWS; r++) {
      var row = [];
      for (var c = 0; c < COLS; c++) { row.push(FLOOR); }
      g.push(row);
    }
    return g;
  }

  /** The default authoring shell: a one-tile wall ring around an all-floor interior. */
  function borderedShell() {
    var g = blankFloorGrid();
    for (var c = 0; c < COLS; c++) { g[0][c] = WALL; g[ROWS - 1][c] = WALL; }
    for (var r = 0; r < ROWS; r++) { g[r][0] = WALL; g[r][COLS - 1] = WALL; }
    return g;
  }

  function cloneGrid(g) {
    return g.map(function (row) { return row.slice(); });
  }

  /** Build a grid from an array of row strings (used by templates). Lenient: pads/trims to COLS×ROWS. */
  function gridFromRows(rows) {
    var g = blankFloorGrid();
    for (var r = 0; r < ROWS; r++) {
      var line = r < rows.length ? rows[r] : '';
      for (var c = 0; c < COLS; c++) {
        var ch = c < line.length ? line.charAt(c) : ' ';
        g[r][c] = (ch === ' ') ? FLOOR : ch;
      }
    }
    return g;
  }

  function countTile(grid, target) {
    var n = 0;
    for (var r = 0; r < ROWS; r++) {
      for (var c = 0; c < COLS; c++) {
        if (grid[r][c] === target) { n++; }
      }
    }
    return n;
  }

  /**
   * Validity for export: exactly one start and exactly one goal. Returns the
   * unmet conditions so the UI can tell the author what to fix.
   */
  function validity(grid) {
    var starts = countTile(grid, START);
    var goals = countTile(grid, GOAL);
    var problems = [];
    if (starts === 0) { problems.push('Place a start tile (S).'); }
    else if (starts > 1) { problems.push('Remove extra start tiles — only one is allowed.'); }
    if (goals === 0) { problems.push('Place a goal tile (G).'); }
    else if (goals > 1) { problems.push('Remove extra goal tiles — only one is allowed.'); }
    return { valid: problems.length === 0, problems: problems, starts: starts, goals: goals };
  }

  // ── Serialization (.lvl text) ───────────────────────────────────────────────

  /**
   * Serialize a grid + metadata to a `.lvl` document. Header lines are emitted
   * only for non-blank name/author; when both are blank the output is grid-only
   * (still valid for the loader). Interior empties are normalized to '.'.
   */
  function serialize(grid, meta) {
    meta = meta || {};
    var headerLines = [];
    var name = meta.name != null ? String(meta.name).trim() : '';
    var author = meta.author != null ? String(meta.author).trim() : '';
    if (name) { headerLines.push('name: ' + name); }
    if (author) { headerLines.push('author: ' + author); }

    var body = [];
    for (var r = 0; r < ROWS; r++) {
      var line = '';
      for (var c = 0; c < COLS; c++) {
        var ch = grid[r][c];
        if (ch == null || ch === ' ') { ch = FLOOR; }
        line += ch;
      }
      body.push(line);
    }

    if (headerLines.length === 0) {
      return body.join('\n');                                  // grid-only is acceptable
    }
    return headerLines.concat([SEPARATOR]).concat(body).join('\n');
  }

  // ── Parsing (.lvl text → grid + metadata) ───────────────────────────────────

  function readHeader(headerLines) {
    var meta = { name: null, author: null };
    for (var i = 0; i < headerLines.length; i++) {
      var line = headerLines[i].trim();
      if (!line) { continue; }
      var colon = line.indexOf(':');
      if (colon < 0) { continue; }                              // tolerate stray lines
      var key = line.substring(0, colon).trim().toLowerCase();
      var value = line.substring(colon + 1).trim();
      if (key === 'name') { meta.name = value; }
      else if (key === 'author') { meta.author = value; }
      // scorer / stars / unknown keys are intentionally ignored (completion-only authoring)
    }
    return meta;
  }

  function parseGrid(rawLines) {
    if (rawLines.length > ROWS) {
      throw new Error('Level has ' + rawLines.length + ' rows but the grid is '
        + COLS + '×' + ROWS + '. Remove the extra rows.');
    }
    var grid = blankFloorGrid();
    var starts = 0;
    var goals = 0;
    for (var r = 0; r < ROWS; r++) {
      var line = r < rawLines.length ? rawLines[r] : '';
      for (var c = 0; c < COLS; c++) {                          // chars past col 24 are ignored (like the game)
        var ch = c < line.length ? line.charAt(c) : ' ';
        if (ch === ' ') { ch = FLOOR; }
        switch (ch) {
          case WALL: case FLOOR: case COIN:
            grid[r][c] = ch; break;
          case START:
            if (++starts > 1) { throw new Error("Multiple 'S' start tiles — a level can have only one."); }
            grid[r][c] = START; break;
          case GOAL:
            if (++goals > 1) { throw new Error("Multiple 'G' goal tiles — a level can have only one."); }
            grid[r][c] = GOAL; break;
          default:
            throw new Error("Unknown tile character '" + ch + "' at column "
              + (c + 1) + ', row ' + (r + 1) + '.');
        }
      }
    }
    return grid;
  }

  /** Parse a raw `.lvl` document. Throws Error with a readable message on malformed input. */
  function parseDocument(document) {
    if (document == null) { throw new Error('Level document is empty.'); }

    var allLines = document.split(/\r?\n/);
    var headerLines = [];
    var gridLines = [];
    var inGrid = false;
    var sawSeparator = false;
    for (var i = 0; i < allLines.length; i++) {
      var line = allLines[i];
      if (!inGrid && line.trim() === SEPARATOR) {
        inGrid = true; sawSeparator = true; continue;
      }
      if (inGrid) { gridLines.push(line); } else { headerLines.push(line); }
    }

    // Header-less document: the whole text is the grid.
    if (!sawSeparator) {
      gridLines = allLines.slice();
      headerLines = [];
    }

    // Drop trailing blank rows (e.g. from a trailing newline) before the 20-row check.
    while (gridLines.length && gridLines[gridLines.length - 1].trim() === '') {
      gridLines.pop();
    }

    var meta = readHeader(headerLines);
    var grid = parseGrid(gridLines);
    return { grid: grid, name: meta.name, author: meta.author };
  }

  // ── base64 (UTF-8, single unwrapped line) ───────────────────────────────────

  function toBase64(text) {
    var bytes = new TextEncoder().encode(text);              // UTF-8, matching Java getBytes(UTF_8)
    var binary = '';
    for (var i = 0; i < bytes.length; i++) { binary += String.fromCharCode(bytes[i]); }
    return btoa(binary);                                     // standard base64, no line breaks
  }

  function fromBase64(b64) {
    var binary = atob(b64);                                  // throws on invalid base64
    var bytes = new Uint8Array(binary.length);
    for (var i = 0; i < binary.length; i++) { bytes[i] = binary.charCodeAt(i); }
    return new TextDecoder().decode(bytes);
  }

  // ── Untrusted load (mirrors the game's raw-vs-base64 detection) ──────────────

  function containsLineBreak(s) {
    return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
  }

  /**
   * Load from untrusted input (pasted text or uploaded file). Input containing a
   * line break is raw `.lvl`; single-line input is base64-decoded first. Throws
   * Error with a readable message on any failure (caller leaves the canvas as-is).
   */
  function load(input) {
    if (input == null || input.trim() === '') {
      throw new Error('No level provided.');
    }
    var trimmed = input.trim();
    var document;
    if (containsLineBreak(trimmed)) {
      document = input;                                       // raw, untrimmed (mirrors the game)
    } else {
      try {
        document = fromBase64(trimmed);
      } catch (e) {
        throw new Error('Could not decode share-code (invalid base64).');
      }
    }
    return parseDocument(document);
  }

  return {
    COLS: COLS, ROWS: ROWS, SEPARATOR: SEPARATOR,
    WALL: WALL, FLOOR: FLOOR, START: START, GOAL: GOAL, COIN: COIN,
    blankFloorGrid: blankFloorGrid,
    borderedShell: borderedShell,
    cloneGrid: cloneGrid,
    gridFromRows: gridFromRows,
    countTile: countTile,
    validity: validity,
    serialize: serialize,
    parseDocument: parseDocument,
    toBase64: toBase64,
    fromBase64: fromBase64,
    load: load
  };
});
