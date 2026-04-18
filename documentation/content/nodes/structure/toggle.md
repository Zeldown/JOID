# ToggleNode

Multi-state toggle. Cycles through N states on click. Use `ToggleState` for each option.

## Create

```java
ToggleNode.create(x, y, width, height)
    .states(
        ToggleState.of("low"),
        ToggleState.of("medium"),
        ToggleState.of("high")
    )
    .value("medium")
    .attach(parent);
```

## API

```java
node.states(ToggleState... states);
node.value(String);              // current state id
node.value(Supplier<String>);
node.backgroundColor(Color);
node.activeColor(Color);
```

## Callbacks

```java
node.onChange((toggle, state) -> {
    System.out.println("Active: " + state.id());
});
```

## Example — graphics quality

```java
final StringSignal quality = new StringSignal("high");

ToggleNode.create(0, 0, 300, 40)
    .states(
        ToggleState.of("low").label("Low"),
        ToggleState.of("medium").label("Medium"),
        ToggleState.of("high").label("High"),
        ToggleState.of("ultra").label("Ultra")
    )
    .value(quality.getOrDefault())
    .onChange((t, s) -> quality.set(s.id()))
    .effect(RoundedNodeEffect.create(20F))
    .attach(parent);
```

## Best practices

- **Use stable ids.** `ToggleState.of("low")` — not `of("Low")` — so the id is independent of user-facing label.
- **Limit to 3–5 states.** More than that, use a `SelectorNode` dropdown.

## See also

- [CheckboxNode](checkbox.md) — binary toggle.
- [SwitchNode](switch.md) — switch-style UI.
- [SelectorNode](selector.md) — dropdown for many options.
