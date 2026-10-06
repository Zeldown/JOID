# ToggleNode

`ToggleNode<F, S>` (`dev.joid.lib.ui.node.impl.structure.toggle`) is a two-state control that flips on each click, like [`CheckboxNode`](checkbox.md), and maps each state to a value: `F` while toggled, `S` otherwise (the "back" side). Use it for on / off switches, theme pickers and any binary choice that you read as a value. It is abstract and draws nothing: you subclass it and draw both states.

## Creating a toggle

```java
public class ThemeToggleNode extends ToggleNode<String, String> {

    protected ThemeToggleNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static ThemeToggleNode create(final double x, final double y, final double width, final double height) {
        return new ThemeToggleNode(x, y, width, height);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.DARKGRAY);
        DrawUtils.SHAPE.drawRect(super.getX() + (super.isToggle() ? super.getWidth() / 2D : 0D), super.getY(), super.getWidth() / 2D, super.getHeight(), Color.WHITE);
    }

}
```

Then, in `UI.init()`:

```java
ThemeToggleNode
.create(860, 500, 200, 50)
.state("dark", "light")
.onChange((toggle, toggled) -> System.out.println("Theme: " + toggle.getValue()))
.attach(this);
```

![The cursor clicks a two-part toggle twice and the white half moves right, then back left](../../images/toggle-click.gif "The white half shows the side: left for light (back), right for dark (toggled).")

The toggle starts on its back side: `isToggle()` is `false` and `getValue()` returns `"light"`. A click flips it to `"dark"`, the next one back to `"light"`.

See [Custom Nodes](../custom-nodes.md) for the constructor and factory contract.

## Values with state and ToggleState

`state(F toggle, S back)` stores the two values in a `ToggleState<F, S>`:

| Side | `isToggle()` | `getValue()` returns |
| --- | --- | --- |
| Toggled | `true` | `getState().getToggle()`, of type `F` |
| Back (initial) | `false` | `getState().getBack()`, of type `S` |

`getValue()` is generic and unchecked: it returns the value as the type you assign it to, so assign it to `F` or `S`. Call `state(...)` before `getValue()`, which otherwise throws a `NullPointerException`.

```java
final int fps = fpsToggle.<Integer>getValue();
```

Here `fpsToggle` is a `ToggleNode<Integer, Integer>` created with `.state(60, 30)`.

`ToggleState<T, B>` is a plain holder with a public constructor `ToggleState(T toggle, B back)` and the getters `getToggle()` and `getBack()`.

## Clicking

- A mouse press on the toggle (any button) flips `isToggle()`, then calls `onChange` with the new side. The press is consumed.
- A press that a node above already consumed is ignored, and a disabled or hidden toggle ignores presses.
- The toggle reacts on press, not on release, and has no keyboard control.
- `toggle(boolean)` sets the side from code without calling `onChange`.

## onChange

`onChange(NodeToggleChangeCallback<T, F, S>)` takes `(node, toggle)`, where `toggle` is the new `isToggle()` value; `node.getValue()` already returns the new value. Cancelling the context in the `pre(...)` phase keeps the previous side (see [Callbacks](../../interactions/callbacks.md)). The callback interface is in `dev.joid.lib.ui.node.impl.structure.toggle.callback`.

## Reference

| Method | Default | Description |
| --- | --- | --- |
| `ToggleNode(double x, double y, double width, double height)` | | Protected constructor for your subclass. |
| `state(F toggle, S back)` | none | Values of the toggled and back sides. |
| `toggle(boolean)` | `false` | Sets the side without calling `onChange`. |
| `isToggle()` | | `true` on the toggled side. |
| `getValue()` | | Value of the current side. |
| `getState()` | | The `ToggleState`, `null` before `state(...)`. |
| `onChange(NodeToggleChangeCallback<T, F, S>)` | | Adds a callback `(node, toggle)` run after each click. |
| `mousePressed(double, double, ClickType, InternalContext)` | | Flips the side. Overridable; call `super.mousePressed(...)` to keep the behavior. |
| `ToggleNode.CALLBACK_CHANGE` | | Callback id of `onChange`. |

Every setter returns the node itself, typed by the generic return of the fluent API.

## See also

- [CheckboxNode](checkbox.md)
- [SwitchNode](switch.md)
- [Callbacks](../../interactions/callbacks.md)
- [Custom Nodes](../custom-nodes.md)