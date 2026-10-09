# Custom Font Implementations

When the MSDF fonts do not fit, for a pixel-art font drawn from sprites or a text engine of your own, you write your own font. Build on the glyph framework of JOID and only draw glyphs, implement the two font interfaces from scratch, or feed MSDF faces from your own source. This page closes the Fonts section and builds on the concepts of [How Fonts Work](how-fonts-work.md).

```java
final BitmapFont pixel = BitmapFont.create(regular, bold);
TextNode.create(100, 100).text(Text.create("Game over", TextInfo.create(pixel, FontWeight.BOLD, 24F, Color.WHITE))).attach(this);
TextNode.create(100, 140).text(Text.create("Game over", TextInfo.create(pixel, 24F, Color.WHITE))).attach(this);
```

![Game over drawn in a bold pixel font, and below it in the regular face](../images/custom-font-bitmap.png "The BitmapFont of this page with 5 × 7 pixel sprites: one family, its bold and regular faces (2× scale).")

`BitmapFont` is the three classes of the next section; `regular` and `bold` are two `BitmapFontFace` built from your sprites.

## Choosing the level

| Level | You write | You get |
|---|---|---|
| Glyph font: extend `GlyphFont` and `GlyphFontProvider` (`dev.joid.lib.font.impl.glyph`) | A face (`IFontFace`), the font class and how one glyph is drawn. | Families and weight resolution, slanted italic, advances, kerning, letter spacing, line height, markup, effects, shadows, measuring. |
| Raw font: implement `IFont` and `IFontProvider` (`dev.joid.lib.font`) | Layout, drawing and measuring of a whole line. | Use everywhere a `TextInfo` goes. |
| MSDF faces from another source: implement `IMsdfSource` | Building an `MsdfFontFace` from your data. | Everything the MSDF fonts do. |

The glyph framework sits between the `TextInfo` and your drawing code like the MSDF font does:

![TextInfo asks a weight, the font hands it to FontFamily.resolve, which picks a face, and the provider draws its glyphs](../images/diagram-font-family.png "A glyph font: the family resolves the face, GlyphFontProvider lays the glyphs out, your drawGlyph draws each one.")

## A bitmap font on GlyphFont

This font draws each character from a sprite `Resource` (see [Resources](../resources/resources.md)) in three classes: the face, the provider and the font.

### The face with IFontFace

`IFontFace` (`dev.joid.lib.font.impl.glyph.dto`) describes one weight and style. Every metric is a fraction of the em, measured upward from the baseline: the descender and an underline below the baseline are negative.

```java
@Getter
@AllArgsConstructor
public final class BitmapFontFace implements IFontFace {

	private final String                 name;
	private final FontWeight             weight;
	private final Map<Integer, Resource> sprites;
	private final Map<Integer, Float>    advances;

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

	public Resource getSprite(final int codepoint) {
		return this.sprites.get(codepoint);
	}

}
```

`hasGlyph` decides which characters exist. A character for which it returns `false` is skipped, neither drawn nor measured, except the space (U+0020) and the no-break space (U+00A0), which advance by the space of the face or by 0.25 em. That is why `hasGlyph` reads the advances here, not the sprites: the space has an advance but no sprite.

### The provider with GlyphFontProvider

