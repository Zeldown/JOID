# CheckboxNode

`CheckboxNode` (`dev.joid.lib.ui.node.impl.structure.checkbox`) is a boolean control that flips between checked and unchecked on each click. It is abstract and draws nothing: you subclass it and draw both states. For a two-state control that also carries a value per state, see [`ToggleNode`](toggle.md).

## Creating a checkbox

```java
public class SettingCheckboxNode extends CheckboxNode {

    protected SettingCheckboxNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static SettingCheckboxNode create(final double x, final double y, final double size) {
        return new SettingCheckboxNode(x, y, size, size);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.DARKGRAY);
        if (super.isChecked()) {
            DrawUtils.SHAPE.drawRect(super.getX() + super.getWidth() / 4D, super.getY() + super.getHeight() / 4D, super.getWidth() / 2D, super.getHeight() / 2D, Color.WHITE);
        }
    }

}
```

Then, in `UI.init()`:

```java
SettingCheckboxNode
.create(940, 520, 40)
.checked(true)
.onChange((checkbox, checked) -> System.out.println("Music: " + checked))
.attach(this);
```

![The cursor clicks a gray checkbox twice: the white square disappears, then comes back](../../images/checkbox-click.gif "Each press flips the state; draw() shows the white mark while isChecked() is true (2× scale).")

See [Custom Nodes](../custom-nodes.md) for the constructor and factory contract.

## Clicking

- A mouse press on the checkbox (any button) flips `isChecked()`, then calls `onChange` with the new state. The press is consumed, so nodes under the checkbox do not react to it.
- A press that a node above the checkbox already consumed is ignored: of two overlapping checkboxes, only the topmost flips.
- A disabled or hidden checkbox is never hovered and ignores presses.
- The checkbox reacts on press, not on release, and has no keyboard control.

## Setting the state with checked

`checked(boolean)` sets the state from code without calling `onChange`. A new checkbox is unchecked.

## Binding a signal with signal

`signal(Signal<Boolean>)` keeps the checkbox and a [signal](../../state/signals.md) in sync, both ways:

```java
final BooleanSignal music = new BooleanSignal(true);

SettingCheckboxNode
.create(940, 520, 40)
.signal(music)
.attach(this);
```

- The checkbox starts on the signal's value: here it is checked.
- Each click writes the new state into the signal, before `onChange` runs.
- Each value the signal publishes later sets the state without calling `onChange`, while the checkbox's UI is open.
- `checked(boolean)` does not write the signal.

`BooleanSignal` is in `dev.joid.lib.utils.signal.impl.primitive`.

## onChange

`onChange(NodeCheckboxChangeCallback<T>)` takes `(node, checked)`, where `checked` is the new state; `node.isChecked()` already returns it. Several callbacks run in the order you added them.

Cancelling the context in the `pre(...)` phase of the callback keeps the previous state; the press is still consumed (see [Callbacks](../../interactions/callbacks.md)). The callback interface is in `dev.joid.lib.ui.node.impl.structure.checkbox.callback`.

## Reference

| Method | Default | Description |
| --- | --- | --- |
| `CheckboxNode(double x, double y, double width, double height)` | | Protected constructor for your subclass. |
| `checked(boolean)` | `false` | Sets the state without calling `onChange`. |
| `signal(Signal<Boolean>)` | none | Binds a signal to the state, both ways. |
| `isChecked()` | | Current state. |
| `getSignal()` | | Bound signal, or `null`. |
| `onChange(NodeCheckboxChangeCallback<T>)` | | Adds a callback `(node, checked)` run after each click. |
| `mousePressed(double, double, ClickType, InternalContext)` | | Flips the state. Overridable; call `super.mousePressed(...)` to keep the behavior. |
| `CheckboxNode.CALLBACK_CHANGE` | | Callback id of `onChange`. |

Every setter returns the node itself, typed by the generic return of the fluent API.

## See also

- [ToggleNode](toggle.md)
- [SwitchNode](switch.md)
- [Signals](../../state/signals.md)
- [Callbacks](../../interactions/callbacks.md)
- [Custom Nodes](../custom-nodes.md)