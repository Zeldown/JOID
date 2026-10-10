# Text and Fonts

Text is built from three pieces: a font that you load once, a `TextInfo` that describes a style (font, size, weight, color), and a `Text` that combines a string with a style. A `TextNode` displays a `Text` and sizes itself to it.

```java
final TextInfo title = TextInfo.create(Fonts.INTER, FontWeight.BOLD, 48F, Color.WHITE);
TextNode.create(100, 100).text(Text.create("Hello JOID", title)).attach(this);
```

![Hello JOID in large bold white letters](../images/ess-text-hello.png "Created without a size, the TextNode takes the size of its text.")

![A font file is loaded once into an MsdfFont, a TextInfo describes a style, a Text holds a string and its style, and a TextNode draws it](../images/ess-diagram-text.png "From a font file to a TextNode.")

## Loading fonts with MsdfFontLoader

JOID draws text with MSDF fonts, which stay sharp at any size, zoom and rotation. `MsdfFontLoader.load(...)` reads `.ttf`, `.otf` or pre-generated `.msdf` files in the background and returns a `CompletableFuture<MsdfFont>`. Pass every weight and style of the family in one call; each style built on it then picks the right face. Keep your fonts in one class and load them once at startup, after `JOID.inst().load()`:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Fonts {

	public static MsdfFont INTER;

	public static void load() {
		Fonts.INTER = MsdfFontLoader.load(Fonts.get("Inter-Regular.ttf"), Fonts.get("Inter-Bold.ttf"), Fonts.get("Inter-Italic.ttf")).join();
	}

	private static InputStream get(final String file) {
		return Fonts.class.getResourceAsStream("/assets/fonts/" + file);
	}

}
```

![A TextInfo asks for weight 600; the MsdfFont resolves it in its FontFamily to the closest upright face, 700 Bold, which the MsdfTextRenderer draws](../images/diagram-font-family.png "How a style picks a face of the family")

A handle is an `InputStream`, a `File`, a URL `String` or an `Asset`. The first load of a font file generates its atlas into a cache folder, which takes a moment; later runs read the cache. To skip that step, ship `.msdf` atlases made with the [MSDF Generator](../fonts/custom-fonts.md#pre-generating-atlases-with-the-msdf-generator). In dev and demo mode, `DevFont.MONTSERRAT` is already loaded with the nine weights of Montserrat.

On this page, `font` is a loaded family and `info` a `TextInfo` field of the UI.

## Styles with TextInfo

`TextInfo.create(font, [weight,] size, [color])` creates a style; the size is in canvas units and every setter chains:

```java
final TextInfo title = TextInfo.create(this.font, FontWeight.BOLD, 48F, Color.WHITE);
final TextInfo body = TextInfo.create(this.font, 24F, Color.WHITE);
final TextInfo caption = TextInfo.create(this.font, 18F, Color.LIGHTGRAY).italic(true).letterSpacing(0.04F).shadow();
```

![Samples of the title, body and caption styles](../images/ess-text-styles.png "The three styles; the caption is italic, spaced and shadowed.")

`FontWeight` names the nine weights, from `THIN` (100) to `BLACK` (900). When the family has no face of a weight, the closest one is drawn and dev mode prints a warning: load every weight you use. Without an italic face, `italic(true)` slants the upright face.

A `TextInfo` is shared by reference: derive a variant with `copy()`, as in `this.info.copy().fontSize(28F)`.

## Text that follows signals

`Text.create` follows the signals its expression reads: the text updates itself and the node resizes. A lambda is read every frame instead, for a clock or a counter:

```java
private final IntegerSignal clicks = IntegerSignal.of(0);
```

```java
TextNode.create(20, 20).text(Text.create("Clicks: " + this.clicks.get(), this.info)).attach(this);
TextNode.create(20, 60).text(Text.create(() -> "Time: " + System.currentTimeMillis() / 1000L, this.info)).attach(this);
```

![Five clicks count a text up to Clicks: 5](../images/ess-state-counter.gif "A text built from a signal follows it.")

`Text.create` accepts any object: a `String` is drawn as is, other objects go through `TextConverter`. Register an `ITextConverter` to turn your own objects, such as translation keys, into strings at every draw.

## Several styles with TextElement

A `Text` is a line of runs, each with its own style:

```java
final TextInfo strong = this.info.copy().weight(FontWeight.BOLD);
TextNode.create(100, 300).text(Text.create(TextElement.create("Welcome back, ", this.info), TextElement.create("Ada", strong))).attach(this);
```

![The words Welcome back, followed by the name Ada in bold](../images/ess-text-runs.png "Two runs on one line, the second in bold.")

## Alignment with Align

The alignment belongs to the `Text`: pass it to `Text.create(text, info, horizontal, vertical)` or set it with `horizontalAlign(Align)` and `verticalAlign(Align)` (`START`, `CENTER`, `END`). Give the node a size to align its text inside it:

```java
RectNode
.create(100, 200, 300, 60)
.color(Color.DARKGRAY)
.hoveredColor(Color.GRAY)
.body(rect -> {
	TextNode.create(0, 0, rect.getWidth(), rect.getHeight()).text(Text.create("Play", this.info, Align.CENTER, Align.CENTER)).attach(rect);
})
.attach(this);
```

![Nine boxes showing a text aligned START, CENTER and END horizontally and vertically](../images/text-align.png "The nine combinations of horizontal and vertical alignment.")

## Long text with TextMode and TextOverflow

By default a `TextNode` draws its text on one line. `mode(TextMode)` changes that, and `TextOverflow` is the mark that ends a cut text:

```java
TextNode.create(100, 400, 300, 0).text(Text.create("A very long subtitle that does not fit", this.info, TextOverflow.ELLIPSIS)).mode(TextMode.OVERFLOW).attach(this);
TextNode.create(100, 450, 300, 0).text(Text.create("A paragraph wrapped on as many lines as it needs.", this.info)).mode(TextMode.SPLIT).attach(this);
```

![A subtitle cut with an ellipsis above a paragraph wrapped on three lines](../images/ess-text-modes.png "OVERFLOW cuts the first text at 300 units, SPLIT wraps the second.")

| Mode | Result |
|---|---|
| `NORMAL` (default) | One line, never cut. A width or height of 0 takes the size of the text. |
| `OVERFLOW` | One line, cut to the width of the node and ended with the overflow mark. |
| `SPLIT` | Wrapped to the width of the node at `\n`, `<br>` or the last space that fits; the height follows the lines. |
| `BOX` | Wrapped like `SPLIT`; only the lines that fit inside the box are drawn. |

The marks are `NONE` (default), `ELLIPSIS` (`...`), `DOT` (`.`) and `HYPHEN` (`-`).

## Case with TextModifier

A modifier rewrites the string right before it is measured and drawn. `TextModifier` holds `UPPER_CASE`, `LOWER_CASE`, `CAPITALIZE`, `WORD_CAPITALIZE`, `CAMEL_CASE`, `UPPER_CAMEL_CASE` and `SNAKE_CASE`; a lambda is a modifier too:

```java
TextNode.create(100, 500).text(Text.create("player settings", this.info).modifier(TextModifier.WORD_CAPITALIZE)).attach(this);
TextNode.create(100, 540).text(Text.create("hunter2", this.info).modifier(text -> text.replaceAll(".", "*"))).attach(this);
```

![hello big_World rewritten by each TextModifier](../images/text-modifiers.png "Each built-in modifier applied to hello big_World.")

## Markup with ITextMarkup

Markup styles parts of a string from inside, such as `<b>bold</b>`. JOID has no syntax of its own: an `ITextMarkup` returns `0` when no code starts at `index`, otherwise it changes `style` and returns the length of the code, which is neither drawn nor measured. `style.getBase()` is the style of the `TextInfo`, to close a tag:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagTextMarkup implements ITextMarkup {

	private static final TagTextMarkup INSTANCE = new TagTextMarkup();

	private static final Pattern TAG = Pattern.compile("<(?:([biu])|c=([0-9a-fA-F]{6})|/([biuc]))>");

	public static @NonNull TagTextMarkup inst() {
		return TagTextMarkup.INSTANCE;
	}

	@Override
	public int parse(final @NonNull String text, final int index, final @NonNull TextStyle style) {
		if (text.charAt(index) != '<') {
			return 0;
		}

		final Matcher matcher = TagTextMarkup.TAG.matcher(text).region(index, text.length());
		if (!matcher.lookingAt()) {
			return 0;
		}

		final TextStyle base = style.getBase();
		if (matcher.group(2) != null) {
			style.color(Color.decode("#" + matcher.group(2)));
		} else if ("b".equals(matcher.group(1))) {
			style.weight(FontWeight.BOLD);
		} else if ("i".equals(matcher.group(1))) {
			style.italic(true);
		} else if ("u".equals(matcher.group(1))) {
			style.effect(UnderlineTextEffect.inst());
		} else if ("b".equals(matcher.group(3))) {
			style.weight(base.getWeight());
		} else if ("i".equals(matcher.group(3))) {
			style.italic(base.isItalic());
		} else if ("u".equals(matcher.group(3))) {
			style.removeEffect(UnderlineTextEffect.inst());
		} else {
			style.color(base.getColor());
		}
		return matcher.end() - index;
	}

}
```

