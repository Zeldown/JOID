# CheckboxNode

`CheckboxNode` holds a boolean that a click flips. It draws nothing: you subclass it once with your look and reuse the class everywhere. For two sides that carry a value each, use [ToggleNode](toggle.md).

```java
public class SettingCheckboxNode extends CheckboxNode {

	private static final Color INK = Color.decode("#999999");

	protected SettingCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull SettingCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new SettingCheckboxNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final float alpha = super.isEnabled() ? 1F : 0.4F;
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(alpha));
		if (super.isChecked()) {
			DrawUtils.SHAPE.drawRect(super.getX() + super.dw(4), super.getY() + super.dh(4), super.dw(2), super.dh(2), SettingCheckboxNode.INK.copyAlpha(alpha));
		}
	}

}
```

Then use it:

```java
SettingCheckboxNode.create(100, 100, 60, 60).attach(this);
```

![The cursor clicks a white checkbox twice: a gray square appears, then disappears](../../images/checkbox-click.gif "Each click flips the state; draw() shows the mark while isChecked() is true.")

A press on the checkbox flips the state; the press is consumed. A disabled or hidden checkbox ignores presses.

## Initial state with checked

A new checkbox is unchecked. `checked(true)` sets the state; draw the disabled look yourself from `isEnabled()`.

```java
SettingCheckboxNode.create(100, 100, 60, 60).attach(this);

SettingCheckboxNode.create(200, 100, 60, 60).checked(true).attach(this);

SettingCheckboxNode.create(300, 100, 60, 60).checked(true).enabled(false).attach(this);
```

![Three checkboxes: empty, checked, and checked at 40% opacity](../../images/checkbox-states.png "Unchecked, checked(true), and checked with enabled(false).")

## Binding a signal with signal

`signal(Signal<Boolean>)` shares the state with a [signal](../../concepts/state.md) both ways: two checkboxes bound to the same signal stay in sync. `checked(this.music.get())` follows a signal one way only.

```java
private final BooleanSignal music = BooleanSignal.of(true);

@Override
public void init() {
	SettingCheckboxNode.create(100, 100, 60, 60).signal(this.music).attach(this);

	SettingCheckboxNode.create(180, 100, 60, 60).signal(this.music).attach(this);
}
```

![Two checkboxes bound to one signal: clicking either flips both](../../images/checkbox-signal.gif "Both checkboxes share the music signal.")

## Events with onChange

`onChange((node, checked) -> ...)` runs after each real change of the state, whatever its source: a click, `checked(...)` or the bound signal.

![Diagram: a click, a setter or a bound signal gives a new value; an equal value stops there; PRE callbacks can cancel; then the value is stored and the signal written; then the POST callbacks run onChange](../../images/diagram-control-change.png "How every control of this family applies a new value.")

The callback also has a `pre(...)` phase that runs before the change: cancel its context to refuse it. Write it with an anonymous class; this checkbox, once checked, stays checked:

```java
final BooleanSignal accepted = BooleanSignal.of(false);

TextNode.create(180, 115).text(Text.create(accepted.get() ? "Terms accepted" : "Accept the terms", this.info)).attach(this);

SettingCheckboxNode
.create(100, 100, 60, 60)
.onChange(new NodeCheckboxChangeCallback<SettingCheckboxNode>() {

	@Override
	public void apply(final @NonNull SettingCheckboxNode node, final boolean checked) {
		accepted.set(checked);
	}

	@Override
	public void pre(final @NonNull SettingCheckboxNode node, final @NonNull DispatchContext context, final boolean checked) {
		if (!checked) {
			context.cancel();
		}
	}

})
.attach(this);
```

`this.info` is a `TextInfo` built from a loaded font (see [Text and Fonts](../../concepts/text.md)).

## Reference

| Method | Description |
|---|---|
| `CheckboxNode(x, y, width, height)` | Protected constructor for your subclass. |
| `draw(mouseX, mouseY)` | Override to draw; read `isChecked()` and `isEnabled()`. |
| `checked(boolean)`, `checked(Supplier<Boolean>)` | Sets the state, or follows a value. Default `false`. |
| `signal(Signal<Boolean>)` | Two-way binding with a writable signal. |
| `onChange(NodeCheckboxChangeCallback<T>)` | `(node, checked)` after each change. |
| `isChecked()` | Current state. |

## Good to know

- Call `checked(...)` and `signal(...)` before `Node` setters such as `enabled` in a chain: a `Node` setter returns a `Node`.
- Setting the current state again runs no callback.

## See also

- Next: [ToggleNode](toggle.md)
- [Building a UI Kit](../../components/ui-kit.md)
- [Input](../../concepts/input.md)
- [Signals and State](../../concepts/state.md)