`GlyphFontProvider<F>` implements the whole `IFontProvider`; you implement `begin`, `drawGlyph` and `end`:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BitmapFontProvider extends GlyphFontProvider<BitmapFontFace> {

	private static final BitmapFontProvider INSTANCE = new BitmapFontProvider();

	public static @NonNull BitmapFontProvider inst() {
		return BitmapFontProvider.INSTANCE;
	}

	@Override
	protected void end() {}

	@Override
	protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {}

	@Override
	protected void drawGlyph(final @NonNull TextGlyph<BitmapFontFace> glyph) {
		final Resource sprite = glyph.getFace().getSprite(glyph.getCodepoint());
		if (sprite == null) {
			return;
		}

		final double x = glyph.getX() + glyph.getOffsetX();
		final double top = glyph.getBaseline() + glyph.getOffsetY() - glyph.getAscender();
		final double width = glyph.getAdvance(glyph.getCodepoint());
		final double height = glyph.getAscender() - glyph.getDescender();
		glyph.getColor().bind(() -> DrawUtils.RESOURCE.drawResource(x, top, width, height, sprite), new Vector4f((float) x, (float) top, (float) (x + width), (float) (top + height)), true);
	}

}
```

| Method | Called |
|---|---|
| `begin(double runX, double runY, double runWidth, double runHeight)` | Before the glyphs of a pass, with the bounds of the whole line (a gradient spans them). |
| `drawGlyph(TextGlyph<F> glyph)` | For every glyph of the pass. |
| `end()` | After the glyphs of a pass. |

For each line, the provider runs the `apply` and `background` hooks of the effects, then one pass for the shadow (when the `TextInfo` has a shadow color) and one for the text, each `begin`, `drawGlyph` for every glyph, `end`, then `decorate` (see [Markup and Text Effects](../text/markup-and-effects.md#text-effects-with-itexteffect)).

- Read `getCodepoint()`, `getOffsetX()`, `getOffsetY()` and `getColor()` at draw time: effects can change the character, the offset and the color. `getColor()` is the shadow color in the shadow pass.
- `isSlanted()` is `true` when italic is requested and the face drawn is upright: shear the glyph (the MSDF provider shears by 0.2 of the height above the baseline) or ignore it.
- `Vector4f` is `javax.vecmath.Vector4f`; `Color.bind(Runnable, Vector4f, boolean)` runs the drawing with the color, a gradient spanning the canvas.
- The space reaches `drawGlyph` even when the face has no glyph for it: return without drawing.

### The font with GlyphFont

```java
public final class BitmapFont extends GlyphFont<BitmapFontFace> {

	private BitmapFont(final FontFamily<BitmapFontFace> family) {
		super(family);
	}

	public static @NonNull BitmapFont create(final @NonNull BitmapFontFace @NonNull... faces) {
		return new BitmapFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		return BitmapFontProvider.inst();
	}

}
```

The text resolves its faces, kerning, markup and effects like an MSDF text. `GlyphFontProvider` reads the faces from `TextInfo.getFont()`: a provider called with a font it does not draw (not a `GlyphFont`, or a font whose `getFontProvider()` is of another class) throws `IllegalArgumentException("<provider> cannot draw the font <font>, it is drawn by <its provider>: draw it with info.getFont().getFontProvider()")`. Measure through `info.getWidth(text)` or `DrawUtils.TEXT`, which always pick the right provider.

### Pixel-perfect bitmap fonts with the bitmap size

A pixel-art font, whose glyphs are drawn on a grid of texels with `nearest()` sampling, stays legible only when each texel covers a whole number of screen pixels. Give its size in texels per em to the `GlyphFont` constructor, and JOID keeps every texel on whole pixels, like a game draws its font:

```java
public final class PixelFont extends GlyphFont<PixelFontFace> {

	private PixelFont(final FontFamily<PixelFontFace> family) {
		super(family, 8);
	}

