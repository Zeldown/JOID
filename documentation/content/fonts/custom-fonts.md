# Custom Font Implementations

When the MSDF fonts do not fit, for a pixel-art font drawn from sprites or a text engine of your own, you write your own font. You can build on the glyph framework of JOID and only draw glyphs, or implement the two font interfaces from scratch. Read [How Fonts Work](how-fonts-work.md) first for the concepts.

## Choosing the level

| Level | You write | You get for free |
|---|---|---|
| Glyph font: extend `GlyphFont` and `GlyphFontProvider` (`dev.joid.lib.font.impl.glyph`) | A face (`IFontFace`), the font class and how one glyph is drawn. | Families and weight resolution, slanted italic, advances, kerning, letter spacing, line height, markup, effects, shadows, measuring. |
| Raw font: implement `IFont` and `IFontProvider` (`dev.joid.lib.font`) | Layout, drawing and measuring of a whole line. | Nothing beyond being usable everywhere a `TextInfo` is. |
| MSDF faces from another source: implement `IMsdfSource` | Building an `MsdfFontFace` from your data. | Everything the MSDF fonts do. |

## A bitmap font on GlyphFont

This example draws each character from a sprite `Resource` (see [Resources](../resources/resources.md)). It has three classes: the face, the provider and the font.

### The face: IFontFace

`IFontFace` (`dev.joid.lib.font.impl.glyph.dto`) describes one weight and style. Every metric is a fraction of the em, measured upward from the baseline: the descender and an underline below the baseline are negative.

```java
public final class BitmapFontFace implements IFontFace {

    private final String name;
    private final FontWeight weight;
    private final Map<Integer, Resource> sprites;
    private final Map<Integer, Float> advances;

    public BitmapFontFace(final String name, final FontWeight weight, final Map<Integer, Resource> sprites, final Map<Integer, Float> advances) {
        this.name = name;
        this.weight = weight;
        this.sprites = sprites;
        this.advances = advances;
    }

    public Resource getSprite(final int codepoint) {
        return this.sprites.get(codepoint);
    }

    @Override
    public boolean isItalic() {
        return false;
    }

    @Override
    public float getAscender() {
        return 0.8F;
    }

    @Override
    public float getDescender() {
        return -0.2F;
    }

    @Override
    public float getLineHeight() {
        return 1.2F;
    }

    @Override
    public float getUnderlineY() {
        return -0.1F;
    }

    @Override
    public float getUnderlineThickness() {
        return 0.05F;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public FontWeight getWeight() {
        return this.weight;
    }

    @Override
    public float getAdvance(final int codepoint) {
        final Float advance = this.advances.get(codepoint);
        return advance == null ? 0F : advance;
    }

    @Override
    public float getKerning(final int previous, final int current) {
        return 0F;
    }

    @Override
    public boolean hasGlyph(final int codepoint) {
        return this.advances.containsKey(codepoint);
    }

}
```

`hasGlyph` decides which characters exist: a character for which it returns `false` is skipped, neither drawn nor measured. That is why it reads the advances and not the sprites here: the space has an advance but no sprite.

### The provider: GlyphFontProvider

`GlyphFontProvider<F>` implements the whole `IFontProvider`. You implement three methods:

| Method | Called |
|---|---|
| `begin(double runX, double runY, double runWidth, double runHeight)` | Before the glyphs of a pass, with the bounds of the whole line (a gradient spans them). |
| `drawGlyph(TextGlyph<F> glyph)` | For every glyph of the pass. |
| `end()` | After the glyphs of a pass. |

For each line, the provider runs the effects `apply` and `background` hooks, then one pass for the shadow (when the `TextInfo` has a shadow color) and one pass for the text: `begin`, `drawGlyph` for every glyph, `end`. Effects `decorate` the glyphs after each pass.

