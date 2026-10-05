# MSDF Atlas

How to turn a `.ttf` or `.otf` into the `font.msdf` file JOID's font system consumes. `MsdfFontLoader` also takes the font file itself and generates that file at runtime — see [Font files](custom-font.md#font-files).

## The generator

JOID ships its own generator. It is pure Java, runs on any OS, needs no native binary, and reads the kerning straight out of the font — both the legacy `kern` table and the `GPOS` pair positioning that modern fonts use. The same source font gives the same atlas on every machine.

### From a release

Download `joid-msdf-generator-X.Y.Z.zip` from the [Releases page](https://github.com/Zeldown/JOID/releases), unzip it anywhere and run the script of your system. Java 8 or later is the only requirement.

```bash
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
```

```bat
.\msdf.bat --font Inter-Regular.ttf --output assets\fonts\Inter-Regular
```

Both scripts wrap `java -jar joid-msdf-X.Y.Z.jar`, which you can call directly. The command writes a single `font.msdf` in the output directory.

| Option | Default | Purpose |
|---|---|---|
| `--font` | — | Source `.ttf` or `.otf` file |
| `--output` | `output` | Directory that receives `font.msdf` |
| `--charset` | `[32, 563]` | Codepoints to include, as a file or inline ranges |
| `--range` | `24` | Distance field range in pixels — higher is smoother at small sizes, blurrier at large ones |
| `--width` / `--height` | `2048` | Atlas size in pixels |
| `--size` | — | Forces the em size in pixels instead of fitting the atlas |

The glyph resolution is not a parameter: the generator binary-searches the largest em size whose glyphs still fit the atlas, and writes it into the file. `--size` overrides that search and fails when the glyphs do not fit.

### From the sources

In a clone of the repository, the `:msdf` module exposes the same generator as a Gradle task:

```bash
./gradlew :msdf:generateFont -Pfont=/path/to/Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
```

| Property | Default | Purpose |
|---|---|---|
| `-Pfont` | — | Source `.ttf` or `.otf` file |
| `-Poutput` | `build/font` | Directory that receives `font.msdf` |
| `-Pcharset` | `msdf/charset.txt` | Codepoints to include |
| `-Prange` | `24` | Distance field range in pixels |
| `-Pwidth` / `-Pheight` | `2048` | Atlas size in pixels |

`./gradlew msdfGenerator` packages that module into the release zip, in `build/distributions`.

### Regenerating the fonts JOID ships

`msdf/fonts.txt` maps each bundled atlas to its source file. Point the task at a directory holding those files:

```bash
./gradlew :msdf:rebuildFonts -Pfonts=/path/to/font/sources
```

## Charset

`charset.txt`, shipped next to the jar in the release zip, accepts ranges and single codepoints:

```
[32, 563]
```

That range covers Latin, Latin-1, Latin Extended-A and part of Extended-B — the set JOID's own fonts use. Several entries are separated by commas, and the whole thing can be passed inline instead of in a file:

```bash
./msdf.sh --font Inter-Regular.ttf --output fonts/Inter --charset "[32, 126], [160, 255], 8364"
```

Codepoints the font does not provide are skipped, so the same charset works for every source.

For CJK, use a subset: a full CJK set runs past 30k glyphs and will not fit a 2048×2048 atlas.

## The `.msdf` file

One file holds everything: the weight and style of the face, atlas metrics, glyph bounds, kerning pairs and the multi-channel distance field, deflated as a whole. The weight and the italic flag come from the `OS/2` table of the source font, so a family loads without telling JOID which file is which. The field is stored with the same adaptive row filtering a PNG uses, so a complete font weighs about the same as the raw `.png` would on its own. The kerning table is grouped by first codepoint and written as variable-length deltas in font units, which costs a tenth of what a flat pair list would: fifty thousand pairs fit in fifteen kilobytes.

```
assets/
└── fonts/
    └── MyFont/
        └── font.msdf
```

Load it with a single stream:

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/assets/fonts/MyFont/font.msdf")).thenAccept(font -> this.myFont = font);
```

## Legacy atlases

The `font.json` + `font.png` pair produced by [msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen) still loads, once both files are wrapped in an `MsdfJsonSource`:

```java
MsdfFontLoader.load(MsdfJsonSource.of(jsonStream, pngStream)).thenAccept(font -> this.myFont = font);
```

Those atlases carry no kerning: `msdf-atlas-gen` reads only the `kern` table, which most modern fonts no longer ship.

## Multiple weights

Generate one atlas per weight and style:

```bash
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
./msdf.sh --font Inter-Italic.ttf  --output assets/fonts/Inter-Italic
./msdf.sh --font Inter-Bold.ttf    --output assets/fonts/Inter-Bold
```

Then load them together as one family — each `TextInfo` picks its weight, see [Weights and styles](custom-font.md#weights-and-styles):

```java
MsdfFontLoader.load(regularStream, italicStream, boldStream).thenAccept(font -> this.myFont = font);
```

## Best practices

- **Keep the range at 24.** It is the value every bundled font uses.
- **2048×2048 fits most Latin fonts.** If the generator reports a small em size, trim the charset rather than growing the atlas.
- **Regenerate after any change to the source font.** Spacing, outlines and kerning are all baked into the file.

## See also

- [Custom Fonts](custom-font.md) — loading and using fonts.
- [Markup & Effects](markup-effects.md) — styling a string from the inside.
- [TextNode](../nodes/design/text.md).
