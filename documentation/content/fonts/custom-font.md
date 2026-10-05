# Custom Fonts

JOID renders text through MSDF (Multi-channel Signed Distance Field) atlases — crisp at any scale. Load a font family with `MsdfFontLoader`, build a `TextInfo`, pick a weight, and feed it into a `Text` / `TextNode`.

## `MsdfFontLoader.load`

`MsdfFontLoader` is asynchronous — it reads every face of the family in parallel on an executor pool and returns a `CompletableFuture` carrying the finished `MsdfFont`.

```java
static CompletableFuture<MsdfFont> load(Object... faces)
```

Each handle names one face: a `font.msdf` file, produced by the generator described in [MSDF Atlas](msdf-atlas.md), or the font file itself — see [Font files](#font-files). A `font.msdf` carries the atlas, the glyph metrics, the kerning table, and the weight and style of the face. Anything an [asset locator](../resources/assets.md) recognises works — an `InputStream`, a `File`, a URL, or a handle of your own.

Minimal load:

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"))
    .thenAccept(font -> this.inter = font);
```

A whole family, in any order — one handle per weight and style:

```java
MsdfFontLoader.load(
    getClass().getResourceAsStream("/fonts/Inter-Light/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Italic/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Bold/font.msdf")
).thenAccept(font -> this.inter = font);
```

One font, one registration: every `TextInfo` built on it chooses its weight. Two faces with the same weight and style, an empty list or an unreadable file complete the future exceptionally with a message naming the problem.

### Font files

A `.ttf`, `.otf` or `.ttc` handle loads like a `font.msdf`: the loader recognises it by its header, generates its atlas with the defaults of the generator and caches it.

```java
MsdfFontLoader.load(new File("fonts/Inter-Regular.ttf"), new File("fonts/Inter-Bold.ttf"))
    .thenAccept(font -> this.inter = font);
```

The first load of a font takes a few seconds on the loader pool. `MsdfFontCache` then keeps the atlas under the SHA-256 of the font file and of the JOID version: the next launches read it like any `font.msdf`, every JOID application of the machine shares it, and an update of JOID generates it again.

| System | Cache |
|---|---|
| Windows | `%LOCALAPPDATA%\joid\msdf` |
| macOS | `~/Library/Caches/joid/msdf` |
| Linux | `$XDG_CACHE_HOME/joid/msdf`, or `~/.cache/joid/msdf` |

`MsdfFontCache.directory(File)` moves the cache, before the first load. Ship `font.msdf` files instead when the first launch must be instant, or when the atlas needs another charset, range or size.

### Sources

A handle is turned into an `MsdfBinarySource`, or an `MsdfOpenTypeSource` for a font file. Build the source yourself when a face needs to be presented differently from what its file declares — a font whose metadata lies, or a family assembled from unrelated files:

```java
MsdfFontLoader.load(
    regularHandle,
    MsdfBinarySource.of(boldHandle).weight(FontWeight.BOLD),
    MsdfBinarySource.of(slantedHandle).italic(true)
);
```

The `font.json` + `font.png` pair of older atlases still loads through an `MsdfJsonSource`. Those files declare no weight, so they start as regular and upright — set the style on the source:

```java
MsdfFontLoader.load(regularHandle, MsdfJsonSource.of(boldJson, boldPng).weight(FontWeight.BOLD));
```

All of them are `IMsdfSource`s, and so is anything you write yourself: the loader reads whatever source it is handed.

### Kerning

Kerning pairs travel inside the `font.msdf` file and apply on their own, both when text is drawn and when it is measured — `TextInfo.getWidth`, line wrapping and text field cursors all account for them. Nothing to enable.

## Weights and styles

Like a CSS `font-family` assembled from `@font-face` rules, one `MsdfFont` holds every face of a typeface, and each text picks its weight. `FontWeight` names the nine weights, from `THIN` (100) to `BLACK` (900):

```java
final TextInfo body = TextInfo.create(InternalFont.MONTSERRAT, 16, Color.WHITE);
final TextInfo title = TextInfo.create(InternalFont.MONTSERRAT, FontWeight.BOLD, 28, Color.WHITE);
final TextInfo quote = body.copy().weight(FontWeight.LIGHT).italic(true);
```

When the family has no face of the requested weight, the face with the closest weight is drawn. Two faces at the same distance are separated the CSS way: 400 takes the heavier one, 500 and below take the lighter one, above 500 takes the heavier one. With Light (300) and Bold (700) loaded:

| Requested | 100 | 200 | 300 | 400 | 500 | 600 | 700 | 800 | 900 |
|---|---|---|---|---|---|---|---|---|---|
| Drawn | 300 | 300 | 300 | 300 | 300 | 700 | 700 | 700 | 700 |

In dev mode (`JOID.inst().setDevMode(true)`) the first fallback of each weight is reported once on the console, with the exact font and the calls that asked for the weight:

```
[JOID] The font weight 600 is not loaded in the family of Inter Bold, 700 is drawn instead (loaded: 300 Inter Light, 700 Inter Bold)
	at dev.joid.lib.font.impl.glyph.GlyphFont.getFace(GlyphFont.java:20)
	at dev.joid.lib.font.impl.glyph.GlyphFontProvider.layout(GlyphFontProvider.java:64)
	...
	at com.example.ui.UIProfile.init(UIProfile.java:42)
```

A face read from a `font.msdf` written by an older generator carries no name, and the message then says `this font family`.

Italic prefers the italic faces of the family. A family without one keeps its upright faces and slants them, so `italic(true)` always shows.

`FontWeight.of(int)` turns any numeric weight into the closest named one: `FontWeight.of(600)` is `SEMI_BOLD`, `FontWeight.of(650)` is `BOLD`.

## Bundled fonts

`InternalFont.MONTSERRAT` holds the nine Montserrat weights the dev mode draws with, and loads when JOID starts in dev or demo mode. `DemoFont.MONTSERRAT` points to the same family once JOID runs with `setDemoMode(true)`, next to `DemoFont.PACIFICO` and `DemoFont.SPACE_GROTESK`. They ship as `.ttf` files under `assets/dev/fonts` and `assets/demo/fonts`, with their SIL Open Font License, and only in the `-dev` artifacts. Great for bootstrapping the quick-start and demo UIs — ship your own fonts for production.

## Build a `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)
TextInfo.create(IFont font, FontWeight weight, float fontSize)
TextInfo.create(IFont font, FontWeight weight, float fontSize, Color color)
```

Setters (all chainable, return the same `TextInfo` instance):

```java
T font(IFont font)
T fontSize(float fontSize)
T weight(FontWeight weight)         // REGULAR by default
T letterSpacing(float letterSpacing) // fraction of the font size, -0.02F = -2 % in Figma
T lineHeight(float lineHeight)      // fraction of the font size, 1.5F = 150 %, 0 keeps the font's own (Auto)
T color(Color color)
T colored(boolean colored)          // false ignores the colors set by markup
T italic(boolean italic)
T markups(ITextMarkup... markups)   // replaces the registered markups, none disables them
T effects(ITextEffect... effects)   // effects applied to the whole text
T shadow()                          // shadowColor = this.color.darker(0.3F)
T shadow(Color color)               // explicit shadow color
T shadow(float x, float y)          // shadow offset in logical units
T copy()
```

```java
final TextInfo info = TextInfo.create(customFont, 24, Color.WHITE)
    .weight(FontWeight.SEMI_BOLD)
    .italic(true)
    .shadow(Color.BLACK)
    .shadow(1F, 1F);