```java
public final class BitmapFontProvider extends GlyphFontProvider<BitmapFontFace> {

    @Override
    protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
    }

    @Override
    protected void drawGlyph(final TextGlyph<BitmapFontFace> glyph) {
        final Resource sprite = glyph.getFace().getSprite(glyph.getCodepoint());
        if (sprite == null) {
            return;
        }

        final double x = glyph.getX() + glyph.getOffsetX();
        final double top = glyph.getBaseline() + glyph.getOffsetY() - glyph.getAscender();
        final double width = glyph.getAdvance(glyph.getCodepoint());
        final double height = glyph.getAscender() - glyph.getDescender();
        final Vector4f canvas = new Vector4f((float) x, (float) top, (float) (x + width), (float) (top + height));
        glyph.getColor().bind(() -> DrawUtils.RESOURCE.drawResource(x, top, width, height, sprite), canvas, true);
    }

    @Override
    protected void end() {
    }

}
```

- Use `getCodepoint()`, not the placement character: an effect can replace the character (`codepoint(...)`), and the offset (`getOffsetX()`, `getOffsetY()`) and color (`getColor()`) can be changed by effects too.
- `getColor()` is the shadow color in the shadow pass.
- `isSlanted()` is `true` when italic is requested and the face drawn is upright: shear the glyph (the MSDF provider shears by 0.2 of the height above the baseline) or ignore it.
- `Vector4f` is `javax.vecmath.Vector4f`; `Color.bind(Runnable, Vector4f, boolean)` draws the runnable with the color, a gradient spanning the canvas.

### The font: GlyphFont

```java
public final class BitmapFont extends GlyphFont<BitmapFontFace> {

    private static final BitmapFontProvider PROVIDER = new BitmapFontProvider();

    public BitmapFont(final BitmapFontFace... faces) {
        super(FontFamily.of(faces));
    }

    @Override
    public IFontProvider getFontProvider() {
        return BitmapFont.PROVIDER;
    }

}
```

```java
final BitmapFont pixel = new BitmapFont(regular, bold);
TextNode.create(0, 0).text(Text.create("Game over", TextInfo.create(pixel, FontWeight.BOLD, 24, Color.WHITE))).attach(flex);
```

![Game over drawn in a bold pixel font, and below it in the regular face](../images/custom-font-bitmap.png "The BitmapFont of this page with 5 × 7 pixel sprites: the snippet draws the bold line; the regular line shows the second face of the family (2× scale).")

The text resolves its faces, kerning, markup and effects like an MSDF text. `GlyphFontProvider` reads the faces from `TextInfo.getFont()`, cast to `GlyphFont<F>`: return your provider only from a `GlyphFont` of the same face type.

### Glyph framework reference

