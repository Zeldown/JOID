# Markup & Effects

JOID's text engine is neutral: it knows no color code, no tag and no decoration. Two extension points let a project bring its own — **markup** turns inline codes into style changes, **effects** transform and decorate each glyph. Both work with every glyph font, MSDF included, when text is drawn and when it is measured.

## `TextStyle`

Every glyph is drawn with a `TextStyle`: a weight, an italic flag, a color and a list of effects. Drawing starts from the style of the `TextInfo`, and markup edits a working copy as it goes through the string.

```java
T weight(FontWeight weight)
T italic(boolean italic)
T color(Color color)
T effect(ITextEffect effect)        // added once, ignored if already there
T removeEffect(ITextEffect effect)
T reset()                           // back to the style of the TextInfo
TextStyle getBase()                 // the style of the TextInfo
```

## Markup

An `ITextMarkup` is asked, at every position of the string, whether a code starts there:

```java
public interface ITextMarkup {

	public int parse(String text, int index, TextStyle style);

}
```

It returns how many characters the code takes, after changing the style, or `0` when nothing starts at `index`. Consumed characters are neither drawn nor measured.

A minimal tag markup:

```java
public final class TagTextMarkup implements ITextMarkup {

	private static final Pattern TAG = Pattern.compile("<(/?)(b|i)>");

	@Override
	public int parse(final String text, final int index, final TextStyle style) {
		final Matcher matcher = TagTextMarkup.TAG.matcher(text).region(index, text.length());
		if (!matcher.lookingAt()) {
			return 0;
		}

		final boolean open = matcher.group(1).isEmpty();
		if (matcher.group(2).equals("b")) {
			style.weight(open ? FontWeight.BOLD : style.getBase().getWeight());
		} else {
			style.italic(open || style.getBase().isItalic());
		}
		return matcher.end() - index;
	}

}
```

### Registering

```java
TextMarkup.register(new TagTextMarkup());   // every TextInfo, the latest registration is tried first
TextMarkup.unregister(markup);

TextInfo.create(font, 16).markups(markup);  // only these markups for this TextInfo
TextInfo.create(font, 16).markups();        // raw text, no markup at all
```

A `TextInfo` follows the registered markups until `markups(...)` is called on it. Turn markup off for text typed by users so it is shown exactly as entered.

### Formatting codes of a host

Nothing in JOID reads `§` codes. A host that has them, like a Minecraft integration, registers its own markup once at startup:

```java
public final class FormattingTextMarkup implements ITextMarkup {

	private static final ITextEffect UNDERLINE  = new UnderlineTextEffect();
	private static final ITextEffect OBFUSCATED = new ObfuscatedTextEffect();

	private static final String   CODES  = "0123456789abcdef";
	private static final String[] COLORS = {"000000", "0000aa", "00aa00", "00aaaa", "aa0000", "aa00aa", "ffaa00", "aaaaaa", "555555", "5555ff", "55ff55", "55ffff", "ff5555", "ff55ff", "ffff55", "ffffff"};

	@Override
	public int parse(final String text, final int index, final TextStyle style) {
		if (text.charAt(index) != '§' || index + 1 >= text.length()) {
			return 0;
		}

		final char code = Character.toLowerCase(text.charAt(index + 1));
		final int color = FormattingTextMarkup.CODES.indexOf(code);
		if (color >= 0) {
			style.reset().color(Color.decode("#" + FormattingTextMarkup.COLORS[color]));
		} else if (code == 'l') {
			style.weight(FontWeight.BOLD);
		} else if (code == 'o') {
			style.italic(true);
		} else if (code == 'n') {
			style.effect(FormattingTextMarkup.UNDERLINE);
		} else if (code == 'k') {
			style.effect(FormattingTextMarkup.OBFUSCATED);
		} else if (code == 'r') {
			style.reset();
		} else {
			return 0;
		}
		return 2;
	}

}
```

## Effects

An `ITextEffect` takes part in drawing each glyph it is attached to. All three hooks are optional:

```java
public interface ITextEffect {

	public default void apply(ITextGlyph glyph) {}       // before drawing: change the codepoint, color or offset
	public default void background(ITextGlyph glyph) {}  // draw behind the text
	public default void decorate(ITextGlyph glyph) {}    // draw over the text

}
```

