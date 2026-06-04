/*
 * editor.js — the level editor UI: canvas rendering, painting, palette,
 * metadata, live validity, the starter gallery, and export/import wiring.
 * All format logic lives in format.js (LvlFormat); this file is just the UI.
 */
(function () {
  'use strict';

  var F = window.LvlFormat;
  var COLS = F.COLS, ROWS = F.ROWS;

  // Tool name → tile character.
  var TOOL_CHAR = {
    wall: F.WALL, floor: F.FLOOR, start: F.START, goal: F.GOAL, coin: F.COIN
  };

  // ── State ───────────────────────────────────────────────────────────────────
  var grid = F.borderedShell();   // task 1.3: new level is a bordered shell
  var selectedTool = 'wall';
  var painting = false;
  var lastCell = null;            // avoid repainting the same cell during a drag

  // ── DOM ──────────────────────────────────────────────────────────────────────
  var canvas = document.getElementById('grid');
  var ctx = canvas.getContext('2d');
  var paletteEl = document.getElementById('palette');
  var validityEl = document.getElementById('validity');
  var nameInput = document.getElementById('name');
  var authorInput = document.getElementById('author');
  var galleryButtonsEl = document.getElementById('gallery-buttons');

  var lvlTextEl = document.getElementById('lvl-text');
  var b64TextEl = document.getElementById('b64-text');
  var copyLvlBtn = document.getElementById('copy-lvl');
  var downloadLvlBtn = document.getElementById('download-lvl');
  var copyB64Btn = document.getElementById('copy-b64');

  var importTextEl = document.getElementById('import-text');
  var importBtn = document.getElementById('import-btn');
  var importFileEl = document.getElementById('import-file');
  var importMsgEl = document.getElementById('import-msg');
  var importOpenBtn = document.getElementById('import-open');
  var importModalEl = document.getElementById('import-modal');
  var importCloseBtn = document.getElementById('import-close');

  // ── Tile sprites (the game's own assets) ─────────────────────────────────────
  var SPRITE_SRC = {
    floor: 'assets/floor.png',
    wall: 'assets/wall.png',
    coin: 'assets/coin.png',
    goal: 'assets/goal.png',
    robot: 'assets/robot.png'
  };
  var sprites = {};
  var spritesReady = false;

  function preloadSprites(done) {
    var keys = Object.keys(SPRITE_SRC);
    var remaining = keys.length;
    keys.forEach(function (k) {
      var im = new Image();
      im.onload = im.onerror = function () { if (--remaining === 0) { done(); } };
      im.src = SPRITE_SRC[k];
      sprites[k] = im;
    });
  }

  // ── Canvas sizing (DPR-aware for crisp scaling) ──────────────────────────────
  var CELL = 26;                  // logical pixels per cell
  function sizeCanvas() {
    var dpr = window.devicePixelRatio || 1;
    canvas.width = COLS * CELL * dpr;   // backing buffer; CSS controls display size
    canvas.height = ROWS * CELL * dpr;  // intrinsic 24:20 ratio → square tiles via width:auto
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.imageSmoothingEnabled = true;
  }

  // ── Rendering ─────────────────────────────────────────────────────────────────
  function render() {
    for (var r = 0; r < ROWS; r++) {
      for (var c = 0; c < COLS; c++) {
        drawCell(c, r, grid[r][c]);
      }
    }
  }

  function drawCell(c, r, ch) {
    var x = c * CELL, y = r * CELL;

    if (ch === F.WALL) {
      // Wall crate fills the whole tile — no gridline on top (matches the game).
      drawSprite('wall', x, y, 1);
    } else {
      drawSprite('floor', x, y, 1);
      // Faint floor gridline (floor only) so cell boundaries stay visible while authoring.
      ctx.strokeStyle = cssVar('--grid-line');
      ctx.lineWidth = 1;
      ctx.strokeRect(x + 0.5, y + 0.5, CELL - 1, CELL - 1);
      if (ch === F.COIN) { drawSprite('coin', x, y, 0.72); }
      else if (ch === F.START) { drawSprite('robot', x, y, 0.82); }
      else if (ch === F.GOAL) { drawSprite('goal', x, y, 1); }
    }
  }

  // Draw a sprite centered in the cell at the given scale (1 = fills the cell).
  // Falls back to a sampled solid color if the image hasn't loaded.
  function drawSprite(key, x, y, scale) {
    var im = sprites[key];
    var size = CELL * scale;
    var ox = x + (CELL - size) / 2;
    var oy = y + (CELL - size) / 2;
    if (spritesReady && im && im.complete && im.naturalWidth > 0) {
      ctx.drawImage(im, ox, oy, size, size);
    } else {
      ctx.fillStyle = cssVar('--sw-' + (key === 'robot' ? 'start' : key));
      ctx.fillRect(ox, oy, size, size);
    }
  }

  var _cssCache = {};
  function cssVar(name) {
    if (_cssCache[name] == null) {
      _cssCache[name] = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }
    return _cssCache[name];
  }

  // ── Painting ──────────────────────────────────────────────────────────────────
  function cellFromEvent(evt) {
    var rect = canvas.getBoundingClientRect();
    var c = Math.floor((evt.clientX - rect.left) / rect.width * COLS);
    var r = Math.floor((evt.clientY - rect.top) / rect.height * ROWS);
    if (c < 0 || c >= COLS || r < 0 || r >= ROWS) { return null; }
    return { c: c, r: r };
  }

  function clearTile(target) {
    for (var r = 0; r < ROWS; r++) {
      for (var c = 0; c < COLS; c++) {
        if (grid[r][c] === target) { grid[r][c] = F.FLOOR; }
      }
    }
  }

  function paintCell(c, r) {
    var ch = TOOL_CHAR[selectedTool];
    if (ch === F.START) {
      clearTile(F.START);          // single start: relocate (old cell → floor)
      grid[r][c] = F.START;
    } else if (ch === F.GOAL) {
      clearTile(F.GOAL);           // single goal: relocate (old cell → floor)
      grid[r][c] = F.GOAL;
    } else {
      grid[r][c] = ch;
    }
  }

  function handlePaint(evt) {
    var cell = cellFromEvent(evt);
    if (!cell) { return; }
    if (lastCell && lastCell.c === cell.c && lastCell.r === cell.r) { return; }
    lastCell = cell;
    paintCell(cell.c, cell.r);
    render();
    refresh();
  }

  canvas.addEventListener('pointerdown', function (evt) {
    evt.preventDefault();
    painting = true;
    lastCell = null;
    canvas.setPointerCapture(evt.pointerId);
    handlePaint(evt);
  });
  canvas.addEventListener('pointermove', function (evt) {
    if (painting) { handlePaint(evt); }
  });
  function stopPainting() { painting = false; lastCell = null; }
  canvas.addEventListener('pointerup', stopPainting);
  canvas.addEventListener('pointercancel', stopPainting);

  // ── Palette ───────────────────────────────────────────────────────────────────
  function selectTool(tool) {
    selectedTool = tool;
    var buttons = paletteEl.querySelectorAll('.tool');
    buttons.forEach(function (b) {
      b.classList.toggle('selected', b.getAttribute('data-tool') === tool);
    });
  }
  paletteEl.addEventListener('click', function (evt) {
    var btn = evt.target.closest('.tool');
    if (btn) { selectTool(btn.getAttribute('data-tool')); }
  });

  // ── Validity + export gating ───────────────────────────────────────────────────
  function refresh() {
    var v = F.validity(grid);
    if (v.valid) {
      validityEl.textContent = '✓ Valid — exactly one start and one goal. Ready to export.';
      validityEl.className = 'validity ok';
    } else {
      validityEl.textContent = '✗ ' + v.problems.join(' ');
      validityEl.className = 'validity bad';
    }
    updateExports(v.valid);
  }

  function currentMeta() {
    return { name: nameInput.value, author: authorInput.value };
  }

  function updateExports(valid) {
    if (valid) {
      var lvl = F.serialize(grid, currentMeta());
      lvlTextEl.value = lvl;
      b64TextEl.value = F.toBase64(lvl);
    } else {
      lvlTextEl.value = '';
      b64TextEl.value = '';
    }
    copyLvlBtn.disabled = !valid;
    downloadLvlBtn.disabled = !valid;
    copyB64Btn.disabled = !valid;
  }

  nameInput.addEventListener('input', refresh);
  authorInput.addEventListener('input', refresh);

  // ── Gallery ─────────────────────────────────────────────────────────────────────
  function loadTemplate(buildFn) {
    grid = buildFn();
    render();
    refresh();
    showImportMsg('', '');
  }
  window.LvlTemplates.GALLERY.forEach(function (tpl) {
    var btn = document.createElement('button');
    btn.className = 'btn';
    btn.type = 'button';
    btn.textContent = tpl.label;
    btn.addEventListener('click', function () { loadTemplate(tpl.build); });
    galleryButtonsEl.appendChild(btn);
  });

  // ── Export actions ───────────────────────────────────────────────────────────────
  function flash(btn) {
    btn.classList.add('flash');
    setTimeout(function () { btn.classList.remove('flash'); }, 700);
  }

  function copyFrom(textareaEl, btn) {
    var text = textareaEl.value;
    if (!text) { return; }
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(function () {
        flash(btn);
      }, function () {
        selectFallback(textareaEl);
      });
    } else {
      selectFallback(textareaEl);
    }
  }
  // Clipboard unavailable (permissions / non-HTTPS): select the visible textarea
  // so the author can copy manually — copying never silently fails.
  function selectFallback(textareaEl) {
    textareaEl.focus();
    textareaEl.select();
  }

  copyLvlBtn.addEventListener('click', function () { copyFrom(lvlTextEl, copyLvlBtn); });
  copyB64Btn.addEventListener('click', function () { copyFrom(b64TextEl, copyB64Btn); });

  downloadLvlBtn.addEventListener('click', function () {
    var text = lvlTextEl.value;
    if (!text) { return; }
    var name = nameInput.value.trim();
    var safe = name ? name.replace(/[^a-zA-Z0-9._-]+/g, '_') : 'level';
    var blob = new Blob([text], { type: 'text/plain' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = safe + '.lvl';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    flash(downloadLvlBtn);
  });

  // ── Import (modal) ──────────────────────────────────────────────────────────────────
  function showImportMsg(text, kind) {
    importMsgEl.textContent = text;
    importMsgEl.className = 'import-msg' + (kind ? ' ' + kind : '');
  }

  function openImportModal() {
    showImportMsg('', '');
    importModalEl.hidden = false;
    importTextEl.focus();
  }
  function closeImportModal() {
    importModalEl.hidden = true;
  }

  importOpenBtn.addEventListener('click', openImportModal);
  importCloseBtn.addEventListener('click', closeImportModal);
  // Click the dim backdrop (outside the dialog) to dismiss.
  importModalEl.addEventListener('click', function (evt) {
    if (evt.target === importModalEl) { closeImportModal(); }
  });
  document.addEventListener('keydown', function (evt) {
    if (evt.key === 'Escape' && !importModalEl.hidden) { closeImportModal(); }
  });

  function applyImport(input) {
    var result;
    try {
      result = F.load(input);          // throws on malformed input → canvas untouched
    } catch (e) {
      showImportMsg('✗ ' + e.message, 'bad');   // modal stays open so the author can fix it
      return;
    }
    grid = result.grid;
    nameInput.value = result.name || '';
    authorInput.value = result.author || '';
    render();
    refresh();
    importTextEl.value = '';           // leave the box clean for next time
    closeImportModal();                // success: the loaded level + validity banner are the confirmation
  }

  importBtn.addEventListener('click', function () {
    applyImport(importTextEl.value);
  });

  importFileEl.addEventListener('change', function () {
    var file = importFileEl.files && importFileEl.files[0];
    if (!file) { return; }
    var reader = new FileReader();
    reader.onload = function () { applyImport(String(reader.result)); };
    reader.onerror = function () { showImportMsg('✗ Could not read the file.', 'bad'); };
    reader.readAsText(file);
    importFileEl.value = '';          // allow re-uploading the same file
  });

  // ── Boot ───────────────────────────────────────────────────────────────────────────
  sizeCanvas();
  selectTool('wall');
  render();        // immediate paint with fallback colors
  refresh();
  preloadSprites(function () {   // then swap in the real game sprites
    spritesReady = true;
    render();
  });
})();
