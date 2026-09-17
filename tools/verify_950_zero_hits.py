#!/usr/bin/env python3
"""Read-only 950 numeric-zero hitmark/sprite evidence; only named outputs are written."""
import argparse, collections, hashlib, json, sys
from pathlib import Path
sys.dont_write_bytecode = True
from verify_950_hitbars import cache, hitmark, describe


def sprite(data):
    count = int.from_bytes(data[-2:], "big")
    assert count == 1
    meta = len(data) - 7 - 8 * count
    u16 = lambda offset: int.from_bytes(data[offset:offset + 2], "big")
    width, height, palette_count = u16(meta), u16(meta + 2), data[meta + 4] + 1
    x, y, sw, sh = [u16(meta + offset) for offset in (5, 7, 9, 11)]
    assert 0 <= x <= x + sw <= width and 0 <= y <= y + sh <= height
    palette_start = meta - 3 * (palette_count - 1)
    palette = [(0, 0, 0)] + [tuple(data[p:p + 3]) for p in range(palette_start, meta, 3)]
    flags = data[0]
    assert flags in (0, 1, 2, 3)
    size = sw * sh
    indices = data[1:1 + size]
    alpha = data[1 + size:1 + size * 2] if flags & 2 else bytes(255 if i else 0 for i in indices)
    assert 1 + size * (2 if flags & 2 else 1) == palette_start
    rgba = [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]
    for sy in range(sh):
        for sx in range(sw):
            at = sx * sh + sy if flags & 1 else sy * sw + sx
            rgba[y + sy][x + sx] = palette[indices[at]] + (alpha[at],)
    counts = collections.Counter(pixel[:3] for row in rgba for pixel in row if pixel[3] > 100)
    return rgba, {"width": width, "height": height, "flags": flags,
                  "opaqueCommonRGB": [(list(rgb), n) for rgb, n in counts.most_common(4)],
                  "opaquePixels": sum(counts.values()),
                  "bluePixels": sum(n for (r, g, b), n in counts.items() if b > max(r, g) + 30)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path)
    parser.add_argument("--sprites-image", type=Path)
    args = parser.parse_args()
    raw = cache.archive(2, 46)
    ids = [0, 14, 8, 22, 113, 123, 141, 158, 422, 428, 458, 464, 482, 492]
    definitions = {str(i): dict(describe(raw[i]), decoded=hitmark(raw[i])) for i in ids}
    for i in (458, 464):
        d = definitions[str(i)]["decoded"]
        assert d["text"] == "0" and "targets" not in d and d["duration"] == 50
    assert definitions["482"]["decoded"]["targets"] == [422, 458, 458, 458, 422]
    assert definitions["492"]["decoded"]["targets"] == [428, 464, 464, 464, 428]
    for i in (8, 22, 113, 123): assert definitions[str(i)]["decoded"]["text"] == "Dodged"
    sprite_ids = [23354, 23355, 23356, 23357, 23358, 23359, 23369]
    sprites, pixels = {}, {}
    for i in sprite_ids:
        files = cache.archive(8, i)
        assert list(files) == [0]
        pixels[i], decoded = sprite(files[0])
        sprites[str(i)] = dict(describe(files[0]), decoded=decoded, address=f"8/{i}/0")
        if i != 23369: assert decoded["bluePixels"] > decoded["opaquePixels"] * 0.9
    evidence = {"scope": "Direct literal blue0 styles458/464; positive damage0/14 unchanged.",
                "cache": str(cache.FLAT), "definitions": definitions, "sprites": sprites}
    if args.output: args.output.write_text(json.dumps(evidence, indent=2) + "\n", encoding="utf-8")
    if args.sprites_image:
        from PIL import Image, ImageDraw
        canvas = Image.new("RGB", (850, 180), (25, 25, 25))
        draw = ImageDraw.Draw(canvas)
        for column, i in enumerate(sprite_ids):
            rows = pixels[i]
            image = Image.new("RGBA", (len(rows[0]), len(rows)))
            image.putdata([pixel for row in rows for pixel in row])
            image = image.resize((image.width * 4, image.height * 4), Image.Resampling.NEAREST)
            canvas.paste(image, (column * 120, 35), image)
            draw.text((column * 120 + 2, 12), str(i), fill="white")
        canvas.save(args.sprites_image)
    print("PASS: direct458/464 contain literal0; six exact index8/file0 sprites are blue; positive0/14 untouched")
    for i in (458, 464): print("HITMARK", i, definitions[str(i)]["sha256"], definitions[str(i)]["decoded"])


if __name__ == "__main__": main()
