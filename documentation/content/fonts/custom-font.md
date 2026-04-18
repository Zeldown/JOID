# Custom Fonts

JOID renders text via MSDF (Multi-channel Signed Distance Field) atlases — crisp at any scale. Use your own fonts by generating an atlas and loading it at startup.

## Load a font

```java
final Font myFont = FontLoader.load(getClass().getResourceAsStream("/fonts/Inter-Regular/font.png"),
                                     getClass().getResourceAsStream("/fonts/Inter-Regular/font.json"));
```

Each font needs two files:

- **`font.png`** — the MSDF atlas (2048×2048 typical).
- **`font.json`** — glyph metadata (metrics, atlas coords).

Both are produced by `msdf-atlas-gen` (see [MSDF Atlas](msdf-atlas.md)).

## Bundled fonts

JOID ships with Montserrat for dev use:

```java
DemoFont.MONTSERRAT          // regular weight only, pre-loaded
```

Use it during development, then swap for your own fonts in production. `DemoFont` is only available when JOID loaded with `setDemoMode(true)`.

## Create a `TextInfo`

```java
TextInfo info = TextInfo.create(myFont, 24, Color.WHITE);
```

`TextInfo` combines font, size, color, and optional flags:

```java
TextInfo.create(myFont, 24, Color.WHITE)
    .bold(true)
    .italic(true)
    .shadow(true)
    .shadowColor(Color.BLACK)
    .shadowOffset(1D, 1D);
```

## Use in nodes

```java
TextNode.create(0, 0)
    .text(Text.create("Hello", info))
    .attach(parent);
```

Or via `DrawUtils.TEXT` for ad-hoc drawing:

```java
DrawUtils.TEXT.drawText(x, y, Text.create("Hello", info));
```

## Font weights and styles

Load each variant separately — Inter-Bold.png, Inter-Italic.png, Inter-BoldItalic.png — and swap `TextInfo` per style:

```java
Font regular = FontLoader.load(...);
Font bold = FontLoader.load(...);

TextInfo regularInfo = TextInfo.create(regular, 16, Color.WHITE);
TextInfo boldInfo = TextInfo.create(bold, 16, Color.WHITE);

Text.create("Normal ", regularInfo).append("Bold", boldInfo);
```

## Async loading

`FontLoader` reads synchronously. Load fonts once at application startup, before opening any UI that uses them.

## Character set

An MSDF atlas only contains the characters you generated for. Missing glyphs render as a placeholder box. Generate atlases with full Latin + symbols for general-purpose fonts.

## Best practices

- **Load once, at startup.** Fonts are reused across UIs.
- **Cache `TextInfo`.** Reused per-node.
- **Pre-size your atlas.** 2048×2048 fits ~500 glyphs at 48px — plenty for most alphabets. For CJK, use 4096×4096 or multiple atlases.

## See also

- [MSDF Atlas](msdf-atlas.md) — generating your atlas.
- [TextNode](../nodes/design/text.md) — rendering text.
