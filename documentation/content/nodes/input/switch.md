# SwitchNode

`SwitchNode` (`dev.joid.lib.ui.node.impl.structure.sw`) holds an ordered list of named states and the index of the current one. It is abstract and has no input handling of its own: you build its children (segments, arrows, labels) in `init(UI)`, once, have them draw the current state, and change the state from them with `index(...)`. The children are rebuilt only when the list of states changes. Use it for segmented controls and "previous / next" pickers.

## Creating a segmented switch

```java
public class SegmentedSwitchNode extends SwitchNode {

    private final TextInfo info;

    protected SegmentedSwitchNode(final double x, final double y, final double width, final double height, final TextInfo info) {
        super(x, y, width, height);
        this.info = info;
    }

    public static SegmentedSwitchNode create(final double x, final double y, final double width, final double height, final TextInfo info) {
        return new SegmentedSwitchNode(x, y, width, height, info);
    }

    @Override
    public void init(final UI ui) {
        final double stateWidth = super.getWidth() / super.getStateList().size();
        int index = 0;
        for (final String state : super.getStateList().getOrDefault()) {
            RectNode
            .create(index * stateWidth, 0, stateWidth, super.getHeight())
            .color(() -> super.getState().equals(state) ? Color.BLUE : Color.DARKGRAY)
            .body(rect -> {
                TextNode
                .create(0, 0, stateWidth, super.getHeight())
                .text(Text.create(state, this.info, Align.CENTER, Align.CENTER))
                .attach(rect);
            })
            .onClick((node, mouseX, mouseY, clickType) -> super.index(state))
            .attach(this);
            index++;
        }
    }

}
```

Then, in `UI.init()` (`font` is an `IFont` you loaded):

```java
SegmentedSwitchNode
.create(760, 500, 400, 50, TextInfo.create(font, 20F, Color.WHITE))
.state("Low", "Medium", "High")
.onChange((node, state) -> System.out.println("Preset: " + state))
.attach(this);
```

![The cursor clicks Medium, then High, in a three-segment switch: the blue highlight follows](../../images/switch-click.gif "Each click calls index(state) and the segment of the new state turns blue.")

A click on a segment calls `index(state)`: `onChange` runs and the index changes. The segments stay the same nodes: each one picks its color through a `Supplier`, read on every frame, so the new current segment turns blue.

See [Custom Nodes](../custom-nodes.md) for the constructor and factory contract.

## Defining the states with state

| Method | States | Current state |
| --- | --- | --- |
| `state(String... stateList)` | The given names, copied | The first one |
| `state(List<String> stateList, int index)` | A copy of the list | `stateList.get(index)` |
| `state(List<String> stateList, String state)` | A copy of the list | `state` |

- `state(...)` calls `onChange` when the selected state name changes.
- Call `state(...)` before the switch is shown: `getState()` and your `init(UI)` need the list.
- An empty list, an index out of the list or a state that is not in the list throws an `IllegalArgumentException`.
- The switch keeps its own copy: changing your list afterwards does not change the states.

## Changing the state with index

| Method | Effect |
| --- | --- |
| `index(int index)` | Selects the state at `index`. |
| `index(String state)` | Selects the state with this name. |

Both run the `onChange` callbacks around the change: `pre(...)` before (cancelling the context there keeps the current state), then the change, then `(node, state)`. Selecting the current state again calls nothing. With a bound signal, the change also writes the name of the new state into it. An index out of the list or an unknown state throws an `IllegalArgumentException`, as does `index(...)` before `state(...)`.

## Binding a signal with signal

`signal(Signal<String>)` keeps the current state and a [signal](../../state/signals.md) of state names in sync, both ways:

```java
private final StringSignal preset = new StringSignal("High");
```

```java
SegmentedSwitchNode
.create(760, 500, 400, 50, TextInfo.create(font, 20F, Color.WHITE))
.state("Low", "Medium", "High")
.signal(this.preset)
.attach(this);
```

