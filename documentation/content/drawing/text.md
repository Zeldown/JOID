# Text

`DrawText` is the facade for rendering text outside the node tree. It accepts either a raw `String` + `TextInfo`, or a pre-built `Text` object that can carry multiple runs, alignment, overflow rules, and modifiers. Reach it through `DrawUtils.TEXT` or directly via `DrawText.getInstance()` — both return the same singleton. Every draw method returns a `FontBounds` describing the pixel rectangle the text occupied.

## Drawing plain strings

### `drawText` (plain, aligned)

```java
FontBounds drawText(double x, double y,
                    String text, TextInfo info,
                    Align horizontalAlign, Align verticalAlign)
```

Shortest form. Internally wraps the string in a one-element `Text` before drawing.

```java
DrawUtils.TEXT.drawText(100, 100, "Hello", info, Align.START, Align.START);
```

### `drawText` (plain, inside a box)

```java
FontBounds drawText(double x, double y, double width, double height,
                    String text, TextInfo info,
                    Align horizontalAlign, Align verticalAlign,
                    TextOverflow overflow, TextMode mode)
```

Draws inside the rectangle `(x, y, width, height)` using the chosen `TextMode` and `TextOverflow`. See the [modes](#modes-textmode) and [overflow](#overflow-textoverflow) tables below.

```java
DrawUtils.TEXT.drawText(40, 40, 200, 60, "A very long subtitle", info,
    Align.CENTER, Align.CENTER, TextOverflow.ELLIPSIS, TextMode.OVERFLOW);
```

## Drawing prebuilt `Text`

### `drawText` (builder)

```java
FontBounds drawText(double x, double y, Text text)
FontBounds drawText(double x, double y, double width, double height, Text text, TextMode mode)
```

Use when you already built a `Text` with multiple elements, alignment, or modifiers.

```java
Text line = Text.create()
    .add(TextElement.create("HP: ", info))
    .add(TextElement.create(() -> String.valueOf(hp.get()), info.copy().weight(FontWeight.BOLD)))
    .align(Align.CENTER, Align.CENTER);

DrawUtils.TEXT.drawText(cx, cy, line);
```

## Splitting text

### `getLines`

```java
List<String> getLines(double width, String text, TextInfo info)
List<Text>   getLines(double width, Text text)
```

Splits a text into lines that fit the given pixel width. `\n`, `\r`, `\r\n` and `<br>` each end a line and never appear in it, and a final line break leaves an empty last line. A line as wide as `width` stays whole; a longer one wraps at its last space, which is dropped, or cuts a word wider than the line. The first overload returns plain strings; the second preserves element/info structure for multi-style text, with the modified text and no modifier on each line.

```java
double y = 40;
for (final String line : DrawUtils.TEXT.getLines(300, longText, info)) {
    DrawUtils.TEXT.drawText(20, y, line, info, Align.START, Align.START);
    y += info.getHeight();
}
```

## Modes (`TextMode`)

| Mode | Behaviour |
|---|---|
| `NORMAL` | Draw the text as-is, aligned inside the box. No wrapping, no overflow handling. |
| `OVERFLOW` | If the text exceeds `width`, truncate and append the current `TextOverflow` suffix. A text that fits is drawn whole, without suffix. |
| `SPLIT` | Wrap to multiple lines to fit `width`. No vertical clipping — the text may overflow `height`. |
| `BOX` | Wrap like `SPLIT`, but skip any line that would sit outside `(y, y+height)`. |

## Overflow (`TextOverflow`)

| Value | Suffix |
|---|---|
| `NONE` | *empty string* |
| `ELLIPSIS` | `...` |
| `DOT` | `.` |
| `HYPHEN` | `-` |

Pass it to the drawing call for `OVERFLOW` mode, or bake it into the `Text`.

```java
Text t = Text.create("A very long subtitle", info).overflow(TextOverflow.ELLIPSIS);
DrawUtils.TEXT.drawText(x, y, 200, 40, t, TextMode.OVERFLOW);
```

## The `Text` builder

### `Text.create`

```java
Text create()
Text create(Object text, TextInfo info)
Text create(Supplier<?> text, TextInfo info)
Text create(Object text, TextInfo info, Align horizontalAlign)
Text create(Object text, TextInfo info, Align horizontalAlign, Align verticalAlign)
Text create(Object text, TextInfo info, TextOverflow overflow)
Text create(Object text, TextInfo info, Align align, TextOverflow overflow)
Text create(Object text, TextInfo info, Align horizontal, Align vertical, TextOverflow overflow)
Text create(List<TextElement> elements)
Text create(TextElement... elements)
```

The `Supplier<?>` variants bind a dynamic source of any type — the text and its width and height re-evaluate at draw time without rebuilding the `Text`.

### `text` / `info` (mutation)

```java
Text text(String text)
Text text(Supplier<?> text)
Text text(int index, String text)
Text text(int index, Supplier<?> text)
Text info(TextInfo info)
Text info(int index, TextInfo info)
```

Replaces the text or `TextInfo` on element `0` by default, or on the indexed element. Invalidates the cached width/height.

### `add` / `addAll` / `remove` / `clear`

```java
Text add(TextElement element)
Text add(Text otherText)
Text addAll(List<TextElement> elements)
Text remove(TextElement element)
Text clear()
```

Build the element list incrementally. Each operation invalidates the cached width/height.

### Configuration

```java
Text align(Align horizontal, Align vertical)
Text horizontalAlign(Align align)
Text verticalAlign(Align align)
Text overflow(TextOverflow overflow)
Text modifier(ITextModifier modifier)
```

### Copying

```java
Text copy()
Text copyProperties()
Text copyWithOverflow(TextOverflow overflow)
Text copyWithHorizontalAlign(Align align)
Text copyWithVerticalAlign(Align align)
Text copyWithModifier(ITextModifier modifier)
```

### Reading

```java
String       getRawText()
String       getText()
String       getText(TextElement element)
double       getWidth()
double       getHeight()
double       dw(double value)       // width / value
double       dh(double value)       // height / value
double       aw(double value)       // width + value
double       ah(double value)       // height + value
FontBounds   getBounds()
boolean      isEmpty()
```

`getRawText` returns the unmodified concatenation; `getText` applies the attached modifier (if any).

## `TextElement`

### `TextElement.create`

```java
TextElement create(Object text, TextInfo info)
TextElement create(Supplier<?> text, TextInfo info)
TextElement create(int text, TextInfo info)
TextElement create(double text, TextInfo info)
TextElement create(float text, TextInfo info)
TextElement create(long text, TextInfo info)
TextElement create(char text, TextInfo info)
TextElement create(boolean text, TextInfo info)
```

Factories for a single run. Primitives are converted via `String.valueOf`.

### Fluent setters

```java
TextElement text(Object text)
TextElement text(Supplier<?> text)
TextElement text(int text)
TextElement text(double text)
TextElement text(float text)
TextElement text(long text)
TextElement text(char text)
TextElement text(boolean text)
TextElement info(TextInfo info)
TextElement modifier(ITextModifier modifier)
```

### Copies

```java
TextElement copy()
TextElement copyWithText(Object text)
TextElement copyWithText(Supplier<?> text)
TextElement copyWithInfo(TextInfo info)
TextElement copyWithModifier(ITextModifier modifier)
```

## Modifiers (`ITextModifier`)

A modifier transforms the final string at draw time. Attach it with `text.modifier(...)` or `element.modifier(...)`. On a `Text`, it applies to the joined text of every element: `CAMEL_CASE` on `"hello "` + `"big world"` draws `helloBigWorld`. Built-ins:

| Modifier | Example |
|---|---|
| `TextUpperCaseModifier` | `HELLO` |
| `TextLowerCaseModifier` | `hello` |
| `TextCapitalizeModifier` | `Hello` |
| `TextWordCapitalizeModifier` | `Hello World` |
| `TextCamelCaseModifier` | `helloWorld` |
| `TextUpperCamelCaseModifier` | `HelloWorld` |
| `TextSnakeCaseModifier` | `hello_world` |

Custom modifiers implement `ITextModifier.modify(String)` and return the transformed string.

## See also

- `DrawUtils` — entry point for the four drawing facades.
- `Custom Fonts` — how `TextInfo` is produced from a font provider.
- `TextNode` — node-level wrapper around the same text pipeline.