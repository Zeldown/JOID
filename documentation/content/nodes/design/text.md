# TextNode

Renders a `Text` object (one or more `TextElement`s) using an MSDF font.

## Create

```java
TextNode.create(x, y)
    .text(Text.create("Hello", TextInfo.create(myFont, 24, Color.WHITE)))
    .attach(parent);
```

`TextNode.create(x, y)` auto-sizes to fit the text. Use `create(x, y, width, height)` to fix a bounding box (text will wrap or be clipped per `TextOverflow`).

## The `Text` builder

`Text` composes of `TextElement`s, each with its own `TextInfo` (font, size, color, bold, italic):

```java
Text.create("Hello")
    .append(" ")
    .append("JOID", TextInfo.create(myFont, 32, Color.RED).bold(true));
```

Default `TextInfo` can be supplied:

```java
final TextInfo info = TextInfo.create(myFont, 20, Color.WHITE);
Text.create("Line 1", info)
    .append("\nLine 2", info);
```

## Alignment

```java
Text.create("Centered", info, Align.CENTER, Align.CENTER);
```

Anchors respect the bounding box of the `TextNode`.

## Text modifiers

Apply case transforms or custom modifiers:

```java
Text.create("hello", info).modifier(new TextUpperCaseModifier());
```

Built-in modifiers:

- `TextUpperCaseModifier`
- `TextLowerCaseModifier`
- `TextCapitalizeModifier`
- `TextWordCapitalizeModifier`
- `TextCamelCaseModifier`
- `TextUpperCamelCaseModifier`
- `TextSnakeCaseModifier`

Implement `ITextModifier` for custom transformations.

## Dynamic text

Use a supplier for reactive text:

```java
TextNode.create(0, 0)
    .text(() -> Text.create("Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

## Effects

Text plays well with shader effects — gradients and borders work out of the box:

```java
TextNode.create(0, 0)
    .text(Text.create("Gradient", TextInfo.create(font, 48, Color.WHITE)))
    .effect(GradientNodeEffect.create(Color.RED, Color.BLUE))
    .effect(BorderNodeEffect.create(Color.WHITE, 1F))
    .attach(parent);
```

> NOTE: `GradientNodeEffect` on `TextNode` forces the FBO path in the shader pipeline — the text is rasterized then gradient-colored through a texture lookup.

## Overflow

```java
node.text(text, TextOverflow.ELLIPSIS);  // truncate with "…"
node.text(text, TextOverflow.CLIP);       // hard clip
node.text(text, TextOverflow.WRAP);       // wrap to next line
```

## Best practices

- **Cache `TextInfo`.** Creating one per frame churns GC.
- **Prefer `Supplier<Text>` + `.watch()` over manual updates.** Reactive text is cheaper than rebuilding the UI.
- **Use `MONTSERRAT` (bundled) during dev.** Swap to your custom MSDF atlas for production — see [Custom Fonts](../../fonts/custom-font.md).

## See also

- [Custom Fonts](../../fonts/custom-font.md)
- [MSDF Atlas](../../fonts/msdf-atlas.md)
- [Color](../../drawing/color.md)
- [Gradient Effect](../../effects/gradient.md)
