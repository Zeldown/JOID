# Custom Fonts

JOID renders text through MSDF (Multi-channel Signed Distance Field) atlases — crisp at any scale. Load your own fonts with `MsdfFontLoader`, build a `TextInfo`, and feed it into a `Text` / `TextNode`.

## `MsdfFontLoader.load`

`MsdfFontLoader` is asynchronous — it off-loads parsing and texture upload to an executor pool and returns a `CompletableFuture` carrying the finished `MsdfFont`.

```java
static CompletableFuture<MsdfFont> load(Object packed)
static CompletableFuture<MsdfFont> load(Object regular, Object bold)
```

Each handle names one `font.msdf` file, produced by the generator described in [MSDF Atlas](msdf-atlas.md). It carries the atlas, the glyph metrics and the kerning table together. Anything an [asset locator](../resources/assets.md) recognises works — an `InputStream`, a `File`, a URL, or a handle of your own.

Minimal load:

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/fonts/Inter/font.msdf"))
    .thenAccept(font -> this.interFont = font);
```

Both regular and bold atlases:

```java
MsdfFontLoader.load(
    getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Bold/font.msdf")
).thenAccept(font -> {
    // font.getRegular() and font.getBold() are the two MsdfFace weights
});
```

With a single handle, the same face serves as both regular and bold.

The `font.json` + `font.png` pair of older atlases still loads. Wrap both files in an `MsdfJsonSource` and pass it wherever a handle is expected — even next to a packed atlas:

```java
MsdfFontLoader.load(MsdfJsonSource.of(json, png));
MsdfFontLoader.load(regularHandle, MsdfJsonSource.of(boldJson, boldPng));
```

Both are `IMsdfSource`s, and so is anything you write yourself: the loader reads whatever source it is handed.

### Kerning

Kerning pairs travel inside the `font.msdf` file and apply on their own, both when text is drawn and when it is measured — `TextInfo.getWidth`, line wrapping and text field cursors all account for them. Nothing to enable.

## Bundled `DemoFont.MONTSERRAT`

`DemoFont.MONTSERRAT` is loaded automatically when JOID is started with `setDemoMode(true)`. Great for bootstrapping the quick-start and demo UIs — ship your own atlas for production.

## Build a `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)
```

Setters (all chainable, return the same `TextInfo` instance):

```java
T font(IFont font)
T fontSize(float fontSize)
T letterSpacing(float letterSpacing)
T lineHeight(float lineHeight)
T color(Color color)
T colored(boolean colored)
T italic(boolean italic)
T shadow()                          // shadowColor = this.color.darker(0.3F)
T shadow(Color color)               // explicit shadow color
T shadow(float x, float y)          // shadow offset in logical units
T copy()
```

There is **no** `bold(boolean)` setter, `shadowColor(Color)`, or `shadowOffset(double, double)` — bold is achieved by swapping the `IFont` (load a bold atlas and use `customFont.getBold()`); shadow is configured through `shadow(...)` overloads.

```java
final TextInfo info = TextInfo.create(customFont.getRegular(), 24, Color.WHITE)
    .italic(true)
    .shadow(Color.BLACK)
    .shadow(1F, 1F);
```

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

## Bold variants

`MsdfFont` holds two faces — `regular` and `bold`. A `TextInfo` is built from the font itself, and the `§l` style code switches to the bold face inside the text:

```java
final TextInfo info = TextInfo.create(font, 16, Color.WHITE);

Text.create("Normal §lBold", info);
```

With a single atlas both faces are the same, so `§l` has no visible effect.

## Asynchronous loading

`MsdfFontLoader.load(...)` returns immediately. The future completes on the fixed-size executor pool once parsing and upload finish, so you choose how to wait:

```java
MsdfFontLoader.load(stream).thenAccept(font -> this.font = font);                 // continue when ready
MsdfFontLoader.load(stream).exceptionally(error -> { error.printStackTrace(); return null; });
this.font = MsdfFontLoader.load(stream).join();                                   // block, at startup
CompletableFuture.allOf(regular, bold, italic).join();                        // wait for a whole family
```

A font that cannot be read completes the future exceptionally rather than failing silently, so never drop the returned future on the floor. Kick the load off at application start so the font is ready before the UI that uses it opens.

## Character set

An MSDF atlas only contains the characters declared in the `charset.txt` at generation time. Missing glyphs render as a placeholder. Generate atlases with the full Latin range + symbols for general-purpose fonts — see [MSDF Atlas](msdf-atlas.md).

## Best practices

- **Load fonts once, at startup.** They're reused across every UI.
- **Cache `TextInfo`.** One per logical style (heading, body, code) — reuse per node.
- **Pre-size your atlas.** 2048×2048 fits ~500 glyphs at 48px. For CJK, use 4096×4096 or split by script.

## Other font implementations

MSDF is one implementation of the font contract, not the contract itself. Everything that draws or measures text — `TextInfo`, `TextNode`, `DrawUtils` — only knows two interfaces from `be.zeldown.joid.lib.font`:

| Interface | Role |
|---|---|
| `IFont` | What a `TextInfo` holds. Hands out its provider. |
| `IFontProvider` | Draws and measures a string for a `TextInfo`. |

The MSDF classes live apart, in `be.zeldown.joid.lib.font.impl.msdf`. A backend with its own text rendering — a game's bitmap font, a platform text API — implements those two interfaces in its own `impl/<name>` package, and every text node works with it unchanged.

## See also

- [MSDF Atlas](msdf-atlas.md) — generating the atlas files.
- [TextNode](../nodes/design/text.md) — rendering text.