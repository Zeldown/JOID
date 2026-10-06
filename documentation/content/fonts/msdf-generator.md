# MSDF Generator

The `msdf` module turns a `.ttf`, `.otf` or `.ttc` font into the `font.msdf` atlas that `MsdfFontLoader` reads (see [How Fonts Work](how-fonts-work.md#the-atlas) for what an atlas holds). It is pure Java (Java 8 or later, no native binary) and ships as a runnable zip. Use it to ship ready-made atlases, so the first launch does not generate them, or to choose your own characters and atlas size.

## Quick start

1. Download `joid-msdf-generator-8.0.0.zip` from the release and unzip it anywhere.
2. Run the script of your system:

```sh
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
```

```bat
.\msdf.bat --font Inter-Regular.ttf --output assets\fonts\Inter-Regular
```

3. Put the output directory in your resources and load the atlas:

```java
final MsdfFont inter = MsdfFontLoader.load(Fonts.class.getResourceAsStream("/assets/fonts/Inter-Regular/font.msdf")).join();
```

The command writes a single `font.msdf` in the output directory and prints one line:

```
<file> -> <font name>, weight <weight>[ italic], <glyphs> glyphs, <pairs> kerning pairs, size <em size>px, <file size>kb, <time>ms
```

## The release zip

| File | Content |
|---|---|
| `joid-msdf-8.0.0.jar` | The generator, runnable with `java -jar` (main class `dev.joid.msdf.MsdfGenerator`). |
| `msdf.sh`, `msdf.bat` | Run `java -jar joid-msdf-8.0.0.jar` with your arguments. |
| `charset.txt` | The default charset, `[32, 563]`. |
| `README.md`, `LICENSE`, `NOTICE` | Usage and licenses. |

From a clone of the repository, `./gradlew msdfGenerator` builds the zip into `build/distributions`.

## Command-line options

Options are `--name value` pairs. Without `--font`, the command prints its usage.

| Option | Default | Description |
|---|---|---|
| `--font` | Required | Source `.ttf`, `.otf` or `.ttc` file. A collection gives the atlas of its first font. |
| `--output` | `output` | Directory that receives `font.msdf`, created when missing. |
| `--charset` | `[32, 563]` | Characters to include: a charset file, or the charset inline. |
| `--range` | `24` | Distance field range, in atlas pixels. |
| `--width` | `2048` | Atlas width, in pixels. |
| `--height` | `2048` | Atlas height, in pixels. |
| `--size` | Fitted | Em size, in atlas pixels. |

Without `--size`, the generator searches the largest em size, in steps of 1/16 pixel, whose glyphs still fit the atlas, and writes it into the file: a larger charset or a smaller atlas gives a smaller em size. With `--size`, the generation fails with `IllegalStateException` "The glyphs do not fit in WxH at Spx" when the glyphs do not fit.

The fonts `MsdfFontLoader` generates at runtime use the defaults: charset `[32, 563]`, 2048×2048, range 24, fitted size.

## Charsets

A charset is a comma-separated list of decimal codepoints and inclusive ranges `[first, last]`:

```
[32, 126], [160, 255], 8364
```

- `--charset` takes the path of a file holding that text, or the text itself when no such file exists.
- `[32, 563]` covers Basic Latin, Latin-1, Latin Extended-A and part of Latin Extended-B.
- Characters the font does not provide are skipped, so one charset works for every font.
- A character without outline, like the space, keeps its advance and takes no room in the atlas.
- A large set such as a full CJK block does not fit a 2048×2048 atlas at a usable size: generate the subset you need.

```sh
./msdf.sh --font NotoSansJP-Regular.otf --output assets/fonts/NotoSansJP --charset japanese-charset.txt --width 4096 --height 4096
```

## Kerning

The generator reads the kerning out of the font:

- The `GPOS` pair positioning of the `kern` feature, with single pairs, class pairs and extension lookups. In a lookup, the first subtable that describes a pair wins; the values of several lookups add up.
- The legacy `kern` table (horizontal format 0 subtables), only when `GPOS` gives no pair.

Only the pairs of two characters of the charset are kept. Text drawn and measured with the atlas applies them on its own, scaled to the font size.

## Families and styles

Generate one atlas per face, then load them together as one family:

```sh
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
./msdf.sh --font Inter-Italic.ttf --output assets/fonts/Inter-Italic
./msdf.sh --font Inter-Bold.ttf --output assets/fonts/Inter-Bold
```

```java
final MsdfFont inter = MsdfFontLoader.load(
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Regular/font.msdf"),
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Italic/font.msdf"),
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Bold/font.msdf")
).join();
```

Each atlas records the weight (`usWeightClass` of the `OS/2` table, 400 when absent), the italic flag (italic or oblique bit of the `OS/2` table, or italic bit of the `head` table) and the full name of the font. Override them when loading with `MsdfBinarySource.of(handle).weight(...)` or `.italic(...)` (see [Adding Your Own Fonts](adding-fonts.md#overriding-a-face-with-msdfsource)).

## Generating from Gradle

In a clone of the repository, the `msdf` module has a `generateFont` task:

```sh
./gradlew :msdf:generateFont -Pfont=/path/to/Inter-Regular.ttf -Poutput=/path/to/assets/fonts/Inter-Regular
```

| Property | Default |
|---|---|
| `-Pfont` | Required |
| `-Poutput` | `build/font` |
| `-Pcharset` | The `charset.txt` of the module |
| `-Prange` | `24` |
| `-Pwidth`, `-Pheight` | `2048` |

Relative paths resolve against the `msdf` module directory; the task has no `--size` property.

## Generating from code with MsdfGenerator

`dev.joid.msdf.MsdfGenerator` is part of the JOID core jar, so your build or your application can generate atlases itself:

| Member | Description |
|---|---|
| `MsdfGenerator.generate(File font, File output, int[] codepoints, int width, int height, double range, double size)` | Writes `output/font.msdf`, creating the directory, and prints the summary line. `size` 0 fits the em size. |
| `MsdfGenerator.generate(byte[] font, File target, int[] codepoints, int width, int height, double range, double size)` | Writes the atlas into `target` and returns the summary (name, weight, glyph and pair counts, em size). |
| `MsdfGenerator.codepoints(String charset)` | Codepoints of an inline charset or of a charset file. |
| `MsdfGenerator.main(String[] arguments)` | The command line. |
| `WIDTH`, `HEIGHT`, `RANGE`, `CHARSET` | The defaults: `2048`, `2048`, `24D`, `"[32, 563]"`. |

Every method throws `Exception`.

```java
final int[] codepoints = MsdfGenerator.codepoints("[32, 126], [160, 255], 8364");
MsdfGenerator.generate(new File("fonts/Inter-Regular.ttf"), new File("build/fonts/Inter-Regular"), codepoints, MsdfGenerator.WIDTH, MsdfGenerator.HEIGHT, MsdfGenerator.RANGE, 0D);
```

`MsdfFontCache.resolve(byte[])` and `MsdfFontCache.generate(byte[], File)` generate with the defaults (see [The MSDF cache](adding-fonts.md#the-msdf-cache-with-msdffontcache)).

## The .msdf format

A `.msdf` file starts with the 8 bytes `JOIDMSDF`, followed by one deflate (zlib) stream. JOID 8.0.0 reads and writes version 4 only: a file of another version is refused with an `IOException`, generate it again. Numbers are big-endian, as written by `DataOutputStream`.

| Field | Type | Content |
|---|---|---|
| Version | unsigned byte | `4` |
| Weight | unsigned short | `usWeightClass` of the font. |
| Italic | boolean | Italic flag. |
| Name | modified UTF-8 | Full name of the font. |
| Width, height | int, int | Atlas size, in pixels. |
| Range | float | Distance range, in atlas pixels. |
| Size | float | Em size, in atlas pixels. |
| Metrics | 5 floats | Line height, ascender, descender, underline Y, underline thickness, as fractions of the em. |
| Glyph count | int | Number of glyph records. |
| Glyph record | int, float, boolean, then 4 floats and 4 unsigned shorts when drawable | Codepoint, advance (em), drawable flag; plane bounds left, bottom, right, top (em, from the pen on the baseline, Y up); atlas bounds left, bottom, right, top (pixels, Y from the bottom row). |
| Units per em | unsigned short | Units per em of the font. |
| Kerning | variable-length integers | Group count; per group, the delta of the first codepoint and the pair count; per pair, the delta of the second codepoint and the zigzag-encoded value in font units. |
| Pixels | rows | From the top row down, each row is one filter byte (0 none, 1 sub, 2 up, 3 average, 4 Paeth, as in PNG) and `width` × 3 bytes of RGB distance. |

Variable-length integers store 7 bits per byte, low bits first, with the high bit set on every byte but the last.

![Colored distance fields of five glyphs above the shapes rebuilt from them](../images/msdf-atlas.png "The pixels of a real font.msdf (Montserrat Bold, 2× scale): three distances per texel; below, the median of the channels thresholded at the outline.")

## See also

- [How Fonts Work](how-fonts-work.md)
- [Adding Your Own Fonts](adding-fonts.md)
- [Text and TextInfo](../text/text-and-textinfo.md)
- [Installation](../getting-started/installation.md)