Register it once; every `TextInfo` follows the registry until you call `markups(...)` on it, and `markups()` with no argument draws the raw string, for text typed by users:

```java
TextMarkup.register(TagTextMarkup.inst());
TextNode.create(100, 100).text(Text.create("<b>Bold</b>, <i>italic</i>, <c=ff5555>red</c> and <u>underlined</u>", this.info)).attach(this);
```

![Bold, italic, red and underlined drawn from one tagged string](../images/markup-quick.png "The tags take no room and are not drawn.")

In `SPLIT` and `BOX`, a style opened by a tag continues on the next lines of its run.

## Effects with ITextEffect

An `ITextEffect` takes part in drawing each glyph it is attached to. Its three hooks are `default` no-ops: `apply` changes the character, color or offset of a glyph before drawing, `background` draws behind the text, `decorate` draws over it. The underline of the markup above decorates each glyph:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnderlineTextEffect implements ITextEffect {

	private static final UnderlineTextEffect INSTANCE = new UnderlineTextEffect();

	public static @NonNull UnderlineTextEffect inst() {
		return UnderlineTextEffect.INSTANCE;
	}

	@Override
	public void decorate(final @NonNull ITextGlyph glyph) {
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), glyph.getUnderlineThickness(), glyph.getColor());
	}

}
```

A wave moves each glyph without changing the layout; the shadow follows it:

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaveTextEffect implements ITextEffect {

	private static final WaveTextEffect INSTANCE = new WaveTextEffect();

	public static @NonNull WaveTextEffect inst() {
		return WaveTextEffect.INSTANCE;
	}

	@Override
	public void apply(final @NonNull ITextGlyph glyph) {
		glyph.offset(0D, Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 150D + glyph.getIndex() * 0.6D) * glyph.getSize() / 8D);
	}

}
```