Attach an effect from markup with `style.effect(effect)`, or to the whole text with `TextInfo.effects(effect)`. A style holds each effect once, so share one instance per effect rather than creating a new one at every code.

The drawing order is fixed:

1. `apply` runs once per glyph.
2. `background` runs once per glyph, behind the shadow and the text.
3. The shadow is drawn from the transformed glyphs, then `decorate` runs on each of them with `isShadow()` set.
4. The text is drawn, then `decorate` runs on each glyph.

The shadow reuses the glyphs `apply` already changed, so a random or animated effect looks the same on the text and on its shadow.

### `ITextGlyph`

| Method | Value |
|---|---|
| `getIndex()` | Position of the glyph in the source string, markup included |
| `getCodepoint()` | The character drawn |
| `getX()`, `getBaseline()` | Pen position and baseline, in pixels |
| `getSize()` | Font size |
| `getAdvance()` | Distance to the next glyph, kerning and letter spacing included — consecutive glyphs touch |
| `getOffsetX()`, `getOffsetY()` | Offset applied when the glyph is drawn |
| `getAscender()`, `getDescender()` | Face metrics, in pixels |
| `getUnderlineY()`, `getUnderlineThickness()` | Underline position and thickness from the font, in pixels |
| `getColor()`, `getStyle()` | Color drawn and the style of the glyph |
| `isShadow()` | `true` while the shadow is drawn |
| `hasGlyph(int)`, `getAdvance(int)` | Whether the face has another character, and its advance |
| `codepoint(int)`, `color(Color)`, `offset(double, double)` | Changes, for `apply` |

### Examples

An underline, snapped to whole pixels so it stays sharp:

```java
public final class UnderlineTextEffect implements ITextEffect {

	@Override
	public void decorate(final ITextGlyph glyph) {
		final double left = Math.round(glyph.getX());
		final double right = Math.round(glyph.getX() + glyph.getAdvance());
		DrawUtils.SHAPE.drawRect(left, Math.round(glyph.getUnderlineY()), right - left, Math.max(1D, Math.round(glyph.getUnderlineThickness())), glyph.getColor());
	}

}
```

A wave, which moves each glyph without touching the layout:

```java
public final class WaveTextEffect implements ITextEffect {

	@Override
	public void apply(final ITextGlyph glyph) {
		glyph.offset(0D, Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 150D + glyph.getIndex()) * glyph.getSize() / 8D);
	}

}
```

Scrambled characters that keep the width of the original one:

```java
public final class ObfuscatedTextEffect implements ITextEffect {

	private static final String POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	@Override
	public void apply(final ITextGlyph glyph) {
		for (int attempt = 0; attempt < 8; attempt++) {
			final char candidate = ObfuscatedTextEffect.POOL.charAt(ThreadLocalRandom.current().nextInt(ObfuscatedTextEffect.POOL.length()));
			if (glyph.hasGlyph(candidate) && glyph.getAdvance(candidate) == glyph.getAdvance(glyph.getCodepoint())) {
				glyph.codepoint(candidate);
				return;
			}
		}
	}

}
```

A highlight behind the text:

```java
public final class HighlightTextEffect implements ITextEffect {

	@Override
	public void background(final ITextGlyph glyph) {
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getBaseline() - glyph.getAscender(), glyph.getAdvance(), glyph.getAscender() - glyph.getDescender(), Color.YELLOW.copyAlpha(0.4F));
	}

}
```

## In the demo

`UIDemoTextMarkup` and `UIDemoTextEffect` run all of the above: `DemoTextMarkup` reads `<b>`, `<i>`, `<u>`, `<h>`, `<w=NNN>` and `<c=RRGGBB>` with their closing tags, and the `demo.ui.font.effect` package holds an underline, a highlight, a wave, a rainbow and a scramble effect.

## Measuring

`TextInfo.getWidth` reads the markup like drawing does: consumed characters take no room and a weight change measures with the matching face. Effects never change the layout — offsets and decorations are visual only.

## See also

- [Custom Fonts](custom-font.md) — loading a family and choosing a weight.
- [Text](../drawing/text.md) — drawing text directly.