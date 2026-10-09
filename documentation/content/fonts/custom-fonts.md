# Custom Font Implementations

When the MSDF fonts do not fit, for a pixel-art font drawn from sprites or a text engine of your own, you write your own font. Build on the glyph framework of JOID and only draw glyphs, describe where the glyphs of a pixel-art atlas lie, implement the two font interfaces from scratch, or feed MSDF faces from your own source. This page closes the Fonts section and builds on the concepts of [How Fonts Work](how-fonts-work.md).

```java
final SpriteFont pixel = SpriteFont.create(regular, bold);
TextNode.create(100, 100).text(Text.create("Game over", TextInfo.create(pixel, FontWeight.BOLD, 24F, Color.WHITE))).attach(this);
TextNode.create(100, 140).text(Text.create("Game over", TextInfo.create(pixel, 24F, Color.WHITE))).attach(this);
```

![Game over drawn in a bold pixel font, and below it in the regular face](../images/custom-font-bitmap.png "The SpriteFont of this page with 5 × 7 pixel sprites: one family, its bold and regular faces (2× scale).")

`SpriteFont` is the three classes of [A sprite font on GlyphFont](#a-sprite-font-on-glyphfont); `regular` and `bold` are two `SpriteFontFace` built from your sprites. For a font drawn from an atlas of texels, extend the bitmap framework instead (see [Pixel-art fonts on BitmapFont](#pixel-art-fonts-on-bitmapfont)).

## Choosing the level

| Level | You write | You get |
|---|---|---|
| Glyph font: extend `GlyphFont` and `GlyphTextRenderer` (`dev.joid.lib.font.impl.glyph`) | A face (`IFontFace`), the font class and how one glyph is drawn. | Families and weight resolution, slanted italic, advances, kerning, letter spacing, line height, markup, effects, shadows, measuring. |
| Bitmap font: extend `BitmapFont` and `BitmapTextRenderer` (`dev.joid.lib.font.impl.bitmap`) | A face, the font class with its bitmap size, and where each glyph lies in its atlas. | Everything a glyph font gets, plus the drawing: texels filtered by area at any scale, lines on the pixel grid, color, gradient, italic, synthetic bold, one-channel atlases. |
| Raw font: implement `IFont` and `ITextRenderer` (`dev.joid.lib.font`) | Layout, drawing and measuring of a whole line. | Use everywhere a `TextInfo` goes. |
| MSDF faces from another source: implement `IMsdfSource` | Building an `MsdfFontFace` from your data. | Everything the MSDF fonts do. |

The glyph framework sits between the `TextInfo` and your drawing code like the MSDF font does:

![TextInfo asks a weight, the font hands it to FontFamily.resolve, which picks a face, and the renderer draws its glyphs](../images/diagram-font-family.png "A glyph font: the family resolves the face, GlyphTextRenderer lays the glyphs out, your drawGlyph draws each one.")

## A sprite font on GlyphFont

This font draws each character from a sprite `Resource` (see [Resources](../resources/resources.md)) in three classes: the face, the renderer and the font.

### The face with IFontFace

`IFontFace` (`dev.joid.lib.font.impl.glyph`) describes one weight and style. Every metric is a fraction of the em, measured upward from the baseline: the descender and an underline below the baseline are negative.

```java
@Getter
@AllArgsConstructor
public final class SpriteFontFace implements IFontFace {

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

### The renderer with GlyphTextRenderer

`GlyphTextRenderer<F>` implements the whole `ITextRenderer`; you implement `begin`, `drawGlyph` and `end`:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpriteTextRenderer extends GlyphTextRenderer<SpriteFontFace> {

	private static final SpriteTextRenderer INSTANCE = new SpriteTextRenderer();

	public static @NonNull SpriteTextRenderer inst() {
		return SpriteTextRenderer.INSTANCE;
	}

	@Override
	protected void end() {}

	@Override
	protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {}

	@Override
	protected void drawGlyph(final @NonNull TextGlyph<SpriteFontFace> glyph) {
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

For each line, the renderer runs the `apply` and `background` hooks of the effects, then one pass for the shadow (when the `TextInfo` has a shadow color or a shadow tint) and one for the text, each `begin`, `drawGlyph` for every glyph, `end`, then `decorate` (see [Markup and Text Effects](../text/markup-and-effects.md#text-effects-with-itexteffect)).

- Read `getCodepoint()`, `getOffsetX()`, `getOffsetY()` and `getColor()` at draw time: effects can change the character, the offset and the color. `getColor()` is the shadow color in the shadow pass.
- `isSlanted()` is `true` when italic is requested and the face drawn is upright: shear the glyph (the MSDF renderer shears by 0.2 of the height above the baseline) or ignore it.
- `Vector4f` is `javax.vecmath.Vector4f`; `Color.bind(Runnable, Vector4f, boolean)` runs the drawing with the color, a gradient spanning the canvas.
- A renderer that draws its glyphs with its own shader calls `uniformColor(IShader shader, Color color)`: it writes the uniforms `color`, `u_HasGradient` and, for a gradient, `u_GradientStart`, `u_GradientEnd`, `u_GradientStartPos`, `u_GradientEndPos` and `u_GradientCanvas` (the bounds of the line given to `begin`), as the MSDF renderer does, so a gradient spans the whole line.
- The space reaches `drawGlyph` even when the face has no glyph for it: return without drawing.
- `isGridAligned()` returns `false` by default. Override it to return `true` and each line starts on the [pixel grid](../drawing/draw-utils.md#snapping-your-own-geometry-with-pixelgrid): its x and its baseline are snapped, the glyphs follow at their exact advances, the offsets of the effects are rounded to whole pixels, and the shadow offset to whole pixels, at least one. Nothing is snapped under a rotation or a skew. `BitmapTextRenderer` returns `true`.

### The font with GlyphFont

```java
public final class SpriteFont extends GlyphFont<SpriteFontFace> {

	private SpriteFont(final FontFamily<SpriteFontFace> family) {
		super(family);
	}

	public static @NonNull SpriteFont create(final @NonNull SpriteFontFace @NonNull... faces) {
		return new SpriteFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull ITextRenderer getTextRenderer() {
		return SpriteTextRenderer.inst();
	}

}
```

The text resolves its faces, kerning, markup and effects like an MSDF text. `GlyphTextRenderer` reads the faces from `TextInfo.getFont()`: a renderer called with a font it does not draw (not a `GlyphFont`, or a font whose `getTextRenderer()` is of another class) throws `IllegalArgumentException("<renderer> cannot draw the font <font>, it is drawn by <its renderer>: draw it with info.getFont().getTextRenderer()")`. Measure through `info.getWidth(text)` or `DrawUtils.TEXT`, which always pick the right renderer.

## Pixel-art fonts on BitmapFont

A pixel-art font is drawn from a grid of texels in an atlas. Sprites drawn as plain `nearest()` quads have texels of uneven widths at a fractional scale and drop rows and columns below one pixel per texel. The bitmap framework (`dev.joid.lib.font.impl.bitmap`) draws them right: extend `BitmapFont` with the size of the em in font pixels, and `BitmapTextRenderer` with one method that tells where each glyph lies in its atlas. The face is an `IFontFace` like any other.

### The font with BitmapFont

```java
public final class PixelFont extends BitmapFont<PixelFontFace> {

	private PixelFont(final FontFamily<PixelFontFace> family) {
		super(family, 8);
	}

	public static @NonNull PixelFont create(final @NonNull PixelFontFace @NonNull... faces) {
		return new PixelFont(FontFamily.of(faces));
	}

	@Override
	public @NonNull ITextRenderer getTextRenderer() {
		return PixelTextRenderer.inst();
	}

}
```

The second argument is the bitmap size, the font pixels per em: here a glyph 5 font pixels wide advances by `6F / 8F` and an ascender of 7 font pixels is `7F / 8F`. A size of 0 or less throws `IllegalArgumentException("The bitmap size of a font must be positive: <size>")`.

### The renderer with BitmapTextRenderer

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PixelTextRenderer extends BitmapTextRenderer<PixelFontFace> {

	private static final PixelTextRenderer INSTANCE = new PixelTextRenderer();

	public static @NonNull PixelTextRenderer inst() {
		return PixelTextRenderer.INSTANCE;
	}

	@Override
	protected BitmapCell getCell(final @NonNull TextGlyph<PixelFontFace> glyph) {
		final int codepoint = glyph.getCodepoint();
		if (codepoint == ' ' || !glyph.hasGlyph(codepoint)) {
			return null;
		}

		final int x = (codepoint - ' ') % 16 * 8;
		final int y = (codepoint - ' ') / 16 * 8;
		return BitmapCell.create(glyph.getFace().getAtlas(), x, y, x + 8, y + 8);
	}

}
```

`getCell` is called for every glyph of every pass and returns the `BitmapCell` to draw, or `null` to draw nothing (the space, a missing glyph, an atlas not loaded yet). `getAtlas()` is a method of your face that returns the atlas as an `ITexture`: create it on the render thread with `BridgeHandler.RENDER.get().createTexture().allocate(width, height).upload(pixels, width, height)`, or keep a `Resource` and return `resource.getTexture()` after `resource.prepareBind()`, as `DemoPixelTextRenderer` (`dev.joid.demo.ui.font.pixel`), the renderer of `DemoFont.PIXEL`, does. The renderer implements `begin`, `drawGlyph` and `end` as `final` methods and does the rest.

### The cell with BitmapCell

| Member | Description |
|---|---|
| `BitmapCell.create(ITexture texture, int texelLeft, int texelTop, int texelRight, int texelBottom)` | The atlas and the texels of the glyph in it, from the top-left corner of the atlas. The texels around them count as transparent. A cell without texels throws `IllegalArgumentException`. |
| `bounds(double left, double top, double right, double bottom)` | Where the cell is drawn, in font pixels (one font pixel is `1 / bitmapSize` em): x from the pen, y down from the top of the ascender. By default `0, 0` and the size of the cell in texels, for an atlas of one texel per font pixel. Empty bounds throw `IllegalArgumentException`. |
| `grayscale(boolean grayscale)` | A one-channel atlas (`R8`): the red channel is the coverage, drawn in the text color. `false` by default: the atlas is RGBA and its colors are multiplied by the text color. |
| `bold(boolean bold)` | Draws the cell a second time one font pixel to the right, a synthetic bold for a family without a bold face. `false` by default. |
| `getWidth()`, `getHeight()`, `getTexelWidth()`, `getTexelHeight()` | The size of the bounds in font pixels and of the cell in texels. |

The bounds decide the size on the window; the texels only decide the detail. Every atlas below draws a glyph of a font of bitmap size 8 at the same size:

| Atlas | Cell | `bounds` |
|---|---|---|
| ASCII sheet, 8 texels per glyph | 8 × 8 texels | The default. |
| Unifont, 16 texels drawn in 8 font pixels | 16 × 16 texels (8 × 16 for a half-width glyph) | `bounds(0, 0, 8, 8)` (`bounds(0, 0, 4, 8)`). |
| High-definition pack, 32 texels per glyph | 32 × 32 texels | `bounds(0, 0, 8, 8)`. |
| One-channel atlas, 8 texels per glyph | 8 × 8 texels | The default, with `grayscale(true)`. |

### How a bitmap font is drawn

A bitmap font keeps its size, like any font: a text of size 20 is laid out, measured, wrapped and drawn at 20 canvas units per em at every interface scale, zoom and window size, so sizes 16 and 20 always differ. The bitmap size changes how its glyphs land on the window, at `size × pixelScale / bitmapSize` window pixels per font pixel:

- Each line starts on the pixel grid (`isGridAligned()` is `true`): its x and its baseline are snapped to the [pixel grid](../drawing/draw-utils.md#snapping-your-own-geometry-with-pixelgrid), the glyphs follow at their exact advances, the offsets of the effects are rounded to whole pixels, and the shadow offset to whole pixels, at least one.
- The core shader `CoreShader.BITMAP` gives each window pixel the share of every texel it covers. A whole number of pixels per texel stays crisp, each texel exactly that many pixels; a fractional number keeps every texel the same width, with one soft pixel where two texels meet; below one pixel per texel, the texels blend instead of dropping rows and columns.

With a bitmap size of 8, an atlas of one texel per font pixel and a font size of 24:

| Pixel scale | Pixels per texel | Drawn |
|---|---|---|
| 0.25 | 0.75 | Each pixel blends the texels it covers |
| 2 / 3 (a 1280×720 window) | 2 | Crisp, 2 × 2 pixels per texel |
| 1 | 3 | Crisp, 3 × 3 pixels per texel |
| 1.2676 (a 2560×1369 window) | 3.8 | Every texel 3.8 pixels wide, one soft pixel between two texels |
| 2 | 6 | Crisp, 6 × 6 pixels per texel |

An atlas of 2 or 4 texels per font pixel has 2 or 4 times fewer pixels per texel, at the same size. For each glyph, `BitmapTextRenderer`:

- creates the `CoreShader.BITMAP` shader the first time it draws on a render bridge, and throws `IllegalStateException("The bitmap font shader is not usable")` when the backend cannot compile it;
- binds the atlas of the cell with `TextureWrap.CLAMP_TO_EDGE` (the shader reads the centers of the texels, so the filter of the atlas does not matter, and it reads no mipmap) and sets the uniforms `texel` (`1 / width` and `1 / height` of the atlas), `pixel` (the texels of the cell per window pixel on each axis, from the texels of the cell and the size of its bounds on the window), `bounds` (the texels of the cell), `grayscale`, `color` and the gradient uniforms (see `uniformColor` above);
- draws the quad of the bounds one window pixel larger on every side, its texture coordinates extended by `pixel` texels, so the soft pixels on the outer edges are drawn too, and shears it by 0.2 of the height above the baseline when `isSlanted()`;
- sets the atlas, the color, `grayscale` and `pixel` again only when they change from the previous glyph;
- throws `IllegalStateException` for a font of its own that is not a `BitmapFont`.

> NOTE: Under a rotation or a skew, nothing is snapped and the glyphs keep their exact position; `pixel` follows the length of each axis of the transform, so the texels stay filtered.

## A raw font on IFont and ITextRenderer

Implement the two interfaces when the text is not made of glyphs on a baseline. Your renderer draws and measures one line of one run:

| Interface | Method | Contract |
|---|---|---|
| `IFont` | `getTextRenderer()` | The renderer that draws this font. |
| `ITextRenderer` | `drawText(double x, double y, String text, TextInfo info)` | Draws one line with its top-left corner at `x`, `y` and returns its `FontBounds`; the returned width places the next run of the `Text`. |
| | `drawText(x, y, text, info, double runX, double runY, double runWidth, double runHeight)` | Same, with the bounds of the whole line (a gradient spans them); calls the first one by default. |
| | `getLineHeight(TextInfo info)` | Line height of the style. |
| | `getWidth(String text, TextInfo info)` | Width of the line; must match what `drawText` draws. |
| | `getHeight(String text, TextInfo info)` | Height of the line. |

`TextInfo.getWidth`, `getHeight` and every measure of `Text` call these methods. With a raw renderer, markup, effects, shadows, letter spacing and weights are yours to implement.

## Producing MSDF faces with IMsdfSource

To keep the MSDF rendering but produce the atlas your own way (another generator, a packed archive, a network service), implement `IMsdfSource` (`dev.joid.lib.font.impl.msdf.source`) and pass it to `MsdfFontLoader.load(...)`. Extend `MsdfSource` instead to get the `weight(...)` and `italic(...)` overrides: implement its `protected MsdfFontFace parse()`.

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
| `GlyphFont<F>` | `protected` constructor taking the `FontFamily`, `getFace(weight, italic)`, `getFamily()`. |
| `GlyphTextRenderer<F>` | Implements `ITextRenderer`; you implement `begin`, `drawGlyph`, `end`, and override `isGridAligned()` (`false` by default) to start the lines on the pixel grid. `layout(text, info)` returns the `GlyphLayout<F>` it measures and draws: `getWidth()` and `getPlacements()`, one `GlyphPlacement<F>` per glyph with `getIndex()`, `getCodepoint()`, `getFont()` (the font of the glyph, another one after a font markup), `getFace()`, `getX()` and `getStyle()`. |
| `TextGlyph<F>` | The glyph handed to `drawGlyph`: the [`ITextGlyph`](../text/markup-and-effects.md#itextglyph) values plus `getFont()`, `getFace()` and `isSlanted()`. |
| `BitmapFont<F>` | A `GlyphFont` with a `protected` constructor taking the `FontFamily` and the bitmap size in font pixels per em (positive), and `getBitmapSize()`. |
| `BitmapTextRenderer<F>` | A `GlyphTextRenderer` whose `begin`, `drawGlyph`, `end` and `isGridAligned()` are `final`; you implement `getCell(TextGlyph<F> glyph)`. |
| `BitmapCell` | `BitmapCell.create(texture, texelLeft, texelTop, texelRight, texelBottom)`, `bounds(left, top, right, bottom)`, `grayscale(boolean)`, `bold(boolean)`, their getters, `getWidth()`, `getHeight()`, `getTexelWidth()`, `getTexelHeight()`. |
| `FontUsage` | `FontUsage.trace(StackTraceElement[] origin, DoubleSupplier usage)` runs the usage with this origin and returns its result (a `null` origin, outside dev mode, runs it as is); `FontUsage.getOrigin()` is the origin of the usage in progress, `null` outside `trace`. |

## Pitfalls

- `getWidth` must match what `drawText` draws, or alignment, wrapping and boxes are off.
- Return your renderer only from a `GlyphFont` of the same face type: the renderer casts the font of the `TextInfo`.
- Do not call a renderer directly with another font: go through `info.getFont().getTextRenderer()`, `info.getWidth(text)` or `DrawUtils.TEXT`.
- A pixel font drawn as plain `nearest()` quads has texels of uneven widths at a fractional scale and drops rows and columns below one pixel per texel: extend `BitmapFont` and `BitmapTextRenderer`.
- Give `bounds` to a cell whose atlas has more than one texel per font pixel (Unifont, high-definition packs): with the default bounds, its glyphs are drawn 2 or 4 times too large.

## See also

- Next: [Custom Nodes](../nodes/custom-nodes.md) — the Advanced section.
- [How Fonts Work](how-fonts-work.md)
- [Adding Your Own Fonts](adding-fonts.md)
- [Markup and Text Effects](../text/markup-and-effects.md)
- [Drawing Text](../drawing/text.md)
- [Resources](../resources/resources.md)