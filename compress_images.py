#!/usr/bin/env python3
"""
Losslessly recompress and right-size every PNG under game/images.

WHAT "the size they should be" MEANS
------------------------------------
Nothing in this game is ever drawn at its source resolution. Greenfoot loads a
PNG and then calls GreenfootImage.scale(w, h) (or fits it preserving aspect)
before blitting. So a 4835x3716 frame that is only ever shown at <=960x720 is
~25x more pixels than the screen can use -- pure bloat.

The TARGETS table below is the display size each image actually renders at,
derived directly from the Java layout code for the current screen scale:

    GameScreenLayout.SCALE_NUM / SCALE_DEN = 6 / 5  (1.2x)
      -> world           960 x 720   (WORLD_WIDTH x WORLD_HEIGHT)
      -> game area        720 x 600
      -> tile             30 x 30    (TILE_SIZE_PX = scale(25))
      -> script area      240 x 600
      -> terminal/controls 720x120 / 240x120

If you change SCALE_NUM/SCALE_DEN in GameScreenLayout.java, either re-derive the
numbers below or just bump --supersample so the assets keep enough resolution.

GUARANTEES
----------
* Resizing only ever shrinks an image, never enlarges it (no fake detail, and
  an asset that is already small enough is left at its size).
* Recompression is verified lossless: every candidate encoding is decoded again
  and compared pixel-for-pixel against the (possibly resized) reference before
  it is allowed to replace the file. A palette-reduced copy is only kept when it
  round-trips identically.
* A file is only rewritten when it gets smaller (or its dimensions changed).

USAGE
-----
    python compress_images.py            # apply in place (originals are in git)
    python compress_images.py --dry-run  # report only, touch nothing
    python compress_images.py --supersample 1.5   # keep 1.5x headroom
    python compress_images.py --root some/other/images

Requires Pillow (`pip install Pillow`). If `oxipng`, `zopflipng`, or `optipng`
is on PATH it is used for an extra lossless squeeze (disable with --no-external).
"""

from __future__ import annotations

import argparse
import fnmatch
import io
import math
import os
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required:  pip install Pillow")

IMAGE_EXTS = (".png", ".gif", ".bmp")  # lossless raster formats we own

EXACT = "exact"  # runtime stretches to these exact dims (aspect may change)
FIT = "fit"      # runtime preserves aspect; box is the bound, None = free axis

# (glob relative to the images root, mode, width, height)
# Width/height are post-scale display pixels at 6/5. None = unconstrained axis.
# First matching pattern wins, so list specific paths before wildcards.
TARGETS: list[tuple[str, str, "int | None", "int | None"]] = [
    # ---- Full-screen / panel backgrounds -----------------------------------
    ("ui/generic-bg.png",                  EXACT, 960, 720),   # bg.scale(W,H) everywhere
    ("home_bg.png",                        EXACT, 960, 720),   # legacy full-screen bg
    ("ui/game_area_frame.png",             FIT,   960, 720),   # frame overlay, bounded by world
    ("intro_slides/*.png",                 FIT,   960, 504),   # IntroWorld IMAGE_H = H*70%
    ("ui/code-editor-bg.png",              EXACT, 240, 600),   # CodeEditor bg.scale(W,H)
    ("ui/controls-bg.png",                 EXACT, 216, 108),   # 0.9 * controls panel
    ("ui/game-top-hud-bar.png",            EXACT, 960,  33),   # HUD strip
    ("ui/output_background.png",           EXACT, 720, 120),   # terminal strip (already small)

    # ---- Result panels (aspect preserved at runtime) -----------------------
    ("ui/final_classification_ui_element.png", FIT, 844, 465), # PANEL_FIT 0.88
    ("ui/level-compete-panel.png",         FIT,   650, 576),   # PANEL_FIT 0.80
    ("ui/type_i_shield.png",               FIT,   None, 267),  # sc(SHIELD_H 372)
    ("ui/type_ii_shield.png",              FIT,   None, 267),
    ("ui/type_iii_shield.png",             FIT,   None, 267),
    ("ui/incomplete-shield.png",           FIT,   None, 267),
    ("ui/type_i_card.png",                 FIT,   152, None),  # sc(CARD_W 212)
    ("ui/type_ii_card.png",                FIT,   152, None),
    ("ui/type_iii_card.png",               FIT,   152, None),
    ("ui/num_*.png",                       FIT,   None, 87),   # sc(NUM_H 122)
    ("ui/star-element.png",                FIT,   85,  81),    # LevelComplete star box
    ("ui/no-star-element.png",             FIT,   85,  81),

    # ---- Buttons stretched to a fixed slot (MenuButton.scale(w,h)) ---------
    ("ui/start-btn.png",                   EXACT, 211, 70),    # Home menu button
    ("ui/settings-btn.png",                EXACT, 211, 70),
    ("ui/instructions-btn.png",            EXACT, 211, 70),
    ("ui/credits-btn.png",                 EXACT, 211, 70),
    ("ui/free-play-btn.png",               EXACT, 264, 88),    # max slot (FreePlay)
    ("ui/back-btn.png",                    EXACT, 264, 88),
    ("ui/load-custom-btn.png",             EXACT, 264, 88),
    ("ui/clear-btn.png",                   EXACT, 168, 56),    # LoadCustom toolbar
    ("ui/paste-btn.png",                   EXACT, 168, 56),
    ("ui/run-btn.png",                     EXACT, 72,  38),    # 0.8 * run slot
    ("ui/reset-btn.png",                   EXACT, 86,  38),    # 0.8 * reset slot
    ("ui/step-btn.png",                    EXACT, 57,  18),    # CodeEditor STEP button

    # ---- Buttons sized by height, aspect preserved -------------------------
    ("ui/final-results-btn.png",           FIT,   None, 44),   # sc(BUTTON_TARGET_H 62)
    ("ui/home-btn.png",                    FIT,   None, 44),   # (source already small)
    ("ui/replay-btn.png",                  FIT,   None, 44),
    ("ui/next-lvl-btn.png",                FIT,   None, 44),

    # ---- Logos -------------------------------------------------------------
    ("logo/logo_white_outline.png",        FIT,   528, None),  # LOGO_WIDTH = scale(440)
    ("logo/logo_dark_outline.png",         FIT,   528, None),

    # ---- Tiles & actors: everything is forced to one 30px cell -------------
    ("game-grid-tiles/floor/floor_tile_*.png", EXACT, 30, 30),
    ("game-grid-tiles/general/coin.png",   EXACT, 30, 30),
    ("game-grid-tiles/general/goal-tile.png", EXACT, 30, 30),
    ("floor-tiles/obstacle_1.png",         EXACT, 30, 30),
    ("robot/robot.png",                    EXACT, 30, 30),
]

