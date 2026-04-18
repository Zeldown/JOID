# Custom Fonts

JOID renders text through MSDF (Multi-channel Signed Distance Field) atlases — crisp at any scale. Load your own fonts with `FontLoader`, build a `TextInfo`, and feed it into a `Text` / `TextNode`.

## `FontLoader.load`

`FontLoader` is asynchronous — it off-loads JSON parsing and texture upload to an executor pool and hands the finished `CustomFont` to a callback.

```java
static void load(FontInputStream regular, Consumer<CustomFont> callback)
static void load(FontInputStream regular, FontInputStream bold, Consumer<CustomFont> callback)
```

A `FontInputStream` pairs the JSON metadata with the PNG atlas:

```java
public FontInputStream(InputStream data, InputStream texture)            // data = .json, texture = .png
```

Minimal load:

```java
FontLoader.load(
    new FontInputStream(
        getClass().getResourceAsStream("/fonts/Inter/font.json"),
        getClass().getResourceAsStream("/fonts/Inter/font.png")
    ),
    customFont -> this.interFont = customFont
);
```

Both regular and bold atlases:

```java
FontLoader.load(
    new FontInputStream(regularJson, regularPng),
    new FontInputStream(boldJson, boldPng),
    customFont -> {
        // customFont.getRegular() and customFont.getBold() are Font instances
    }
);
```

If the single-argument overload is used, the same font is stored as both regular and bold on the `CustomFont` wrapper.

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

`CustomFont` owns two `Font` instances — `regular` and `bold`. Create two `TextInfo` objects, one per weight:

```java
final TextInfo regularInfo = TextInfo.create(customFont.getRegular(), 16, Color.WHITE);
final TextInfo boldInfo    = TextInfo.create(customFont.getBold(),    16, Color.WHITE);

Text.create()
    .add(TextElement.create("Normal ", regularInfo))
    .add(TextElement.create("Bold",    boldInfo));
```

If you only have a single atlas, `CustomFont` falls back to the regular font for both.

## Asynchronous loading

`FontLoader.load(...)` returns immediately; the callback fires on the fixed-size executor pool once parsing and upload finish. Kick the load off at application start so the font is ready before the UI that uses it opens.

## Character set

An MSDF atlas only contains the characters declared in the `charset.txt` at generation time. Missing glyphs render as a placeholder. Generate atlases with the full Latin range + symbols for general-purpose fonts — see [MSDF Atlas](msdf-atlas.md).

## Best practices

- **Load fonts once, at startup.** They're reused across every UI.
- **Cache `TextInfo`.** One per logical style (heading, body, code) — reuse per node.
- **Pre-size your atlas.** 2048×2048 fits ~500 glyphs at 48px. For CJK, use 4096×4096 or split by script.

## See also

- [MSDF Atlas](msdf-atlas.md) — generating the atlas files.
- [TextNode](../nodes/design/text.md) — rendering text.