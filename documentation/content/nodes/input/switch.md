# SwitchNode

`SwitchNode` holds an ordered list of named states and the current one: segmented controls, previous / next pickers, radio groups. It has no input of its own: your subclass builds its children in `init(UI)`, and these children select a state when clicked.

```java
public class SegmentedSwitchNode extends SwitchNode {

	private static final Color PLACEHOLDER = Color.decode("#DDDDDD");

	private final TextInfo info;

	protected SegmentedSwitchNode(final double x, final double y, final double width, final double height, final TextInfo info) {
		super(x, y, width, height);
		this.info = info;
	}

	public static @NonNull SegmentedSwitchNode create(final double x, final double y, final double width, final double height, final @NonNull TextInfo info) {
		return new SegmentedSwitchNode(x, y, width, height, info);
	}

	@Override
	public void init(final @NonNull UI ui) {
		final double stateWidth = super.getWidth() / super.getStateList().size();
		FlexNode
		.horizontal(0, 0, super.getHeight())
		.body(flex -> {
			for (final String state : super.getStateList().get()) {
				RectNode
				.create(0, 0, stateWidth, super.getHeight())
				.color(Signal.from(() -> super.getState().equals(state) ? Color.WHITE : SegmentedSwitchNode.PLACEHOLDER))
				.body(rect -> {
					TextNode.create(0, 0, stateWidth, super.getHeight()).text(Text.create(state, this.info, Align.CENTER, Align.CENTER)).attach(rect);
				})
				.onClick((node, mouseX, mouseY, button) -> super.state(state))
				.attach(flex);
			}
		})
		.attach(this);
	}

}
```

Then use it (`this.info` is a `TextInfo` built from a loaded font, see [Text and Fonts](../../concepts/text.md)):

```java
SegmentedSwitchNode.create(100, 100, 480, 60, this.info).states("Low", "Medium", "High").attach(this);
```

![The cursor clicks Medium, High, then Low in a three-segment switch: the white segment follows the clicks](../../images/switch-click.gif "Each click calls state(...) on the switch; the segment of the current state turns white.")

Each segment follows `getState()` through `Signal.from(() -> ...)`: the loop variable decides the condition, so a plain expression would stay fixed.

## Defining the states with states

`states(String...)` sets the list; the first state becomes current. `states(Supplier<List<String>>)` follows a list that changes. Each new list removes the children, runs `init(UI)` again and starts on the first state; a change of the current state rebuilds nothing.

```java
private final BooleanSignal expert = BooleanSignal.of(false);

@Override
public void init() {
	SettingCheckboxNode.create(100, 100, 60, 60).signal(this.expert).attach(this);

	SegmentedSwitchNode
	.create(100, 190, 480, 60, this.info)
	.states(this.expert.map(expert -> expert ? Arrays.asList("Low", "Medium", "High", "Ultra") : Arrays.asList("Low", "Medium", "High")))
	.attach(this);
}
```

![Checking Expert adds an Ultra segment to the switch; the cursor selects Ultra, then unchecking Expert removes it and the switch is back on Low](../../images/switch-states.gif "Each new list of states rebuilds the segments and starts on the first state.")

`SettingCheckboxNode` is the subclass written in [CheckboxNode](checkbox.md).

## Selecting with index and state

`index(int)` selects the state at a position, from `0`; `state(String)` selects it by name. Call `states(...)` first: an index out of the list or an unknown name throws `IllegalArgumentException`.

```java
SegmentedSwitchNode.create(100, 100, 480, 60, this.info).states("Low", "Medium", "High").index(2).attach(this);
```

A picker that shows the current state and goes to the next one on each click:

```java
public class CycleSwitchNode extends SwitchNode {

	private final TextInfo info;

	protected CycleSwitchNode(final double x, final double y, final double width, final double height, final TextInfo info) {
		super(x, y, width, height);
		this.info = info;
	}

	public static @NonNull CycleSwitchNode create(final double x, final double y, final double width, final double height, final @NonNull TextInfo info) {
		return new CycleSwitchNode(x, y, width, height, info);
	}

	@Override
	public void init(final @NonNull UI ui) {
		RectNode
		.create(0, 0, super.getWidth(), super.getHeight())
		.color(Color.WHITE)
		.body(rect -> {
			TextNode.create(0, 0, super.getWidth(), super.getHeight()).text(Signal.from(() -> Text.create(super.getState(), this.info, Align.CENTER, Align.CENTER))).attach(rect);
		})
		.onClick((node, mouseX, mouseY, button) -> super.index((super.getStateIndex().peek() + 1) % super.getStateList().size()))
		.attach(this);
	}

}
```

![The cursor clicks a white box three times: Easy becomes Normal, Hard, then Easy again](../../images/switch-cycle.gif "Each click selects the next index, wrapping to the first state.")

## Sharing the state with signal

`signal(Signal<String>)` binds the name of the current state to a [signal](../../concepts/state.md) both ways. Call it after `states(...)`; a name that is not a state is ignored.

```java
private final StringSignal preset = StringSignal.of("High");

@Override
public void init() {
	SegmentedSwitchNode.create(100, 100, 480, 60, this.info).states("Low", "Medium", "High").signal(this.preset).attach(this);

	TextNode.create(100, 190).text(Text.create("Preset: " + this.preset.get(), this.info)).attach(this);
}
```

## Events with onChange

`onChange((node, state) -> ...)` runs after each change of the current state, whatever its source: a click, `index(...)`, `state(...)`, a new list or the bound signal. Cancel the context in the `pre(...)` phase to keep the current state, as shown for [CheckboxNode](checkbox.md).

## Reference

| Method | Description |
|---|---|
| `SwitchNode(x, y, width, height)` | Protected constructor for your subclass. |
| `init(UI)` | Override to build the children; runs again for each new list of states. |
| `states(String...)`, `states(Supplier<List<String>>)` | Sets the states; the first becomes current. |
| `index(int)`, `state(String)` | Selects a state by position or name (also `Supplier` overloads). |
| `signal(Signal<String>)` | Two-way binding of the state name. |
| `onChange(NodeSwitchChangeCallback<T>)` | `(node, state)` after each change. |
| `getState()`, `getStateIndex()`, `getStateList()` | Current name, index signal, list signal. |

## Good to know

- Children added with `body(...)` on the switch are removed at the first change of the list: build them in `init(UI)`.
- Read the state through `Signal.from(() -> ...)`, a lambda or `draw`: a value read once in `init(UI)` stays fixed.

## See also

- Next: [SelectorNode](selector.md)
- [Building a UI Kit](../../components/ui-kit.md)
- [Layout](../../concepts/layout.md)
- [Signals and State](../../concepts/state.md)