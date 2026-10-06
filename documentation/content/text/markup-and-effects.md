# Markup and Text Effects

Markup and effects style a string from the inside. An `ITextMarkup` turns inline codes such as `<b>` into style changes; an `ITextEffect` transforms and decorates each glyph (a wave, an underline, a highlight). The JOID library has no markup syntax of its own: you register the markup your project uses, and it works with every [glyph font](../fonts/how-fonts-work.md#glyph-fonts-with-glyphfont), when text is drawn and when it is measured.

## Quick example

```java
public final class UnderlineTextEffect implements ITextEffect {

    public static final UnderlineTextEffect INSTANCE = new UnderlineTextEffect();

    @Override
    public void decorate(final ITextGlyph glyph) {
        DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), glyph.getUnderlineThickness(), glyph.getColor());
    }

}
```

```java
public final class TagTextMarkup implements ITextMarkup {

    private static final Pattern TAG = Pattern.compile("<(?:([biu])|c=([0-9a-fA-F]{6})|/([biuc]))>");

    @Override
    public int parse(final String text, final int index, final TextStyle style) {
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
            style.effect(UnderlineTextEffect.INSTANCE);
        } else if ("b".equals(matcher.group(3))) {
            style.weight(base.getWeight());
        } else if ("i".equals(matcher.group(3))) {
            style.italic(base.isItalic());
        } else if ("u".equals(matcher.group(3))) {
            style.removeEffect(UnderlineTextEffect.INSTANCE);
        } else {
            style.color(base.getColor());
        }
        return matcher.end() - index;
    }

}
```

```java
TextMarkup.register(new TagTextMarkup());

final Text text = Text.create("<b>Bold</b>, <i>italic</i>, <c=ff5555>red</c> and <u>underlined</u>", TextInfo.create(font, 20, Color.WHITE));
TextNode.create(0, 0).text(text).attach(flex);
```

![Bold, italic, red and underlined drawn from one tagged string](../images/markup-quick.png "The tags are read while drawing: they take no room and are not drawn.")

`ITextMarkup`, `TextMarkup` (`dev.joid.lib.font.dto.markup`), `ITextEffect`, `ITextGlyph` (`dev.joid.lib.font.dto.effect`), `TextStyle` (`dev.joid.lib.font.dto`).

## Writing markup with ITextMarkup

```java
public int parse(final String text, final int index, final TextStyle style);
```

Before each character of a run, JOID asks the markups whether a code starts at `index`:

- Return `0` when nothing starts there. The character is then drawn normally.
- Otherwise change `style` and return the number of characters the code takes (above 0). These characters are neither drawn nor measured, and the next character is read after them.
- `parse` runs for every character of every run, at every measure and draw: return `0` quickly when the first character cannot start a code.

The `style` is a working copy that starts from the style of the `TextInfo` for each run and keeps its changes until the end of the run. `style.getBase()` is the style of the `TextInfo`, to close a tag back to it. The style methods are listed on [Styling Text](styling-text.md#textstyle).

| Change | Call |
|---|---|
| Weight | `style.weight(FontWeight.BOLD)`; the face switches and kerning restarts. |
| Italic | `style.italic(true)` |
| Color | `style.color(color)`; ignored when the `TextInfo` is not `colored`. |
| Add or remove an effect | `style.effect(effect)`, `style.removeEffect(effect)`; an effect is held once. |
| Back to the `TextInfo` style | `style.reset()` |

## Registering markup with TextMarkup

| Method | Description |
|---|---|
| `TextMarkup.register(ITextMarkup markup)` | Adds a markup for every `TextInfo` that follows the registry. The latest registration is tried first. |
| `TextMarkup.unregister(ITextMarkup markup)` | Removes it; an unknown markup is ignored. |
| `TextMarkup.getRegistered()` | Registered markups, latest first, read-only. |
| `TextMarkup.parse(List<ITextMarkup> markups, String text, int index, TextStyle style)` | Asks each markup in order and returns the first count above 0, or `0`. |

The first markup that consumes characters at an index wins. A `TextInfo` follows the registry until you call `markups(...)` on it:

```java
TextInfo.create(font, 16).markups(new TagTextMarkup());
TextInfo.create(font, 16).markups();
```

The first line uses only this markup for the run; the second one draws the raw string with no markup at all. Disable markup for text typed by users so it shows exactly as entered.

## The demo markup

The demo UI of fonts uses `DemoTextMarkup` (`dev.joid.demo.ui.font.markup`), only present in the `dev` jars. Its syntax is a reference for your own:

| Tag | Effect | Closing tag |
|---|---|---|
| `<b>` | Bold | `</b>` back to the base weight |
| `<w=NNN>` | Weight of three digits, through `FontWeight.of` | `</w>` back to the base weight |
| `<i>` | Italic | `</i>` |
| `<c=RRGGBB>` | Color | `</c>` back to the base color |
| `<u>` | Underline effect | `</u>` |
| `<h>` | Highlight effect | `</h>` |

![Each tag of the demo markup, written on the left and drawn on the right](../images/markup-demo.png "The demo markup: left, the raw string drawn with markups() disabled; right, the same string with DemoTextMarkup.inst().")

Closing tags go back to the `TextInfo` style, they do not restore an outer tag of the same kind.

## Limits of markup

- The style restarts at each `TextElement`: a tag opened in one run does not continue in the next one.
- `TextMode.SPLIT`, `TextMode.BOX` and `TextMode.OVERFLOW` cut the raw string, tags included. A style opened before a line break does not continue on the next line, and a cut can fall inside a tag. For wrapped or cut text, prefer one `TextElement` per style.
- With `colored(false)`, markup colors are ignored. Markup colors take the alpha of the `TextInfo` color.
- `Text.getText()` and `TextElement.getText()` return the string with its codes; only measuring and drawing read them.

## Text effects with ITextEffect

An `ITextEffect` takes part in drawing each glyph it is attached to. Its three hooks are `default` no-ops, override the ones you need:

| Hook | When | Use |
|---|---|---|
| `apply(ITextGlyph glyph)` | Before anything is drawn. | Change the character, the color or the offset of the glyph. |
| `background(ITextGlyph glyph)` | After every `apply`, before the shadow and the text. | Draw behind the text. |
| `decorate(ITextGlyph glyph)` | After the glyphs of a pass are drawn, once for the shadow and once for the text. | Draw over the glyphs. |

Attach an effect to a whole run with `TextInfo.effects(...)`, or to part of a string from markup with `style.effect(...)`. A style holds each effect once, so share one instance per effect.

For one line, the order is:

1. `apply` on every glyph.
2. `background` on every glyph.
3. When the `TextInfo` has a shadow color: the shadow glyphs are drawn, then `decorate` runs on each of them with `isShadow()` returning `true`.
4. The glyphs are drawn, then `decorate` runs on each of them.

The shadow glyphs are copies of the glyphs after `apply`, shifted by the shadow offset and drawn with the shadow color: a random or animated character looks the same on the text and on its shadow. Effects only run when text is drawn: they never change the measured width or the layout.

### ITextGlyph reference

Positions and sizes are in UI units.

| Method | Description |
|---|---|
| `getIndex()` | Index of the character in the string of the run, markup included. |
| `getCodepoint()` | Character drawn. |
| `getX()` | Pen position of the glyph. |
| `getBaseline()` | Y of the baseline. |
| `getSize()` | Font size. |
| `getAdvance()` | Distance to the next glyph, kerning and letter spacing included; consecutive glyphs touch. |
| `getAdvance(int codepoint)` | Advance of another character of the face at this size. |
| `hasGlyph(int codepoint)` | Whether the face holds another character. |
| `getAscender()` | Height above the baseline (positive). |
| `getDescender()` | Depth below the baseline (negative). |
| `getUnderlineY()`, `getUnderlineThickness()` | Y of the underline below the baseline and its thickness, from the font. |
| `getOffsetX()`, `getOffsetY()` | Offset applied when the glyph is drawn. |
| `getColor()` | Color the glyph is drawn with (the shadow color for a shadow glyph). |
| `getStyle()` | `TextStyle` of the glyph. |
| `isShadow()` | `true` for a shadow glyph. |
| `codepoint(int codepoint)` | Draws another character in the same place; the layout does not change. |
| `color(Color color)` | Draws the glyph with another color. |
| `offset(double x, double y)` | Sets the offset of the glyph. |

### Effect examples

A highlight behind the text:

```java
public final class HighlightTextEffect implements ITextEffect {

    private static final Color COLOR = new Color(255, 214, 0, 110);

    @Override
    public void background(final ITextGlyph glyph) {
        final double top = glyph.getBaseline() - glyph.getAscender();
        DrawUtils.SHAPE.drawRect(glyph.getX(), top, glyph.getAdvance(), glyph.getBaseline() - glyph.getDescender() - top, HighlightTextEffect.COLOR);
    }

}
```

![The words Highlighted text on a translucent yellow band](../images/text-effect-highlight.png "The highlight drawn by background() behind each glyph, here on a whole run with TextInfo.effects(...).")

A wave that moves each glyph without touching the layout:

```java
public final class WaveTextEffect implements ITextEffect {

    @Override
    public void apply(final ITextGlyph glyph) {
        glyph.offset(0D, Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 150D + glyph.getIndex() * 0.6D) * glyph.getSize() / 8D);
    }

}
```

A rainbow, glyph by glyph, keeping the alpha of the text:

```java
public final class RainbowTextEffect implements ITextEffect {

    @Override
    public void apply(final ITextGlyph glyph) {
        glyph.color(Color.RAINBOW(BridgeHandler.CLOCK.get().currentTimeMillis() + glyph.getIndex() * 120L).copyAlpha(glyph.getColor().a));
    }

}
```

Scrambled characters of about the same width, centered on the original:

```java
public final class ScrambleTextEffect implements ITextEffect {

    private static final String POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public void apply(final ITextGlyph glyph) {
        if (Character.isWhitespace(glyph.getCodepoint())) {
            return;
        }

        final double advance = glyph.getAdvance(glyph.getCodepoint());
        final char candidate = ScrambleTextEffect.POOL.charAt(ThreadLocalRandom.current().nextInt(ScrambleTextEffect.POOL.length()));
        if (glyph.hasGlyph(candidate) && Math.abs(glyph.getAdvance(candidate) - advance) <= advance * 0.25D) {
            glyph.codepoint(candidate).offset(glyph.getOffsetX() + (advance - glyph.getAdvance(candidate)) / 2D, glyph.getOffsetY());
        }
    }

}
```

![Three lines animated by the wave, rainbow and scramble effects](../images/text-effects-animated.gif "WaveTextEffect, RainbowTextEffect and ScrambleTextEffect on the same text (0.7× scale; the scramble draws a seeded random so the render is reproducible).")

`BridgeHandler` is in `dev.joid.lib.bridge`. The demo UI of fonts (`UIDemoFont`, demo mode) runs these effects, the demo markup, the bundled families and every weight.

```java
final TextInfo info = TextInfo.create(font, 22, Color.WHITE).effects(new WaveTextEffect()).shadow(Color.BLACK);
TextNode.create(0, 0).text(Text.create("the shadow waves with the text", info)).attach(flex);
```

![A waving line of text with a black shadow that follows each glyph](../images/text-effect-wave.gif "The shadow glyphs are copies of the glyphs after apply, so they wave with the text.")

## See also

- [Text and TextInfo](text-and-textinfo.md)
- [Styling Text](styling-text.md)
- [How Fonts Work](../fonts/how-fonts-work.md)
- [Custom Font Implementations](../fonts/custom-fonts.md)
- [Drawing Text](../drawing/text.md)
- [Shapes](../drawing/shapes.md)