# Images with no known display size (cursor handled in a compiled class, plus a
# couple of unreferenced assets). These are recompressed but never resized.
KNOWN_NO_TARGET = {
    "ui/cursor-824.png",
    "ui/image14228.png",
    "snake2.png",
}


@dataclass
class Result:
    rel: str
    ow: int
    oh: int
    nw: int
    nh: int
    obytes: int
    nbytes: int
    action: str  # "rewrote", "skip (optimal)", "dry-run", "no target"


def find_target(rel_posix: str):
    """Return (mode, w, h) for the first matching pattern, or None."""
    for pattern, mode, w, h in TARGETS:
        if fnmatch.fnmatch(rel_posix, pattern):
            return mode, w, h
    return None


def planned_size(mode, tw, th, cw, ch, supersample):
    """Compute the new (w, h), only ever shrinking from the current size."""
    if tw is not None:
        tw = max(1, round(tw * supersample))
    if th is not None:
        th = max(1, round(th * supersample))

    if mode == EXACT:
        nw = tw if tw is not None else cw
        nh = th if th is not None else ch
        # Never upscale a given axis.
        return min(nw, cw), min(nh, ch)

    # FIT: scale down uniformly to fit inside the box.
    rx = tw / cw if tw else math.inf
    ry = th / ch if th else math.inf
    r = min(rx, ry)
    if r >= 1.0:
        return cw, ch  # already small enough
    return max(1, round(cw * r)), max(1, round(ch * r))


def encode_png(img: Image.Image) -> bytes:
    """Lossless PNG bytes, max zlib effort, ancillary chunks stripped."""
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True, compress_level=9)
    return buf.getvalue()


def rgba_bytes(img: Image.Image) -> bytes:
    return img.convert("RGBA").tobytes()


def lossless_candidates(ref: Image.Image) -> list[bytes]:
    """Encodings that decode back to exactly `ref`'s pixels, smallest-friendly."""
    reference = rgba_bytes(ref)
    out: list[bytes] = []

    # 1) Straight re-encode in the working mode.
    direct = encode_png(ref)
    if Image.open(io.BytesIO(direct)).convert("RGBA").tobytes() == reference:
        out.append(direct)

    # 2) Palette reduction -- only kept if it is truly lossless (<=256 colours).
    try:
        pal = ref.convert("RGBA").quantize(
            colors=256, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE
        )
        pal_bytes = encode_png(pal)
        if Image.open(io.BytesIO(pal_bytes)).convert("RGBA").tobytes() == reference:
            out.append(pal_bytes)
    except Exception:
        pass

    return out


EXTERNAL_OPTIMIZERS = [
    ("oxipng", lambda p: ["oxipng", "-o", "max", "--strip", "safe", "-q", p]),
    ("zopflipng", lambda p: ["zopflipng", "-y", p, p]),
    ("optipng", lambda p: ["optipng", "-quiet", "-o7", p]),
]


