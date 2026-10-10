# ToggleNode

`ToggleNode<F, S>` is a two-sided control: a click flips it between its toggled and back sides, and each side carries a value of your choice. Use it for paired settings such as a quality, a frame rate or a language pair. It draws nothing: subclass it once with your look. For a plain boolean, use [CheckboxNode](checkbox.md); for more than two choices, [SwitchNode](switch.md).

```java
public class QualityToggleNode extends ToggleNode<String, String> {

	private static final Color INK = Color.decode("#999999");

	protected QualityToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull QualityToggleNode create(final double x, final double y, final double width, final double height) {
		return new QualityToggleNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final float alpha = super.isEnabled() ? 1F : 0.4F;
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(alpha));
		DrawUtils.SHAPE.drawRect(super.getX() + (super.isToggle() ? super.dw(2) : 0D), super.getY(), super.dw(2), super.getHeight(), QualityToggleNode.INK.copyAlpha(alpha));
	}

}
```

Then use it (`this.info` is a `TextInfo` built from a loaded font, see [Text and Fonts](../../concepts/text.md)):

```java
private final StringSignal quality = StringSignal.of("Low");

@Override
public void init() {
	QualityToggleNode
	.create(100, 100, 120, 60)
	.state("High", "Low")
	.onChange((node, toggle) -> this.quality.set(node.getValue()))
	.attach(this);

	TextNode.create(100, 190).text(Text.create("Quality: " + this.quality.get(), this.info)).attach(this);
}
```

![The cursor clicks a toggle twice: the gray knob jumps right and the text reads Quality: High, then it jumps back and reads Quality: Low](../../images/toggle-click.gif "Each click flips the toggle; onChange writes getValue() into quality, and the text follows.")

A press on the toggle flips it and is consumed; a disabled or hidden toggle ignores presses.

## Side values with state and getValue

`state(F toggle, S back)` gives a value to each side; `getValue()` returns the value of the current side. The two types are free: `ToggleNode<Integer, Integer>` for a frame rate (`state(144, 60)`). `state(...)` does not change the side.

## Initial side with toggle

A new toggle is on its back side; `toggle(true)` puts it on the toggled side. `toggle(this.volume.get() > 50)` follows a signal one way: a click flips it but never writes into the source.

```java
QualityToggleNode.create(100, 100, 120, 60).attach(this);

QualityToggleNode.create(260, 100, 120, 60).toggle(true).attach(this);

QualityToggleNode.create(420, 100, 120, 60).toggle(true).enabled(false).attach(this);
```

![Three toggles: knob on the left, knob on the right, and knob on the right at 40 % opacity](../../images/toggle-states.png "The back side, toggle(true), and toggle(true) with enabled(false).")

## Sharing the side with signal

`signal(Signal<Boolean>)` binds the side to a [signal](../../concepts/state.md) both ways (`true` = toggled):

```java
private final BooleanSignal night = BooleanSignal.of(false);

@Override
public void init() {
	QualityToggleNode.create(100, 100, 120, 60).signal(this.night).attach(this);

	QualityToggleNode.create(240, 100, 120, 60).signal(this.night).attach(this);

	TextNode.create(100, 190).text(Text.create(this.night.get() ? "Night mode" : "Day mode", this.info)).attach(this);
}
```

## Events with onChange

`onChange((node, toggle) -> ...)` runs after each real change of the side, whatever its source: a click, `toggle(...)` or the bound signal. `node.getValue()` already returns the value of the new side. Cancel the context in the `pre(...)` phase to keep the current side, as shown for [CheckboxNode](checkbox.md).

## Reference

| Method | Description |
|---|---|
| `ToggleNode(x, y, width, height)` | Protected constructor for your subclass. |
| `draw(mouseX, mouseY)` | Override to draw; read `isToggle()` and `isEnabled()`. |
| `toggle(boolean)`, `toggle(Supplier<Boolean>)` | Sets the side, or follows a value. Default `false` (back side). |
| `state(F toggle, S back)` | Values of the two sides. |
| `getValue()` | Value of the current side. |
| `signal(Signal<Boolean>)` | Two-way binding with a writable signal. |
| `onChange(NodeToggleChangeCallback<T, F, S>)` | `(node, toggle)` after each change. |
| `isToggle()` | Current side. |

## Good to know

- Call `toggle(...)` and `state(...)` before `Node` setters such as `enabled` in a chain: a `Node` setter returns a `Node`.
- `getValue()` is generic: assign it to the type of the side (`final String value = node.getValue();`).

## See also

- Next: [SwitchNode](switch.md)
- [CheckboxNode](checkbox.md)
- [Building a UI Kit](../../components/ui-kit.md)
- [Signals and State](../../concepts/state.md)