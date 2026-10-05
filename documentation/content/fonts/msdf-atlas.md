# MSDF Atlas

How to turn a `.ttf` or `.otf` into the `font.msdf` file JOID's font system consumes.

## The generator

JOID ships its own generator in the `:msdf` module. It is pure Java, runs on any OS, needs no native binary, and reads the kerning straight out of the font — both the legacy `kern` table and the `GPOS` pair positioning that modern fonts use.

```bash
./gradlew :msdf:generateFont -Pfont=/path/to/Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
```

It writes a single `font.msdf` next to the `-Poutput` path.

### Parameters

| Property | Default | Purpose |
|---|---|---|
| `-Pfont` | — | Source `.ttf` or `.otf` file |
| `-Poutput` | `build/font` | Directory that receives `font.msdf` |
| `-Pcharset` | `msdf/charset.txt` | Codepoints to include |
| `-Prange` | `24` | Distance field range in pixels — higher is smoother at small sizes, blurrier at large ones |
| `-Pwidth` / `-Pheight` | `2048` | Atlas size in pixels |

The glyph resolution is not a parameter: the generator binary-searches the largest em size whose glyphs still fit the atlas, and writes it into the file.

### Regenerating the fonts JOID ships

`msdf/fonts.txt` maps each bundled atlas to its source file. Point the task at a directory holding those files:

```bash
./gradlew :msdf:rebuildFonts -Pfonts=/path/to/font/sources
```

## Charset

`charset.txt` accepts ranges and single codepoints:

```
[32, 563]
```

That range covers Latin, Latin-1, Latin Extended-A and part of Extended-B — the set JOID's own fonts use. Codepoints the font does not provide are skipped, so the same charset works for every source.

For CJK, use a subset: a full CJK set runs past 30k glyphs and will not fit a 2048×2048 atlas.

## The `.msdf` file

One file holds everything: atlas metrics, glyph bounds, kerning pairs and the multi-channel distance field, deflated as a whole. The field is stored with the same adaptive row filtering a PNG uses, so a complete font weighs about the same as the raw `.png` would on its own, with the metrics and the kerning table carried along for free.

```
assets/
└── fonts/
    └── MyFont/
        └── font.msdf
```

Load it with a single stream:

```java
FontLoader.load(getClass().getResourceAsStream("/assets/fonts/MyFont/font.msdf"), font -> this.myFont = font);
```

## Legacy atlases

The `font.json` + `font.png` pair produced by [msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen) still loads, through the `FontInputStream` overloads:

```java
FontLoader.load(new FontInputStream(jsonStream, pngStream), font -> this.myFont = font);
```

Those atlases carry no kerning: `msdf-atlas-gen` reads only the `kern` table, which most modern fonts no longer ship.

## Multiple weights

Generate one atlas per weight, then load each one separately and bind them to different `TextInfo`s:

```bash
./gradlew :msdf:generateFont -Pfont=Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
./gradlew :msdf:generateFont -Pfont=Inter-Bold.ttf    -Poutput=assets/fonts/Inter-Bold
```

A regular and a bold atlas can also be paired into one `CustomFont`, which is what the `§l` style switch draws from:

```java
FontLoader.load(regularStream, boldStream, font -> this.myFont = font);
```

## Best practices

- **Keep the range at 24.** It is the value every bundled font uses.
- **2048×2048 fits most Latin fonts.** If the generator reports a small em size, trim the charset rather than growing the atlas.
- **Regenerate after any change to the source font.** Spacing, outlines and kerning are all baked into the file.

## See also

- [Custom Fonts](custom-font.md) — loading and using fonts.
- [TextNode](../nodes/design/text.md).