Attach an effect to a whole run with `TextInfo.effects(...)`, or to part of a string from markup with `style.effect(...)`:

```java
final TextInfo wave = this.info.copy().effects(WaveTextEffect.inst()).shadow(Color.BLACK);
TextNode.create(100, 100).text(Text.create("the shadow waves with the text", wave)).attach(this);
```

![A waving line of text with a black shadow that follows each glyph](../images/text-effect-wave.gif "The shadow glyphs are copies of the glyphs after apply.")

## Measuring text

Ask the style for the size of a string: `getWidth(text)` and `getHeight()`, or `aw(text, value)` and `ah(value)`, which add a padding. A box that fits its label with 24 units on each side and 12 above and below:

```java
RectNode.create(100, 520, this.info.aw("Settings", 48D), this.info.ah(24D)).color(Color.DARKGRAY).attach(this);
```

![A gray box sized around the word Settings](../images/ess-text-measure.png "The box is the text plus 48 units wide and the line height plus 24 high.")

## Reference

| `TextInfo` method | Description |
|---|---|
| `TextInfo.create(font, [weight,] size, [color])` | New style; `REGULAR` and `Color.BLACK` by default. |
| `fontSize(float)`, `weight(FontWeight)`, `italic(boolean)`, `color(Color)` | Size in canvas units, weight, italic, color (a gradient spans the line). |
| `letterSpacing(float)` | Space between glyphs as a fraction of the size (`0.1F` = 10 %), `0F` by default. |
| `lineHeight(float)` | Line height as a fraction of the size (`1.5F` = 150 %); `0F` keeps the font's. |
| `shadow()`, `shadow(Color)`, `shadow(float x, float y)` | Shadow 30 % darker than the text, of a color, at an offset (`fontSize / 13.5` by default). |
| `markups(ITextMarkup...)`, `effects(ITextEffect...)`, `colored(boolean)` | Markups of the run (`markups()` turns them off), effects of every glyph, whether markup colors apply. |
| `copy()` | New style with every property copied. |
| `getWidth(text)`, `getHeight()`, `aw`, `ah`, `dw`, `dh` | Size of a line, plus or divided by a value. |

| `Text` method | Description |
|---|---|
| `Text.create(text, info, [horizontal, [vertical]], [overflow])` | One run; `text` is any object or a `Supplier`. |
| `Text.create(TextElement...)` | Several runs on one line. |
| `horizontalAlign(Align)`, `verticalAlign(Align)` | Alignment in the box, `START` by default. |
| `overflow(TextOverflow)`, `modifier(ITextModifier)` | Mark of a cut text, modifier of the whole line. |
| `getWidth()`, `getHeight()` | Size of the line. |

| Registry | Description |
|---|---|
| `TextMarkup.register(ITextMarkup)`, `unregister(...)` | Markups of every `TextInfo` that follows the registry, the latest first. |
| `TextConverter.register(ITextConverter)`, `unregister(...)` | Converters from objects to strings, the latest first. |

## Good to know

- A `TextInfo` is shared by reference: changing it changes every text built on it. Call `copy()` to derive a variant.
- A weight the family does not hold draws its closest face; a character missing from the atlas is skipped.
- `MsdfFontLoader.load(...).join()` blocks: load fonts once at startup, never in `init()`.

## See also

- [TextNode](../nodes/visual/text.md): the node in detail.
- [Custom Fonts and the MSDF Generator](../fonts/custom-fonts.md): pixel-art and custom fonts, pre-generated atlases.
- [Signals and State](state.md): what a text follows.
- [Drawing](../drawing/drawing.md): drawing text in your own code.
- [Building a UI Kit](../components/ui-kit.md): shared fonts and text styles.