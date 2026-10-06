# SwitchNode

`SwitchNode` (`dev.joid.lib.ui.node.impl.structure.sw`) holds an ordered list of named states and the index of the current one, and rebuilds its children each time either changes. It is abstract and has no input handling of its own: you build its children (segments, arrows, labels) in `init(UI)` and change the state from them with `index(...)`. Use it for segmented controls and "previous / next" pickers.

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
            .color(super.getState().equals(state) ? Color.BLUE : Color.DARKGRAY)
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

![The cursor clicks Medium, then High, in a three-segment switch: the blue highlight follows](../../images/switch-click.gif "Each click calls index(state) and the switch rebuilds its segments with the new one highlighted.")

A click on a segment calls `index(state)`: `onChange` runs, the index changes, and the switch rebuilds its segments with the new one highlighted.

See [Custom Nodes](../custom-nodes.md) for the constructor and factory contract.

## Defining the states with state

| Method | States | Current state |
| --- | --- | --- |
| `state(String... stateList)` | The given names, copied | The first one |
| `state(List<String> stateList, int index)` | The given list itself | `stateList.get(index)` |
| `state(List<String> stateList, String state)` | A copy of the list | `state` |

- `state(...)` does not call `onChange`.
- Call `state(...)` before the switch is shown: `getState()` and your `init(UI)` need the list.
- The arguments are checked with `assert` statements only, active when the JVM runs with `-ea`. Without them, an empty list, an index out of range or an unknown state is stored as is and fails later in `getState()`.

## Changing the state with index

| Method | Effect |
| --- | --- |
| `index(int index)` | Selects the state at `index`. |
| `index(String state)` | Selects the state with this name. |

Both run the `onChange` callbacks around the change: `pre(...)` before (cancelling the context there keeps the current state), then the change, then `(node, state)`. They call `onChange` even when the state is already the current one; the switch is then not rebuilt. With a bound signal, the change also writes the name of the new state into it.

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
- Each value the signal publishes later selects that state without calling `onChange`, while the switch's UI is open, and the switch rebuilds. A name that is not one of the states is ignored.
- Call `signal(...)` after `state(...)`: the signal's value is applied once, when you bind it, and only an existing state can be selected. `state(...)` does not write the signal.

## Rebuilding on change

The switch [watches](../../state/watch.md) its two signals, `getStateList()` (`ListSignal<String>`) and `getStateIndex()` (`IntegerSignal`), with `WatchProperty.CLEAR_CHILDREN` and `WatchProperty.RELOAD`. Each time one of them changes once the switch belongs to a UI:

1. every child of the switch is removed;
2. `init(UI)` runs again, so your code adds the children for the new state.

Consequences:

- Build the children in `init(UI)`. Children added with `body(...)` on the switch are removed at the first change and never come back.
- `state(...)` changes both signals, so the switch may rebuild twice.
- Writing the signals directly (for example `getStateIndex().set(2)` or `getStateList().add("Ultra")`) rebuilds the switch without calling `onChange`.
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
        .text(Text.create(super.getState(), this.info, Align.CENTER, Align.CENTER))
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
| `onChange(NodeSwitchChangeCallback<T>)` | Adds a callback `(node, state)` run after `index(...)`. |
| `SwitchNode.CALLBACK_CHANGE` | Callback id of `onChange`. |

Every setter returns the node itself, typed by the generic return of the fluent API. The callback interface is in `dev.joid.lib.ui.node.impl.structure.sw.callback`.

## See also

- [ToggleNode](toggle.md)
- [SelectorNode](selector.md)
- [Watching Signals](../../state/watch.md)
- [Signals](../../state/signals.md)
- [Custom Nodes](../custom-nodes.md)