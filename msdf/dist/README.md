# JOID MSDF generator

Turns a `.ttf` or `.otf` into the `font.msdf` atlas that [JOID](https://github.com/Zeldown/JOID) @JOID_VERSION@ loads. Pure Java, no native binary, no install: the same font gives the same atlas on every OS.

Java 8 or later is the only requirement.

## Run

```sh
./msdf.sh --font Inter-Regular.ttf --output fonts/Inter-Regular
```

```bat
.\msdf.bat --font Inter-Regular.ttf --output fonts\Inter-Regular
```

Both scripts wrap `java -jar joid-msdf-@JOID_VERSION@.jar`, which you can call directly. The command writes a single `font.msdf` in the output directory — weight and style of the face, atlas, glyph metrics, kerning pairs and the distance field, compressed as one file.

Drop that directory in your resources and load it with one stream:

```java
MsdfFontLoader.load(MyMod.class.getResourceAsStream("/assets/fonts/Inter-Regular/font.msdf"));
```

Generate one atlas per weight and style of a family, then load them together: the weight and the italic flag are read from the font, and each `TextInfo` picks the weight it draws with.

```java
MsdfFontLoader.load(regularStream, italicStream, boldStream);
```

## Options

| Option | Default | Purpose |
|---|---|---|
| `--font` | — | Source `.ttf` or `.otf` file |
| `--output` | `output` | Directory that receives `font.msdf` |
| `--charset` | `[32, 563]` | Codepoints to include, as a file or inline ranges |
| `--range` | `24` | Distance field range in pixels — higher is smoother at small sizes, blurrier at large ones |
| `--width` / `--height` | `2048` | Atlas size in pixels |
| `--size` | — | Forces the em size in pixels instead of fitting the atlas |

The glyph resolution is not a parameter: the generator binary-searches the largest em size whose glyphs still fit the atlas, and writes it into the file. `--size` overrides that and fails when the glyphs do not fit.

## Charset

`charset.txt` ships next to the jar and holds the set JOID's own fonts use:

```
[32, 563]
```

That range covers Latin, Latin-1, Latin Extended-A and part of Extended-B. Ranges and single codepoints both work, separated by commas:

```
[32, 126], [160, 255], 8364
```

Codepoints the font does not provide are skipped, so the same charset works for every source. For CJK, use a subset: a full CJK set runs past 30k glyphs and will not fit a 2048x2048 atlas.

```sh
./msdf.sh --font Inter-Regular.ttf --output fonts/Inter-Regular --charset charset.txt
```

## Kerning

The generator reads the kerning out of the font itself — the legacy `kern` table and the `GPOS` pair positioning that modern fonts use, class pairs and extension lookups included — and resolves it the way a text shaper does: the first subtable of a lookup that describes a pair wins, and the lookups of the `kern` feature accumulate. Nothing to enable, nothing to pass.