	public static @NonNull PixelFont create(final @NonNull PixelFontFace @NonNull... faces) {
		return new PixelFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		return PixelFontProvider.inst();
	}

}
```

Here the em is 8 texels: a glyph 5 texels wide advances by `6F / 8F`, an ascender of 7 texels is `7F / 8F`. The fonts built with `super(family)`, the MSDF fonts among them, keep their size as is.

`GlyphFontProvider` snaps the size of a bitmap font to the pixel scale, the window pixels per canvas unit:

1. `pixels = size × pixelScale / bitmapSize`, the window pixels of one texel.
2. Rounded to the nearest whole number, halves up, and at least 1: a texel is never smaller than a pixel.
3. The text is laid out and drawn at `pixels × bitmapSize / pixelScale`, the size `getFontSize(info)` returns.

With a bitmap size of 8 and a font size of 24:

| Pixel scale | Exact pixels per texel | Drawn pixels per texel | Size used |
|---|---|---|---|
| 0.26 (interface scale 0.25, window fit 1.04) | 0.78 | 1 | 30.77 |
| 0.5 | 1.5 | 2 | 32 |
| 2 / 3 (a 1280×720 window) | 2 | 2 | 24 |
| 1 | 3 | 3 | 24 |
| 2 | 6 | 6 | 24 |

- Measuring follows the same size: `getWidth`, `getHeight`, `getLineHeight`, wrapping, alignment, overflow and the caret of the text fields match what is drawn. A `Text` is measured again when the size used changes, so a node sized by its text follows a resize, a zoom or a new interface scale.
- The glyphs land on whole pixels: their x and baseline are snapped to the [pixel grid](../drawing/draw-utils.md#snapping-your-own-geometry-with-pixelgrid), the offsets of the effects are rounded to whole pixels, and the shadow offset to whole pixels, at least one.
- The size grows when the scale shrinks: at a small interface scale, a pixel font keeps one pixel per texel and takes more canvas units than its font size.

The pixel scale of a UI is `getView().getPixelScale()` (interface scale × zoom × window fit), during `init`, `update`, drawing and input. Outside a UI, it is the scale of the current transform of the render bridge, or 1 without one; `FontScale.run(DoubleSupplier scale, Runnable runnable)` (`dev.joid.lib.font`) sets it for code that measures and draws text itself, and `FontScale.getScale()` reads it.

> NOTE: The size follows the view, not the transforms of the nodes. A node scaled by an integer factor keeps whole pixels per texel; another scale, a rotation or a skew draws the text as measured with the transform on top, and its glyphs keep their exact position, off the pixel grid.

## A raw font on IFont and IFontProvider

Implement the two interfaces when the text is not made of glyphs on a baseline. Your provider draws and measures one line of one run:

| Interface | Method | Contract |
|---|---|---|
| `IFont` | `getFontProvider()` | The provider that draws this font. |
| `IFontProvider` | `drawText(double x, double y, String text, TextInfo info)` | Draws one line with its top-left corner at `x`, `y` and returns its `FontBounds`; the returned width places the next run of the `Text`. |
| | `drawText(x, y, text, info, double runX, double runY, double runWidth, double runHeight)` | Same, with the bounds of the whole line (a gradient spans them); calls the first one by default. |
| | `getLineHeight(TextInfo info)` | Line height of the style. |
| | `getWidth(String text, TextInfo info)` | Width of the line; must match what `drawText` draws. |
| | `getHeight(String text, TextInfo info)` | Height of the line. |
| | `getFontSize(TextInfo info)` | The size the text is laid out and drawn at, `info.getFontSize()` by default. A `Text` is measured again when it changes. |

`TextInfo.getWidth`, `getHeight` and every measure of `Text` call these methods. With a raw provider, markup, effects, shadows, letter spacing and weights are yours to implement.

## Producing MSDF faces with IMsdfSource

To keep the MSDF rendering but produce the atlas your own way (another generator, a packed archive, a network service), implement `IMsdfSource` (`dev.joid.lib.font.impl.msdf.dto.source`) and pass it to `MsdfFontLoader.load(...)`. Extend `MsdfSource` instead to get the `weight(...)` and `italic(...)` overrides: implement its `protected MsdfFontFace parse()`.

| Method | Description |
|---|---|
| `read()` | Returns the `MsdfFontFace`; throws `IOException`. Runs on the loader threads. |
| `describe()` | Text of the dev mode log line, `"read from <class name>"` by default. |

Build the face with `MsdfFontFace.create(MsdfAtlas atlas, MsdfMetrics metrics, Map<Integer, MsdfGlyph> glyphs, Map<Long, Float> kerningPairs, BufferedImage image, String name, FontWeight weight, boolean italic)` (`IllegalArgumentException("A font face needs a name")` for an empty name):

| Part | Constructor | Units |
|---|---|---|
| `MsdfAtlas` | `new MsdfAtlas(int width, int height, float size, float distanceRange)` | Atlas size in pixels, then the em size and the range in atlas pixels. |
| `MsdfMetrics` | `new MsdfMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness)` | Fractions of the em. |
| `MsdfGlyph` | `new MsdfGlyph(int codepoint, float advance, MsdfBounds planeBounds, MsdfBounds atlasBounds)` | Advance in em; both bounds `null` for a glyph without outline, like the space. |
| `MsdfBounds` | `new MsdfBounds(float left, float bottom, float right, float top)` | Plane bounds in em from the pen on the baseline, Y up; atlas bounds in pixels, Y from the bottom row. |
| Kerning map | Keys from `MsdfFontFace.pair(int previous, int current)` | Values in fractions of the em. |
| `image` | The RGB distance field atlas. | Uploaded as a texture the first time the face draws. |

## Font warnings with FontUsage

The [missing weight warnings](adding-fonts.md#missing-weight-warnings) point at the code that created the text. If you draw text yourself from `TextElement`s, wrap the font calls in `FontUsage.trace` (`dev.joid.lib.font`) so the warnings keep pointing at the code that created the run:

```java
final double width = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(element.getText()));
```

## Reference

| Type | Members |
|---|---|
| `IFontFace` | `getName()`, `getWeight()`, `isItalic()`, `hasGlyph(codepoint)`, `getAdvance(codepoint)`, `getKerning(previous, current)`, and the metrics `getAscender()`, `getDescender()`, `getLineHeight()`, `getUnderlineY()`, `getUnderlineThickness()`, all as fractions of the em measured upward from the baseline. |
| `FontFamily<F>` | `FontFamily.of(F... faces)` sorts the faces by weight and refuses an empty family (`"A font family needs at least one face"`) or two faces of the same weight and style; `resolve(weight, italic)` picks the face and prints the dev warning; `getFaces()` lists them. |
| `GlyphFont<F>` | `protected` constructors taking the `FontFamily`, and the bitmap size in texels per em (`0` by default: not a bitmap font; a negative size throws `IllegalArgumentException`), `getFace(weight, italic)`, `getFamily()`, `getBitmapSize()`, `isBitmap()`, `snapSize(float size, double pixelScale)` (the size used at that pixel scale; the size itself for a font that is not a bitmap, a size of 0 or a scale of 0). |
| `GlyphFontProvider<F>` | Implements `IFontProvider`; you implement `begin`, `drawGlyph`, `end`. `getFontSize(info)` is the snapped size of a bitmap font, the size of the `TextInfo` otherwise. `layout(text, info)` returns the `GlyphLayout<F>` it measures and draws: `getWidth()` and `getPlacements()`, one `GlyphPlacement<F>` per glyph with `getIndex()`, `getCodepoint()`, `getFace()`, `getX()` and `getStyle()`. |
| `TextGlyph<F>` | The glyph handed to `drawGlyph`: the [`ITextGlyph`](../text/markup-and-effects.md#itextglyph) values plus `getFace()` and `isSlanted()`. |
| `FontScale` | `FontScale.run(DoubleSupplier scale, Runnable runnable)` runs the code with this pixel scale and restores the previous one, even when it throws; `FontScale.getScale()` is the pixel scale of the scope, else the scale of the render bridge transform, else 1. A UI runs its `init`, frames and input in a scope of its pixel scale. |
| `FontUsage` | `FontUsage.trace(StackTraceElement[] origin, DoubleSupplier usage)` runs the usage with this origin and returns its result (a `null` origin, outside dev mode, runs it as is); `FontUsage.getOrigin()` is the origin of the usage in progress, `null` outside `trace`. |

## Pitfalls

- `getWidth` must match what `drawText` draws, or alignment, wrapping and boxes are off.
- Return your provider only from a `GlyphFont` of the same face type: the provider casts the font of the `TextInfo`.
- Do not call a provider directly with another font: go through `info.getFont().getFontProvider()`, `info.getWidth(text)` or `DrawUtils.TEXT`.
- A pixel font without its bitmap size drops rows and columns of texels at small or fractional scales: pass the bitmap size to `GlyphFont` and draw its sprites with `nearest()` resources.
- The drawn size of a bitmap font is `glyph.getSize()`, not the size of the `TextInfo`: draw the quads from the glyph.

## See also

- Next: [Custom Nodes](../nodes/custom-nodes.md) — the Advanced section.
- [How Fonts Work](how-fonts-work.md)
- [Adding Your Own Fonts](adding-fonts.md)
- [Markup and Text Effects](../text/markup-and-effects.md)
- [Drawing Text](../drawing/text.md)
- [Resources](../resources/resources.md)