```

Markup and effects let a single string switch weight, color or decoration halfway through — see [Markup & Effects](markup-effects.md).

## Use in nodes

```java
TextNode.create(0, 0)
    .text(Text.create("Hello", info))
    .attach(parent);
```

Or via `DrawUtils.TEXT` for ad-hoc drawing:

```java
DrawUtils.TEXT.drawText(x, y, "Hello", info, Align.START, Align.START);
```

## Asynchronous loading

`MsdfFontLoader.load(...)` returns immediately. The future completes on the fixed-size executor pool once every face is read, so you choose how to wait:

```java
MsdfFontLoader.load(regular, bold).thenAccept(font -> this.font = font);         // continue when ready
MsdfFontLoader.load(regular, bold).exceptionally(error -> { error.printStackTrace(); return null; });
this.font = MsdfFontLoader.load(regular, bold).join();                           // block, at startup
```

A font that cannot be read completes the future exceptionally rather than failing silently, so never drop the returned future on the floor. Kick the load off at application start so the font is ready before the UI that uses it opens.

## Character set

An MSDF atlas only contains the characters declared in the `charset.txt` at generation time. Characters missing from the atlas are skipped. Generate atlases with the full Latin range + symbols for general-purpose fonts — see [MSDF Atlas](msdf-atlas.md).

## Best practices

- **Load each family once, at startup.** It is reused across every UI.
- **Cache `TextInfo`.** One per logical style (heading, body, code) — reuse per node.
- **Ship the weights you use.** Each face is one atlas; the family resolves the others to the closest one.

## Other font implementations

MSDF is one implementation of the font contract, not the contract itself. Everything that draws or measures text — `TextInfo`, `TextNode`, `DrawUtils` — only knows two interfaces from `dev.joid.lib.font`:

| Interface | Role |
|---|---|
| `IFont` | What a `TextInfo` holds. Hands out its provider. |
| `IFontProvider` | Draws and measures a string for a `TextInfo`. |

A font made of glyph images gets the rest for free from `dev.joid.lib.font.impl.glyph`: describe one face with `IFontFace`, extend `GlyphFont` and `GlyphFontProvider`, and draw a single glyph in `drawGlyph`. Families, weight resolution, kerning, letter spacing, markup, effects and shadows come from the base classes. The MSDF classes in `dev.joid.lib.font.impl.msdf` are built exactly that way.

## See also

- [Markup & Effects](markup-effects.md) — styling a string from the inside.
- [MSDF Atlas](msdf-atlas.md) — generating the atlas files.
- [TextNode](../nodes/design/text.md) — rendering text.