# RoundedNodeEffect

Rounds the corners of a node. Per-corner control — you can round only top-left and top-right for pill shapes or tabs.

## Apply

```java
node.effect(RoundedNodeEffect.create(16F));
```

All corners at 16px radius.

## Per-corner

```java
node.effect(RoundedNodeEffect.create(16F, true, true, false, false));
// left, top, right, bottom (wait: order is left, top, right, bottom → rounds TOP-LEFT + TOP-RIGHT)
```

Wait — the constructor is `(radius, left, top, right, bottom)` — each boolean enables rounding on that side. Two booleans set to `true` round the corners where both sides meet:

- `left=true, top=true` → top-left corner rounded
- `left=true, bottom=true` → bottom-left
- `right=true, top=true` → top-right
- `right=true, bottom=true` → bottom-right

So `(16F, true, true, false, false)` rounds the **top-left** corner only. For top-rounded tabs:

```java
RoundedNodeEffect.create(16F, true, true, true, false);  // rounds top-left AND top-right
```

## Reactive radius

```java
node.effect(RoundedNodeEffect.create(() -> isSelected ? 20F : 8F));
```

## Modifying after creation

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
rounded.radius(24F);
rounded.left(true);
```

## How it works

`RoundedNodeEffect` produces a `RoundedShaderPass` with priority 100. The pass uses the `RoundedShader` to MSDF-sample a signed distance field, giving crisp corners at any scale.

## Best practices

- **Radius ≤ min(width, height) / 2.** Larger values clip to a pill/circle shape.
- **Round by multiples of 4.** Visual consistency across components.
- **Combine with `BorderNodeEffect`.** The border follows the rounded shape automatically.

## See also

- [Effects Overview](overview.md).
- [Shader Pipeline](../shaders/pipeline.md).
- [CircleNodeEffect](circle.md) — full circle shape.
