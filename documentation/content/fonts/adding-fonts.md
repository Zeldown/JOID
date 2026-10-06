# Adding Your Own Fonts

This page walks you through adding a font to your application: getting the files, loading them as a family with `MsdfFontLoader`, using the family in your text, and fixing what can go wrong. For the concepts behind families, faces and MSDF atlases, see [How Fonts Work](how-fonts-work.md).

## Step 1: Get the font files

`MsdfFontLoader` reads TrueType (`.ttf`), OpenType (`.otf`) and font collections (`.ttc`), or `.msdf` atlases made by the [MSDF Generator](msdf-generator.md).

- Take one file per face: `Inter-Regular.ttf`, `Inter-Italic.ttf`, `Inter-Bold.ttf`. Each weight and each italic you want to draw is one file.
- Check the license of the font allows you to embed it in your application.
- Put the files in your resources, for example `src/main/resources/assets/fonts/`.

## Step 2: Load the family with MsdfFontLoader

`MsdfFontLoader.load(Object... faces)` (`dev.joid.lib.font.impl.msdf`) reads every face in parallel on a pool of daemon threads and returns a `CompletableFuture<MsdfFont>`. Give it every face of the family at once:

```java
public final class Fonts {

    public static MsdfFont INTER;

    public static void load() {
        Fonts.INTER = MsdfFontLoader.load(
            Fonts.class.getResourceAsStream("/assets/fonts/Inter-Regular.ttf"),
            Fonts.class.getResourceAsStream("/assets/fonts/Inter-Italic.ttf"),
            Fonts.class.getResourceAsStream("/assets/fonts/Inter-Bold.ttf")
        ).join();
    }

}
```

- Call `join()` at startup to block until the family is ready, or chain `thenAccept(...)` to keep going and receive the font later.
- Load each family once and reuse the `MsdfFont` in every UI.
- The atlas textures are uploaded on the render thread the first time a face draws, so loading off the render thread is safe.

### Accepted handles

Each argument of `load` is one face, in any order:

| Handle | Read as |
|---|---|
| `InputStream` | The stream content. |
| `File` | The file content. |
| `String` | An HTTP(S) URL. |
| `Asset` | The asset content. |
| Any other object an `AssetLocator` supports | See [Assets](../resources/assets.md). |
| `IMsdfSource` | Read as is (see [Overriding a face](#overriding-a-face-with-msdfsource)). |

### Format detection

The format is detected from the first four bytes of the content, not from the file name:

| Content | What happens |
|---|---|
| TrueType (`.ttf`, including Apple `true` fonts) | The atlas is generated, or read from the [MSDF cache](#the-msdf-cache-with-msdffontcache). |
| OpenType with CFF outlines (`.otf`) | Same as TrueType. |
| Font collection (`.ttc`) | Same as TrueType, with the first font of the collection. |
| Anything else | Read as a `.msdf` atlas made by the [MSDF Generator](msdf-generator.md). |

### Weight, italic and name of a face

You do not declare which file is which: each face describes itself.

- The weight comes from the `usWeightClass` of the font (`OS/2` table, 400 when absent), rounded with `FontWeight.of`.
- The italic flag comes from the italic or oblique bits of the font.
- The face name is the full font name (`Inter Bold`).

A `.msdf` atlas carries the same three values from the font it was generated from. When a file declares the wrong values, [override them](#overriding-a-face-with-msdfsource).

## Step 3: Use the family in TextInfo

Give the family to a [`TextInfo`](../text/text-and-textinfo.md) and pick the weight and style there:

```java
final TextInfo body = TextInfo.create(Fonts.INTER, 18, Color.WHITE);
final TextInfo title = TextInfo.create(Fonts.INTER, FontWeight.BOLD, 32, Color.WHITE);
final TextInfo note = TextInfo.create(Fonts.INTER, 14, Color.WHITE).italic(true);

TextNode.create(0, 0).text(Text.create("Settings", title)).attach(flex);
```

![A bold Settings title above a regular line and a small italic note](../images/font-usage.png "The title, body and note styles of one family (drawn here with the demo Montserrat family in place of Inter).")

The family draws the face that matches the weight and italic flag, or its closest face (see [Choosing a face](how-fonts-work.md#choosing-a-face-with-fontfamily-resolve)).

## Step 4: Check the dev mode log

In dev mode, every face prints how it was read and how long it took:

```
[JOID] Font Inter Regular generated into the msdf cache (C:\Users\me\AppData\Local\joid\msdf\3f2a...e1.msdf) in 2841.37ms
[JOID] Font Inter Bold read from a .msdf file in 41.02ms
```

A face that is "generated" took the slow path: its atlas did not exist yet. The next launch reads it from the cache. To skip generation on every machine, ship pre-generated atlases (below).

## Shipping pre-generated .msdf atlases

Generate one atlas per face with the [MSDF Generator](msdf-generator.md), put the output directories in your resources, and load the `font.msdf` files with the same method:

```java
final MsdfFont inter = MsdfFontLoader.load(
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Regular/font.msdf"),
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Italic/font.msdf"),
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Bold/font.msdf")
).join();
```

A `.msdf` atlas is read directly, without the cache. Use it when the first launch must be fast, or when you need other characters or another atlas size than the defaults. An atlas written by another `.msdf` format version is refused: generate it again with the generator of your JOID version.

## Overriding a face with MsdfSource

A handle is read through an `IMsdfSource` (`dev.joid.lib.font.impl.msdf.dto.source`). Build the source yourself to declare a face with another weight or style than its file says, for a font with wrong metadata or a family assembled from unrelated files:

```java
MsdfFontLoader.load(
    regularStream,
    MsdfOpenTypeSource.of(boldStream).weight(FontWeight.BOLD),
    MsdfBinarySource.of(new File("fonts/Handwritten/font.msdf")).italic(true)
);
```

| Class | Description |
|---|---|
| `MsdfOpenTypeSource.of(Object handle)` | A `.ttf`, `.otf` or `.ttc`, through the MSDF cache. `getFile()` and `isGenerated()` tell which cache file was used and whether it was generated. `MsdfOpenTypeSource.supports(byte[] header)` tells whether a header is a font. |
| `MsdfBinarySource.of(Object handle)` | A `.msdf` atlas. |
| `MsdfSource` | Base of both: `weight(FontWeight)` and `italic(boolean)` override only what you set. |
| `IMsdfSource` | `read()` returns an `MsdfFontFace` (throws `IOException`); `describe()` is the text of the dev mode log line. Implement it to produce faces your own way (see [Custom Font Implementations](custom-fonts.md#producing-msdf-faces-with-imsdfsource)). |

## Building a family from faces with MsdfFont.create

`MsdfFont.create(MsdfFontFace... faces)` builds a family from faces you already hold, for example a subset of a loaded family:

```java
final MsdfFont lightBold = MsdfFont.create(
    Fonts.INTER.getFace(FontWeight.LIGHT, false),
    Fonts.INTER.getFace(FontWeight.BOLD, false)
);
```

![A sentence requested in the nine weights from a family with only Light and Bold](../images/font-resolve.png "The same Light + Bold family built from the demo Montserrat faces: requests up to 500 draw Light, from 600 Bold.")

`MsdfFontFace.style(FontWeight weight, boolean italic)` returns a copy of a face declared with another weight and style, sharing the same atlas, to add it to a family under another identity.

## The MSDF cache with MsdfFontCache

A font file has no distance field: the first time it loads, its atlas is generated with the defaults of the generator (characters 32 to 563, a 2048×2048 atlas, a range of 24 pixels, the largest em size that fits) and written to a cache shared by every JOID application of the user. The next loads read the cached file directly.

| System | Cache directory |
|---|---|
| Windows | `%LOCALAPPDATA%\joid\msdf` (`<user.home>\AppData\Local\joid\msdf` without the variable) |
| macOS | `~/Library/Caches/joid/msdf` |
| Linux and others | `$XDG_CACHE_HOME/joid/msdf`, or `~/.cache/joid/msdf` |

- A cached file is named after the SHA-256 of the font bytes, the JOID version, the `.msdf` format version and the generation settings. Another font, a font update or another JOID version gets its own file. Older files are never deleted.
- The file is written to a temporary file and moved in place, so several processes can load the same font at the same time.
- A cached file that cannot be read (corrupted, or written by an older format) is generated again once.
- To force a regeneration, delete the cached files: they are generated again on the next load.

| Method | Description |
|---|---|
| `MsdfFontCache.getDirectory()` | Current cache directory. |
| `MsdfFontCache.directory(File directory)` | Moves the cache for the next loads. |
| `MsdfFontCache.locate(byte[] font)` | Cache file of a font, without reading or writing anything. |
| `MsdfFontCache.resolve(byte[] font)` | Cache file of a font, generated first when missing. Throws `IOException`. |
| `MsdfFontCache.generate(byte[] font, File file)` | Generates the atlas of a font into any file, creating its directory. Throws `IOException`. |

```java
MsdfFontCache.directory(new File(JOID.inst().getConfigDir(), "fonts"));
```

Call `directory(...)` before loading the fonts that should use it.

## Troubleshooting

### Missing weight warnings

In dev mode, the first time a family resolves another weight than the one requested, while text is measured or drawn, it prints the fallback once per requested weight on `System.err`, with the stack trace of the code that created the text:

```
[JOID] The font weight 600 is not loaded in the family of Inter Bold, 700 is drawn instead (loaded: 400 Inter Regular, 700 Inter Bold)
	at com.example.ui.UIProfile.init(UIProfile.java:42)
	...
```

Load the missing weight, or change the weight of the `TextInfo` (or of the markup) at the line the trace points at. The trace points at the `TextElement` (or `Text.create`) call that created the run, recorded in dev mode. Text measured or drawn without a `TextElement` points at the live call stack. When you write your own text drawing, keep the warnings pointing at your code with `FontUsage` (see [Custom Font Implementations](custom-fonts.md#font-warnings-with-fontusage)).

### Load errors

The future completes exceptionally instead of throwing: `join()` throws a `CompletionException` whose cause is the problem.

| Cause | Situation |
|---|---|
| `IllegalArgumentException` "A msdf font needs at least one face" | `load()` without handle. |
| `IllegalArgumentException` "Two faces share the weight 700 italic" | Two faces with the same weight and the same style: [override](#overriding-a-face-with-msdfsource) one of them. |
| `IllegalArgumentException` "No asset locator found for input of type ..." | A handle no locator supports. |
| `IOException` "Not a JOID msdf font" | Content that is neither a font nor a `.msdf` atlas. |
| `IOException` "Unsupported msdf font version ..." | A `.msdf` atlas written by another version of the generator: generate it again. |
| `IOException` "Unable to generate the msdf atlas of the font in ..." | A font the generator cannot read. |
| `IOException` "Unable to create the msdf cache ..." | A cache directory that cannot be created. |

```java
MsdfFontLoader.load(new File("fonts/Inter-Regular.ttf")).whenComplete((font, error) -> {
    if (error != null) {
        error.printStackTrace();
        return;
    }
    this.font = font;
});
```

### Other symptoms

| Symptom | Cause and fix |
|---|---|
| Some characters do not show and take no room | The atlas does not hold them. Generate an atlas with a charset that includes them (see [Charsets](msdf-generator.md#charsets)). |
| Italic text looks slanted rather than cursive | The family has no italic face, so the upright face is sheared. Load the italic file. |
| The first launch is slow | The atlases are generated (see the dev mode log). Ship [pre-generated atlases](#shipping-pre-generated-msdf-atlases). |
| `IllegalStateException` "The msdf font shader is not usable" when drawing | The MSDF font shader did not compile on the backend. |

## MsdfFont and MsdfFontFace reference

| `MsdfFont` method | Description |
|---|---|
| `MsdfFont.create(MsdfFontFace... faces)` | New family; throws `IllegalArgumentException` when empty or when two faces share a weight and a style. |
| `getFace(FontWeight weight, boolean italic)` | The face the family draws for this request. |
| `getFamily()` | The `FontFamily`: `getFaces()` lists the faces sorted by weight; `resolve(weight, italic)` is what `getFace` calls. |
| `getFontProvider()` | The shared `MsdfFontProvider` that draws and measures the text. |

| `MsdfFontFace` method | Description |
|---|---|
| `getName()`, `getWeight()`, `isItalic()` | Identity of the face. |
| `hasGlyph(int codepoint)`, `getGlyph(int codepoint)` | Whether the atlas holds a character, and its `MsdfGlyph` (`getAdvance()`, `getPlaneBounds()`, `getAtlasBounds()`), `null` when absent. |
| `getAdvance(int codepoint)` | Advance as a fraction of the em, `0F` for a missing character. |
| `getKerning(int previous, int current)` | Kerning of the pair as a fraction of the em, `0F` without pair. |
| `getAscender()`, `getDescender()`, `getLineHeight()`, `getUnderlineY()`, `getUnderlineThickness()` | Metrics as fractions of the em (see `getMetrics()`). |
| `getXHeight()` | Height of the `x` glyph, `0F` without `x`. |
| `getAtlas()` | `MsdfAtlas`: `getWidth()`, `getHeight()`, `getSize()` (em size in atlas pixels), `getDistanceRange()`. |
| `getTexture()` | The atlas `Resource`. |
| `getGlyphs()`, `getKerningPairs()` | The raw maps of the face. |
| `style(FontWeight weight, boolean italic)` | Copy of the face declared with another weight and style, sharing the same atlas. |
| `MsdfFontFace.create(...)`, `MsdfFontFace.pair(int previous, int current)` | Build a face from its parts and key a kerning pair (see [Custom Font Implementations](custom-fonts.md#producing-msdf-faces-with-imsdfsource)). |

## See also

- [How Fonts Work](how-fonts-work.md)
- [MSDF Generator](msdf-generator.md)
- [Custom Font Implementations](custom-fonts.md)
- [Text and TextInfo](../text/text-and-textinfo.md)
- [Assets](../resources/assets.md)