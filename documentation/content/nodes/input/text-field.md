# TextFieldNode

`TextFieldNode` is a single-line text input that handles typing, selection and the clipboard; `IntegerFieldNode` is its sibling for whole numbers. Use [MultilineTextFieldNode](multiline-text-field.md) for several lines.

In the examples, `this.info` is a `TextInfo` built from a loaded font (see [Text and Fonts](../../concepts/text.md)).

```java
private final StringSignal query = StringSignal.of("");

@Override
public void init() {
	RectNode
	.create(760, 515, 400, 50)
	.color(Color.DARKGRAY)
	.body(rect -> {
		TextFieldNode
		.create(10, 0, 380, 50)
		.onEnter((field, text) -> this.query.set(text))
		.info(this.info)
		.placeholder("Search")
		.attach(rect);
	})
	.attach(this);
}
```

![The cursor clicks a gray search field and types a query, then Enter removes the text cursor](../../images/textfield-type.gif "A click focuses the field, typing edits the text, Enter commits and leaves it.")

The field draws no background: put it in a `RectNode` or draw one in a subclass. `info(TextInfo)` is required.

## How input becomes a value

![Diagram: a keystroke builds a new text; accept decides whether it is kept; onChange receives the text, the corrected value and its validity; the commit on Enter or focus loss writes the corrected value back](../../images/diagram-field-input.png "From a keystroke to a committed value")

`accept` decides whether a keystroke is kept; `onChange` receives the text, the corrected value and its validity. The commit (Enter, focus loss) replaces the text by the formatted value.

## Filtering with accept and format

`accept(Predicate<String>)` refuses keystrokes whose resulting text fails; `format(UnaryOperator<String>)` transforms the text at the commit.

```java
TextFieldNode
.create(760, 500, 400)
.format(String::trim)
.info(this.info)
.placeholder("Nickname")
.accept(text -> text.length() <= 12 && text.matches("[A-Za-z ]*"))
.attach(this);
```

![Typing letters and digits in a Nickname field: the digits are refused, the letters are kept](../../images/textfield-accept.gif "accept refuses every keystroke that would make the text invalid.")

## Numbers with IntegerFieldNode

Bounds apply at the commit; the arrows and the wheel add or subtract `step`.

```java
IntegerFieldNode
.create(40, 125, 440, 50)
.min(0)
.max(100)
.value(42)
.step(5)
.info(this.info)
.attach(this);
```

![An integer field: typing 425 shows out of range, the down arrow commits 95, the wheel steps by 5](../../images/integer-field.gif "Bounds apply at the commit; the arrows and the wheel step by 5.")

## Binding a signal

`signal(Signal<V>)` keeps the value and a [signal](../../concepts/state.md) in sync both ways.

```java
private final IntegerSignal amount = IntegerSignal.of(3);

@Override
public void init() {
	IntegerFieldNode.create(40, 40, 320, 50).signal(this.amount).info(this.info).attach(this);

	TextNode.create(40, 120).text(Text.create("Doubled: " + this.amount.get() * 2, this.info)).attach(this);
}
```

## Events with onChange, onEnter and onFocus

| Callback | Lambda | Fired when |
|---|---|---|
| `onChange` | `(field, text, value, valid) -> ...` | Every text change, valid or not; `value` is the corrected value. |
| `onEnter` | `(field, text) -> ...` | Enter commits and leaves the field. |
| `onFocus` | `field -> ...` | The focus changes; read `field.isFocused()`. |

A click elsewhere commits and leaves; Escape restores the previous text. Double click selects a word, triple click everything; Ctrl (Command on macOS) + A, C, X, V work as usual.

![A double click selects a word, dragging extends the selection word by word, a triple click selects the whole text](../../images/textfield-select.gif "Double click selects a word, triple click the whole field.")

## Styling the field

Subclass the field and draw before `super.draw`:

```java
public class SearchFieldNode extends TextFieldNode {

	protected SearchFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull SearchFieldNode create(final double x, final double y, final double width) {
		return new SearchFieldNode(x, y, width, 0D);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
		if (super.isFocused()) {
			DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), Color.GRAY, 2D);
		}

		super.draw(mouseX, mouseY);
	}

}
```

![A white search field that gets a gray border when clicked, then receives text](../../images/textfield-styled.gif "The subclass draws the background, and the border while the field is focused.")

## Reference

Every setter has a value overload and a `Supplier` overload, except `accept` and `format`.

| Method | Default | Description |
|---|---|---|
| `create(x, y, width)`, `create(x, y, width, height)` | | Height from the `TextInfo` plus margins, or given. |
| `info(TextInfo)` | none | Required style of text, placeholder and cursor. |
| `text(String)`, `placeholder(String)` | `""` | Text, and hint shown while empty and unfocused. |
| `accept(Predicate<String>)`, `format(UnaryOperator<String>)` | all, identity | Keystroke filter; transformation at the commit. |
| `maxTextLength(int)`, `allowEmpty(boolean)` | `-1`, `false` | Length cap; empty text valid with a `null` value. |
| `focused(boolean)` | `false` | Focuses or leaves from code. |
| `markup(boolean)` | `false` | Applies the markups of the `TextInfo`. |
| `marginHorizontal(...)`, `marginVertical(...)` | `2`, `10` | Inner padding. |
| `horizontalAlign(Align)`, `verticalAlign(Align)` | `START`, `CENTER` | Alignment while the text fits. |
| `min(int)`, `max(int)`, `step(int)`, `value(int)` | | `IntegerFieldNode` bounds, step (`1`) and value. |
| `signal(Signal<V>)` | | Two-way binding. |
| `getText()`, `getValue()`, `isValid()`, `isFocused()` | | Raw text, corrected value, validity, focus. |

## Good to know

- A shared field setter (`info`, `onChange`) returns a `FieldNode`: call `format`, `onEnter`, `min`, `max` first.
- A focused field consumes every key: UI keybinds wait until it loses the focus.
- The text keeps only characters from U+0020 to U+0233: line breaks, tabs and emojis are dropped.

## See also

- Next: [MultilineTextFieldNode](multiline-text-field.md)
- [Input](../../concepts/input.md)
- [Signals and State](../../concepts/state.md)
- [Text and Fonts](../../concepts/text.md) for markup