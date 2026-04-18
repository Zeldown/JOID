# SwitchNode

iOS-style on/off switch with a sliding thumb. Same purpose as `CheckboxNode` but visually distinctive — better for "enable/disable this feature" toggles.

## Create

```java
SwitchNode.create(x, y, width, height)
    .value(false)
    .attach(parent);
```

## API

```java
node.value(boolean);
node.value(Supplier<Boolean>);
node.onColor(Color);              // track color when on
node.offColor(Color);              // track color when off
node.thumbColor(Color);
```

## Callbacks

```java
node.onChange((switch_, value) -> {
    System.out.println("On: " + value);
});
```

## Example — dark mode toggle

```java
final BooleanSignal darkMode = new BooleanSignal(true);

SwitchNode.create(0, 0, 56, 28)
    .value(darkMode.getOrDefault())
    .onColor(Color.decode("#3b82f6"))
    .offColor(Color.decode("#374151"))
    .thumbColor(Color.WHITE)
    .effect(RoundedNodeEffect.create(14F))
    .onChange((s, val) -> darkMode.set(val))
    .attach(parent);
```

## Best practices

- **Use for binary settings** that are typically user-facing (notifications, auto-save, dark mode).
- **Prefer `CheckboxNode` for forms** — "I agree to terms" doesn't need a switch.

## See also

- [CheckboxNode](checkbox.md).
- [ToggleNode](toggle.md) — multi-state.
