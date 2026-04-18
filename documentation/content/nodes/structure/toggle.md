# ToggleNode

A two-value toggle backed by a `ToggleState<F, S>` pair. `ToggleNode` is **abstract** — you subclass it to draw the two visual states, while the base class handles click-to-flip and exposes the currently selected value.

Unlike `CheckboxNode` (boolean only), `ToggleNode` lets the two sides carry arbitrary values. Typical use: `ToggleState<String, String>` for a text pair, `ToggleState<Enum, Enum>` for two modes.

## Usage

```java
public class MyToggle extends ToggleNode<String, String> {

    public MyToggle(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            isToggle() ? Color.decode("#3b82f6") : Color.decode("#374151"), 6F);
    }
}
```

Then use it:

```java
new MyToggle(0, 0, 80, 32)
    .state("ON", "OFF")
    .toggle(false)
    .onChange((node, next) -> System.out.println("flipped, now toggle=" + next))
    .attach(parent);
```

## API

```java
T state(F toggle, S back)          // the two values the toggle carries
T toggle(boolean value)             // true → value is the "toggle" (F) side

<V> V getValue()                   // returns F if toggle, otherwise S
boolean isToggle()
ToggleState<F, S> getState()

T onChange(NodeToggleChangeCallback<T, F, S> callback)
```

The generic parameters `F` and `S` are the types of the two sides. The change callback receives the node and the **new** boolean toggle state.

## Behavior

`mousePressed` is overridden: when the cursor is over the node, the toggle flips and the callback fires with the new boolean. The associated value can be read with `getValue()` inside the callback.

## Example — draft / preview

```java
public class DraftPreviewToggle extends ToggleNode<String, String> { ... }

new DraftPreviewToggle(0, 0, 160, 36)
    .state("Draft", "Preview")
    .toggle(true)
    .onChange((node, next) -> {
        String current = node.getValue();
        System.out.println("switched to " + current);
    })
    .attach(parent);
```

## See also

- `CheckboxNode` — boolean only.
- `SwitchNode` — cycle through N string states.