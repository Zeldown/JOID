# TextNode

Renders a `Text` object (one or more `TextElement`s) using an MSDF font.

## Create

```java
TextNode.create(x, y)                              // auto-size to the text
TextNode.create(x, y, width, height)               // fixed bounding box
```

When `TextNode.create(x, y)` is used, the node resizes to match the text once the `Text` is attached. With the four-arg form, the bounding box drives wrapping / clipping via the node's `mode(TextMode)`.

## API

```java
T text(Text text)                                  // the Text to render
T mode(TextMode mode)                              // NORMAL / OVERFLOW / SPLIT / BOX (default NORMAL)
T reset()                                          // lock in the current width/height as the initial size
```

There is **no** `text(Supplier<Text>)`, `text(Text, TextOverflow)`, or alignment setter on `TextNode` itself — alignment and overflow strings live on the `Text` builder.

## `TextMode`

| Mode | Behaviour |
|---|---|
| `NORMAL` | Draw as-is, no wrapping. Auto-sizes the node's width/height if they were `0`. |
| `OVERFLOW` | Truncate the text if it exceeds `width`; the node auto-sizes its height. |
| `SPLIT` | Wrap to multiple lines inside `width`; the node grows vertically to fit all lines. |
| `BOX` | Like `SPLIT`, but lines outside the fixed `(y, y + height)` box are dropped. |

## Building the `Text`

```java
final TextInfo info = TextInfo.create(myFont, 20, Color.WHITE);

TextNode.create(40, 40)
    .text(Text.create("Hello, JOID", info))
    .attach(parent);
```

Multiple runs with different `TextInfo` go through `Text.add(TextElement)` — there is no `.append(...)` method on `Text`:

```java
final TextInfo regular = TextInfo.create(myFont, 20, Color.WHITE);
final TextInfo emphasis = TextInfo.create(myFont, 20, Color.decode("#a78bfa")).italic(true);

TextNode.create(40, 40)
    .text(Text.create()
        .add(TextElement.create("Hello, ", regular))
        .add(TextElement.create("JOID", emphasis)))
    .attach(parent);
```

See the [`Text` builder reference](../../drawing/text.md) for every factory, setter, and modifier.

## Alignment

Alignment lives on the `Text`, not the node:

```java
Text.create("Centered", info, Align.CENTER, Align.CENTER);
```

The anchors resolve against the `TextNode`'s bounding box.

## Overflow suffix

`TextOverflow` (`NONE`, `ELLIPSIS`, `DOT`, `HYPHEN`) is also a property of the `Text`, paired with `TextMode.OVERFLOW`:

```java
TextNode.create(40, 40, 200, 40)
    .mode(TextMode.OVERFLOW)
    .text(Text.create("A very long subtitle", info).overflow(TextOverflow.ELLIPSIS))
    .attach(parent);
```

## `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)

T font(IFont font)
T fontSize(float fontSize)
T letterSpacing(float letterSpacing)
T lineHeight(float lineHeight)
T color(Color color)
T colored(boolean colored)
T italic(boolean italic)
T shadow(Color shadowColor)
T shadow()                                         // defaults to color.darker(0.3F)
T shadow(float x, float y)
T copy()
```

`TextInfo` has no `bold(boolean)` setter — ship a bold MSDF atlas as a separate `IFont` if you need bold weight. Use `italic(true)` to toggle italic rendering (requires the font to provide italic glyphs).

## Reactive text

The node's `text(...)` takes a `Text` directly. For reactive updates, pair it with `watch(...)`:

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(0, 0)
    .text(Text.create(() -> "Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

`Text.create(Supplier<Object>, TextInfo)` re-evaluates on each draw, and `.watch(count)` triggers a reload whenever `count` changes.

## Effects

Text plays well with shader effects — gradients and borders work out of the box:

```java
TextNode.create(0, 0)
    .text(Text.create("Gradient", TextInfo.create(font, 48, Color.WHITE)))
    .effect(GradientNodeEffect.create(Color.RED, Color.BLUE))
    .effect(BorderNodeEffect.create(Color.WHITE, 1F))
    .attach(parent);
```

> NOTE. `GradientNodeEffect` on `TextNode` forces the FBO path — the text is rasterised first, then coloured by the gradient shader reading that texture.

## Best practices

- **Cache `TextInfo`.** Creating one per frame churns GC.
- **Prefer `Text.create(Supplier, info)` + `.watch(signal)`** over rebuilding the full UI for text updates.
- **Use `DemoFont.MONTSERRAT` (loaded by `setDemoMode(true)`) during development.** Ship your own MSDF atlas for production — see [Custom Fonts](../../fonts/custom-font.md).

## See also

- [Custom Fonts](../../fonts/custom-font.md)
- [MSDF Atlas](../../fonts/msdf-atlas.md)
- [Drawing / Text](../../drawing/text.md) — `Text`, `TextElement`, modifiers, modes, overflow.
- [Color](../../drawing/color.md)
- [Gradient Effect](../../effects/gradient.md)