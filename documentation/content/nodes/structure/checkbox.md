# CheckboxNode

Two-state checkbox (checked / unchecked) with optional indeterminate state support.

## Create

```java
CheckboxNode.create(x, y, size)
    .value(false)
    .attach(parent);
```

## API

```java
node.value(boolean);
node.value(Supplier<Boolean>);
node.color(Color borderColor, Color checkColor);
node.hoveredColor(Color);
```

## Callbacks

```java
node.onChange((checkbox, value) -> {
    System.out.println("Checked: " + value);
});
```

## Example — preferences row

```java
final BooleanSignal notifications = new BooleanSignal(true);

FlexNode.horizontal(0, 0, 32).margin(12).body(row -> {
    CheckboxNode.create(0, 0, 24)
        .value(notifications.getOrDefault())
        .onChange((cb, val) -> notifications.set(val))
        .attach(row);

    TextNode.create(0, 0)
        .text(Text.create("Enable notifications", info))
        .anchor(Align.CENTER_Y)
        .attach(row);
}).attach(parent);
```

## Best practices

- **Pair with a label.** A checkbox alone is ambiguous — always pair with a `TextNode` in a `FlexNode`.
- **Bind to a `BooleanSignal`** for reactive state.

## See also

- [ToggleNode](toggle.md) — multi-state toggle.
- [SwitchNode](switch.md) — switch-style UI for on/off.
