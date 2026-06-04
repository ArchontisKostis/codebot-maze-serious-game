# Codebot Level Editor

A zero-backend, static web tool for authoring custom Codebot levels. Draw a
24×20 maze, then export it as a `.lvl` file or a base64 share-code that the game
loads via its Free Play / Load Custom screen.

Hosted at **`codebot.archontis.gr/lvl-editor`**.

## Files

| File           | Purpose                                                              |
| -------------- | ------------------------------------------------------------------- |
| `index.html`   | Page shell (logo header, canvas, palette, level form, import modal) |
| `styles.css`   | Navy "RIVETS terminal" theme (mirrors the game's `UiTheme` palette)  |
| `format.js`    | The `.lvl` format mirror — serialize/parse + base64. **No DOM.**     |
| `templates.js` | Starter-gallery templates                                            |
| `editor.js`    | UI: canvas rendering, painting, validity, export/import wiring       |
| `assets/`      | The game's own art: tile sprites (floor, wall, coin, goal, robot), the play-area frame, and the `logo` |
| `test/`        | Node round-trip tests, sample generator, Java interop harness        |
| `samples/`     | Generated sample levels (`.lvl` + `.b64`) for the interop check      |

The output format is a **hard contract** with the game's loader — see
[`../lvl-format.md`](../lvl-format.md).

## Run locally

It's a static site with no build step. Open `index.html` directly, or serve it:

```bash
cd lvl-editor
python -m http.server 8000   # then visit http://localhost:8000
```

(Serving over `http(s)` rather than `file://` lets the clipboard API work; a
visible textarea fallback covers the case where it doesn't.)

## Verify

```bash
node lvl-editor/test/roundtrip.test.js   # format round-trip + malformed-input cases
node lvl-editor/test/gen-samples.js      # regenerate samples/
```

The interop check (samples accepted by the game's real Java loader) is compiled
against the game sources with Greenfoot's bundled JDK — see
[`test/InteropCheck.java`](test/InteropCheck.java).

## Deploy

Publish the contents of this directory (`index.html`, `styles.css`, the three
`.js` files, and the `assets/` sprites) under the path
`codebot.archontis.gr/lvl-editor`. The `test/` and `samples/` directories are
dev-only and need not be deployed.