def external_squeeze(data: bytes, reference_rgba: bytes) -> bytes:
    """Run the first available external optimizer; keep result only if smaller and lossless."""
    tool = next(((n, b) for n, b in EXTERNAL_OPTIMIZERS if shutil.which(n)), None)
    if not tool:
        return data
    name, build = tool
    tmp = tempfile.NamedTemporaryFile(suffix=".png", delete=False)
    try:
        tmp.write(data)
        tmp.close()
        subprocess.run(build(tmp.name), check=True,
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        with open(tmp.name, "rb") as fh:
            squeezed = fh.read()
        ok = Image.open(io.BytesIO(squeezed)).convert("RGBA").tobytes() == reference_rgba
        if ok and len(squeezed) < len(data):
            return squeezed
        return data
    except Exception:
        return data
    finally:
        try:
            os.unlink(tmp.name)
        except OSError:
            pass


def process(path: str, rel_posix: str, args) -> Result:
    obytes = os.path.getsize(path)
    with Image.open(path) as im:
        im.load()
        cw, ch = im.size

        target = find_target(rel_posix)
        nw, nh = cw, ch
        if target:
            nw, nh = planned_size(*target, cw, ch, args.supersample)

        resized = (nw, nh) != (cw, ch)
        work = im.resize((nw, nh), Image.Resampling.LANCZOS) if resized else im.copy()

    if args.no_resize:
        resized = False
        nw, nh = cw, ch
        with Image.open(path) as im:
            im.load()
            work = im.copy()

    # Build best lossless encoding.
    if args.no_optimize and not resized:
        return Result(rel_posix, cw, ch, cw, ch, obytes, obytes,
                      "skip (--no-optimize)")

    candidates = lossless_candidates(work)
    if not candidates:
        # Should never happen (direct re-encode is always lossless), but be safe.
        return Result(rel_posix, cw, ch, nw, nh, obytes, obytes, "skip (no lossless candidate)")

    best = min(candidates, key=len)
    if not args.no_external:
        best = external_squeeze(best, rgba_bytes(work))

    no_target = target is None
    # Decide whether to write: dimensions changed, or we genuinely got smaller.
    smaller = len(best) < obytes
    should_write = resized or smaller

    if args.dry_run:
        action = "dry-run" + ("" if should_write else " (no gain)")
        nb = len(best) if should_write else obytes
        return Result(rel_posix, cw, ch, nw, nh, obytes, nb, action)

    if not should_write:
        action = "no target" if no_target else "skip (optimal)"
        return Result(rel_posix, cw, ch, nw, nh, obytes, obytes, action)

    with open(path, "wb") as fh:
        fh.write(best)
    return Result(rel_posix, cw, ch, nw, nh, obytes, len(best), "rewrote")


def kb(n: int) -> str:
    return f"{n/1024:8.1f}KB"


def main() -> int:
    here = os.path.dirname(os.path.abspath(__file__))
    default_root = os.path.join(here, "game", "images")

    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--root", default=default_root,
                    help="images directory (default: game/images)")
    ap.add_argument("--dry-run", action="store_true",
                    help="report what would change without writing")
    ap.add_argument("--supersample", type=float, default=1.0,
                    help="multiply every target size (headroom for higher scales)")
    ap.add_argument("--no-resize", action="store_true",
                    help="recompress only, keep current dimensions")
    ap.add_argument("--no-optimize", action="store_true",
                    help="resize only, skip recompression of unchanged-size files")
    ap.add_argument("--no-external", action="store_true",
                    help="do not call oxipng/zopflipng/optipng even if installed")
    args = ap.parse_args()

    root = os.path.abspath(args.root)
    if not os.path.isdir(root):
        sys.exit(f"not a directory: {root}")

    files = []
    for dirpath, _dirs, names in os.walk(root):
        for name in names:
            if name.lower().endswith(IMAGE_EXTS):
                full = os.path.join(dirpath, name)
                rel = os.path.relpath(full, root).replace(os.sep, "/")
                files.append((full, rel))
    files.sort(key=lambda t: t[1])

    print(f"Scanning {len(files)} images under {root}")
    print(f"{'image':52} {'before':>9} {'->':^4} {'after':>9}  dims          action")
    print("-" * 110)

    total_before = total_after = 0
    untargeted = []
    for full, rel in files:
        r = process(full, rel, args)
        total_before += r.obytes
        total_after += r.nbytes
        dims = f"{r.ow}x{r.oh}"
        if (r.nw, r.nh) != (r.ow, r.oh):
            dims += f"->{r.nw}x{r.nh}"
        saved = r.obytes - r.nbytes
        flag = f"  (-{saved/1024:.1f}KB)" if saved > 0 else ""
        print(f"{rel:52} {kb(r.obytes)} {'->':^4} {kb(r.nbytes)}  {dims:18} {r.action}{flag}")
        if r.action == "no target" and rel not in KNOWN_NO_TARGET:
            untargeted.append(rel)

    print("-" * 110)
    saved = total_before - total_after
    pct = (saved / total_before * 100) if total_before else 0
    verb = "would save" if args.dry_run else "saved"
    print(f"TOTAL: {kb(total_before)} -> {kb(total_after)}   {verb} {saved/1024:,.1f}KB ({pct:.1f}%)")

    if untargeted:
        print("\nNOTE: no display-size target known for these (recompressed only):")
        for rel in untargeted:
            print(f"  - {rel}   (add an entry to TARGETS to right-size it)")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
