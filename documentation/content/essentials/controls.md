# Input Controls

JOID ships controls that handle the mouse and the keyboard for you: text fields, checkboxes, toggles, switches, sliders and selectors. Text fields are ready to use; the others bring the behavior and leave the look to you, as the design-neutral idea of the [Introduction](../getting-started/introduction.md) says. This page shows a text field, a checkbox you draw yourself, and how a control binds to a signal. It uses the callbacks of [Input and Callbacks](../concepts/input.md), the signals of [Signals and Reactivity](../concepts/signals.md) and the `TextInfo` of [Text](text.md).

## Text fields

`TextFieldNode` (`dev.joid.lib.ui.node.impl.design.textfield`) is ready to use: it draws its text, cursor and selection, and you give it a background. `info` is the style of its text, a `TextInfo` built from a loaded font as shown in [Text](text.md):

```java
RectNode
.create(760, 515, 400, 50)
.color(Color.WHITE)
.body(rect -> {
	TextFieldNode
	.create(10, 0, 380, 50)
	.info(this.info)
	.placeholder("Search")
	.<TextFieldNode>onChange((field, text, value, valid) -> System.out.println("Search: " + text))
	.onEnter((field, text) -> System.out.println("Submitted: " + text))
	.attach(rect);
})
.attach(this);
```

![The cursor clicks a white search field and types joid docs, then Enter removes the text cursor](../images/ess-input-field.gif "A click focuses the field, typing edits it, Enter validates it and calls onEnter.")

- A click focuses the field and places the cursor where you click; a double click selects a word, a triple click the whole text.
- `onChange` fires on every change of the text, with the raw `text`, the `value` it gives and whether it is `valid`. `accept(text -> ...)` refuses a keystroke that would give an unwanted text.
- Enter validates the text, leaves the field and calls `onEnter`; Escape restores the text from before the focus and leaves the field.
- `<TextFieldNode>` before `onChange` gives the chain its type back, so that `onEnter`, a method of single-line fields only, follows.

## Controls you draw yourself

The other controls handle the input and leave the look to you: you extend them and draw both states in `draw`, which JOID calls every frame. `DrawUtils.SHAPE` (`dev.joid.lib.draw`) draws shapes there, in canvas units: `drawRect(x, y, width, height, color)` fills a rectangle. Inside `draw`, `super.getX()` and `super.getY()` place the drawing on the node. A checkbox, with a protected constructor and a `create` factory like the built-in nodes:

```java
public class SettingCheckboxNode extends CheckboxNode {

	protected SettingCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull SettingCheckboxNode create(final double x, final double y, final double size) {
		return new SettingCheckboxNode(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
		if (super.isChecked()) {
			DrawUtils.SHAPE.drawRect(super.getX() + super.dw(4), super.getY() + super.dh(4), super.dw(2), super.dh(2), Color.GRAY);
		}
	}

}
```

```java
SettingCheckboxNode
.create(940, 520, 40)
.checked(true)
.onChange((checkbox, checked) -> System.out.println("Music: " + checked))
.attach(this);
```

![The cursor clicks a white checkbox twice: the gray square disappears, then comes back](../images/ess-input-checkbox.gif "Each press flips the state and calls onChange.")

| Control | Use it for | Ready to use |
| --- | --- | --- |
| [TextFieldNode](../nodes/input/text-field.md) | A line of text; `IntegerFieldNode` for whole numbers. | Yes |
| [MultilineTextFieldNode](../nodes/input/multiline-text-field.md) | Several lines of text. | Yes |
| [CheckboxNode](../nodes/input/checkbox.md) | On or off. | Extend it |
| [ToggleNode](../nodes/input/toggle.md) | Two states, each with a value. | Extend it |
| [SliderNode](../nodes/input/slider.md) | A value from a range, by dragging a cursor. | Extend it |
| [SwitchNode](../nodes/input/switch.md) | Segmented controls, previous and next pickers. | Extend it |
| [SelectorNode](../nodes/input/selector.md) | A dropdown list. | Extend it |

Each control calls its `onChange` on every real change of its value, whatever its source: a click, a setter or a signal. Every control also binds to a signal with `signal(...)`, shown in the next section. Any node can be dragged with the mouse too: `draggable(DraggableProperty.parent())` keeps it inside its parent node (see [Drag and Drop](../interactions/drag-drop.md)).

## Binding controls with signal

`signal(...)` binds a control to a signal in both directions: the control starts on the value of the signal, writes every change the user makes into it, and shows every change made elsewhere. A boolean signal goes as is to `visible(...)` or `enabled(...)`:

```java
private final BooleanSignal music = BooleanSignal.of(true);
```

```java
SettingCheckboxNode.create(100, 100, 40).signal(this.music).attach(this);

TextNode.create(160, 106).text(Text.create(this.music.get() ? "Music on" : "Music off", this.info)).attach(this);

RectNode.create(100, 180, 300, 120).color(Color.LIGHTGRAY).visible(this.music).attach(this);
```

![Clicking a checkbox turns the text to Music off and hides the panel, clicking again brings both back](../images/ess-state-music.gif "The checkbox writes the signal; the text and the panel follow it.")

`SettingCheckboxNode` is the checkbox of [the previous section](#controls-you-draw-yourself). Every control has `signal(...)`: checkboxes, toggles, switches, sliders, selectors and text fields. A control follows one signal at a time: calling `signal(...)` again replaces the previous one. Its value setters (`checked(...)`, `value(...)`...) follow a signal in one direction only.

## Pitfalls

- A focused text field takes Escape first: the first Escape cancels the edit, the next one closes the UI.
- Tab does not move between fields: the user clicks the next field.
- `signal(...)` needs a signal it can write: a `ComputedSignal` from `map` or `Signal.from` throws an `IllegalArgumentException`; pass it to a setter instead.
- `onChange` is declared on `FieldNode`, the parent class of the text fields: after it, add a witness such as `<TextFieldNode>` to call a method of the field itself, as in the first example.

## See also

- Next: [Saving State](saving-state.md)
- [Component Catalog](../components/overview.md): every control, with its page.
- [TextFieldNode](../nodes/input/text-field.md): accepting, formatting and validating input.
- [CheckboxNode](../nodes/input/checkbox.md): the checkbox API and its states.
- [Building a UI Kit](../components/ui-kit.md): drawing a whole set of controls once.
- [Reactive Properties](../state/reactive-properties.md): one-way setters and two-way `signal(...)` in detail.