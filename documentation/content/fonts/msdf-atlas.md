# MSDF Atlas

How to generate the `.png` + `.json` pair that JOID's font system consumes.

## Tool: `msdf-atlas-gen`

Download from [github.com/Chlumsky/msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen/releases).

## Generate a font

The exact command used to produce the fonts JOID ships with:

```bash
msdf-atlas-gen.exe -font font.ttf -charset charset.txt -dimensions 2048 2048 -imageout font.png -json font.json -type msdf -pxrange 24 -coloringstrategy distance
```

Point `-font` at your TTF/OTF source, drop the characters you want in `charset.txt`, and you're done. The other flags are JOID's proven defaults.

### Parameters

| Flag | Purpose |
|---|---|
| `-font` | Source TTF/OTF file |
| `-charset` | List of characters to include (one per line) |
| `-dimensions` | Atlas size in pixels |
| `-imageout` | Output PNG path |
| `-json` | Output JSON path (glyph metrics) |
| `-type msdf` | Multi-channel SDF — best quality for UI fonts |
| `-pxrange` | Distance field range (higher = smoother at small sizes, more blur at large sizes; 24 is a good default) |
| `-coloringstrategy distance` | MSDF coloring algorithm (don't touch unless you know what you're doing) |

## Charset

The `charset.txt` file contains the characters to include, one per line or in a single line. A good starting set:

```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
0123456789
!@#$%^&*()_+-=[]{}|;:,.<>?/~`'"\
 éèêëàâäôöûüùçÉÈÊËÀÂÄÔÖÛÜÙÇ
```

For CJK, use a subset — full CJK is 30k+ glyphs and won't fit in a 2048×2048 atlas.

## Output structure

JOID expects this layout:

```
assets/
└── fonts/
    └── MyFont/
        ├── font.png
        └── font.json
```

Load them by passing the two streams to `FontLoader.load(pngStream, jsonStream)`.

## JSON schema

`font.json` structure (simplified):

```json
{
    "atlas": {
        "type": "msdf",
        "distanceRange": 24,
        "size": 32,
        "width": 2048,
        "height": 2048,
        "yOrigin": "bottom"
    },
    "metrics": {
        "emSize": 1,
        "lineHeight": 1.171875,
        "ascender": 0.9296875,
        "descender": -0.2421875,
        ...
    },
    "glyphs": [
        { "unicode": 65, "advance": 0.6, "planeBounds": {...}, "atlasBounds": {...} },
        ...
    ]
}
```

JOID's `FontLoader` handles both `yOrigin: bottom` and `yOrigin: top`.

## Multiple weights

Generate one atlas per weight:

```bash
msdf-atlas-gen -font Inter-Regular.ttf -imageout Regular/font.png -json Regular/font.json ...
msdf-atlas-gen -font Inter-Bold.ttf -imageout Bold/font.png -json Bold/font.json ...
msdf-atlas-gen -font Inter-Italic.ttf -imageout Italic/font.png -json Italic/font.json ...
```

Load each separately and bind them to different `TextInfo`s.

## Best practices

- **`-pxrange 24`** is a good general-purpose default.
- **`-dimensions 2048 2048`** fits most Latin fonts. Check the `overflow` warning — if the atlas overflows, bump to 4096 or trim the charset.
- **Keep the PNG alpha channel.** Some tools strip it; JOID needs RGBA.
- **Regenerate when changing the source font.** Even minor tweaks (spacing, hinting) require a new atlas.

## See also

- [Custom Fonts](custom-font.md) — loading and using fonts.
- [TextNode](../nodes/design/text.md).
