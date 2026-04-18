# CheckboxNode

Two-state checkbox (checked / unchecked). `CheckboxNode` is **abstract** — you subclass it to provide the rendering, while the base class handles the click-to-toggle logic and the change callback.

## Usage

```java
public class MyCheckbox extends CheckboxNode {

    public MyCheckbox(final double x, final double y, final double size) {
        super(x, y, size, size);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 4F);
        if (isChecked()) {
            DrawUtils.SHAPE.drawRoundedRect(getX() + 4, getY() + 4,
                getWidth() - 8, getHeight() - 8, Color.decode("#3b82f6"), 2F);
        }
    }
}
```

Then use it:

```java
new MyCheckbox(0, 0, 24)
    .checked(true)
    .onChange((cb, next) -> System.out.println("checked: " + next))
    .attach(parent);
```

## API

```java
T checked(boolean value)
T onChange(NodeCheckboxChangeCallback<T> callback)

boolean isChecked()
```

The `checked(boolean)` setter and the `onChange` callback are the entire public surface. Everything visual is up to your subclass's `draw()`.

## Behavior

`mousePressed` is overridden on the base class: when the cursor is over the node and the event is not already cancelled, it flips `checked` and fires the change callback with the **new** value.

## Example — preferences row

```java
final BooleanSignal notifications = new BooleanSignal(true);

FlexNode.horizontal(0, 0, 32).margin(12).body(row -> {
    new MyCheckbox(0, 0, 24)
        .checked(notifications.getOrDefault())
        .onChange((cb, val) -> notifications.set(val))
        .attach(row);

    TextNode.create(0, 0)
        .text(Text.create("Enable notifications", info))
        .anchor(Align.START, Align.CENTER)
        .attach(row);
}).attach(parent);
```

## See also

- `ToggleNode` — toggle between two arbitrary values, not booleans.
- `SwitchNode` — multi-state switch.