| Type | Role |
|---|---|
| `IFontFace` (`impl.glyph.dto`) | One face: `getName()`, `getWeight()`, `isItalic()`, `hasGlyph(codepoint)`, `getAdvance(codepoint)`, `getKerning(previous, current)` and the metrics `getAscender()`, `getDescender()`, `getLineHeight()`, `getUnderlineY()`, `getUnderlineThickness()`, all as fractions of the em measured upward from the baseline. |
| `FontFamily<F>` | `FontFamily.of(F... faces)` sorts the faces by weight and refuses an empty family or two faces of the same weight and style; `resolve(weight, italic)` picks the closest face; `getFaces()` lists them. |
| `GlyphFont<F>` | Base of the font: `protected` constructor taking the `FontFamily`, `getFace(weight, italic)`, `getFamily()`. |
| `GlyphFontProvider<F>` | Base of the provider: implements the whole `IFontProvider`. You implement `begin(runX, runY, runWidth, runHeight)`, `drawGlyph(TextGlyph<F> glyph)` and `end()`. `layout(text, info)` returns the `GlyphLayout<F>` the provider measures and draws: `getWidth()` and `getPlacements()`, one `GlyphPlacement<F>` per glyph with `getIndex()`, `getCodepoint()`, `getFace()`, `getX()` and `getStyle()`. |
| `TextGlyph<F>` | The glyph handed to `drawGlyph`: the `ITextGlyph` values (see [ITextGlyph reference](../text/markup-and-effects.md#itextglyph-reference)) plus `getFace()` and `isSlanted()` (italic requested on an upright face). |

## A raw font on IFont and IFontProvider

| Interface | Method | Contract |
|---|---|---|
| `IFont` | `getFontProvider()` | The provider that draws this font. |
| `IFontProvider` | `drawText(double x, double y, String text, TextInfo info)` | Draws one line with its top-left corner at `x`, `y` and returns its `FontBounds`. The returned width places the next run of the `Text`. |
| | `drawText(x, y, text, info, double runX, double runY, double runWidth, double runHeight)` | Same, with the bounds of the whole line made of every run (a gradient spans them). Calls the first one by default. |
| | `getLineHeight(TextInfo info)` | Line height of the info. |
| | `getWidth(String text, TextInfo info)` | Width of the line; must match what `drawText` draws. |
| | `getHeight(String text, TextInfo info)` | Height of the line. |

`TextInfo.getWidth`, `getHeight` and every measure of `Text` call these methods. With a raw provider, markup, effects, shadows, letter spacing and weight resolution are yours to implement.

## Producing MSDF faces with IMsdfSource

To keep the MSDF rendering but produce the atlas your own way (another generator, a packed archive, a network service), implement `IMsdfSource` (`dev.joid.lib.font.impl.msdf.dto.source`) and pass it to `MsdfFontLoader.load(...)`:

| Method | Description |
|---|---|
| `read()` | Returns the `MsdfFontFace`; throws `IOException`. Runs on the loader threads. |
| `describe()` | Text of the dev mode log line, `"read from <class name>"` by default. |

Extend `MsdfSource` instead to get the `weight(...)` and `italic(...)` overrides: implement its `protected MsdfFontFace parse()`.

Build the face with `MsdfFontFace.create(MsdfAtlas atlas, MsdfMetrics metrics, Map<Integer, MsdfGlyph> glyphs, Map<Long, Float> kerningPairs, BufferedImage image, String name, FontWeight weight, boolean italic)` (throws `IllegalArgumentException` for an empty name):

| Part | Constructor | Units |
|---|---|---|
| `MsdfAtlas` | `new MsdfAtlas(float distanceRange, float size, int width, int height)` | Range and em size in atlas pixels, atlas size in pixels. |
| `MsdfMetrics` | `new MsdfMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness)` | Fractions of the em. |
| `MsdfGlyph` | `new MsdfGlyph(int codepoint, float advance, MsdfBounds planeBounds, MsdfBounds atlasBounds)` | Advance in em; both bounds `null` for a glyph without outline, like the space. |
| `MsdfBounds` | `new MsdfBounds(float left, float bottom, float right, float top)` | Plane bounds in em from the pen on the baseline, Y up; atlas bounds in pixels, Y from the bottom row. |
| Kerning map | Keys from `MsdfFontFace.pair(int previous, int current)` | Values in fractions of the em. |
| `image` | The RGB distance field atlas. | Uploaded as a texture the first time the face draws. |

## Font warnings with FontUsage

The missing weight warnings point at the code that created the text (see [Missing weight warnings](adding-fonts.md#missing-weight-warnings)). If you write your own text drawing on top of `TextElement`s, wrap the font calls in `FontUsage.trace` (`dev.joid.lib.font`) so the warnings keep pointing at the code that created the run:

```java
final double width = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(element.getText()));
```

| Method | Description |
|---|---|
| `FontUsage.trace(StackTraceElement[] origin, DoubleSupplier usage)` | Runs the usage with this origin and returns its result; a `null` origin (outside dev mode) runs it as is. |
| `FontUsage.getOrigin()` | Origin of the usage in progress, `null` outside `trace` or for a `null` origin. |

## See also

- [How Fonts Work](how-fonts-work.md)
- [Adding Your Own Fonts](adding-fonts.md)
- [Markup and Text Effects](../text/markup-and-effects.md)
- [Drawing Text](../drawing/text.md)
- [Resources](../resources/resources.md)