- The switch starts on the signal's value: here "High".
- Each `index(...)` writes the name of the new state into the signal, before `(node, state)` runs.
- Each value the signal publishes later selects that state through `index(...)`, while the switch's UI is open. A name that is not one of the states is ignored.
- Call `signal(...)` after `state(...)`: the signal's value is applied once, when you bind it, and only an existing state can be selected. `state(...)` and `index(...)` write the signal and call `onChange` when the state changes; selecting the current state again does nothing.

## Building the children

`init(UI)` runs when the switch loads. The switch then [watches](../../state/watch.md) its list of states, `getStateList()` (`ListSignal<String>`), with `WatchProperty.CLEAR_CHILDREN` and `WatchProperty.RELOAD`. Each time the list changes once the switch belongs to a UI:

1. every child of the switch is removed;
2. `init(UI)` runs again, so your code adds the children for the new states.

A change of the current state does not rebuild anything: the children stay the same nodes, so they keep their hover and their animations, and can animate from one state to the next. Consequences:

- Build the children in `init(UI)`, and have them read `getState()` when they draw: a `Supplier` (`color(() -> ...)`, `Text.create(() -> ..., info)`) or a node whose `draw` reads the switch. A value read once in `init(UI)` does not follow the state.
- Children added with `body(...)` on the switch are removed at the first change of the list and never come back.
- `state(...)` with the same states as before does not rebuild.
- Writing the signals directly (for example `getStateIndex().set(2)` or `getStateList().add("Ultra")`) does not call `onChange`; adding a state rebuilds the switch.
- Create the switch in `UI.init()`, as for any node.

## Reading the state

- `getState()` returns the name of the current state.
- `getStateIndex().getOrDefault()` returns its index.
- `getStateList().getOrDefault()` returns the list of names.

An `init(UI)` that shows only the current state and moves to the next one on each click:

```java
@Override
public void init(final UI ui) {
    RectNode
    .create(0, 0, super.getWidth(), super.getHeight())
    .color(Color.DARKGRAY)
    .body(rect -> {
        TextNode
        .create(0, 0, super.getWidth(), super.getHeight())
        .text(Text.create(() -> super.getState(), this.info, Align.CENTER, Align.CENTER))
        .attach(rect);
    })
    .onClick((node, mouseX, mouseY, clickType) -> super.index((super.getStateIndex().getOrDefault() + 1) % super.getStateList().size()))
    .attach(this);
}
```

## Reference

| Method | Description |
| --- | --- |
| `SwitchNode(double x, double y, double width, double height)` | Protected constructor for your subclass. Starts with no states and index `0`. |
| `state(String... stateList)` | Sets the states; the first one is current. |
| `state(List<String> stateList, int index)` | Sets the states and the current index. |
| `state(List<String> stateList, String state)` | Sets the states and the current state. |
| `index(int index)` | Selects a state by index and calls `onChange`. |
| `index(String state)` | Selects a state by name and calls `onChange`. |
| `signal(Signal<String> signal)` | Binds a signal to the name of the current state, both ways. |
| `getState()` | Name of the current state. |
| `getSignal()` | Bound signal, or `null`. |
| `getStateList()` | `ListSignal<String>` of the state names. |
| `getStateIndex()` | `IntegerSignal` of the current index. |
| `onChange(NodeSwitchChangeCallback<T>)` | Adds a callback `(node, state)` run after each change of the current state. |
| `SwitchNode.CALLBACK_CHANGE` | Callback id of `onChange`. |

Every setter returns the node itself, typed by the generic return of the fluent API. The callback interface is in `dev.joid.lib.ui.node.impl.structure.sw.callback`.

## See also

- [ToggleNode](toggle.md)
- [SelectorNode](selector.md)
- [Watching Signals](../../state/watch.md)
- [Signals](../../state/signals.md)
- [Custom Nodes](../custom-nodes.md)