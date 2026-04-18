# BorderNodeEffect

Outlines a node with a colored (or gradient) border. Respects the shape of the node — corners follow `RoundedNodeEffect`, `CircleNodeEffect`.

## Apply

```java
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
```

2px white border.

## Modes

Two modes control whether the border sits **outside** (default) or **inside** the node bounds:

```java
node.effect(BorderNodeEffect.create(Color.WHITE, 2F, BorderMode.OUT));  // default
node.effect(BorderNodeEffect.create(Color.WHITE, 2F, BorderMode.IN));
```

- **OUT** — the border extends outward from the node's edge; the node itself is unchanged.
- **IN** — the border eats into the node's edge; the visible content shrinks by `width` on all sides.

## Gradient borders

The border color can be a gradient — the renderer interpolates along a vector:

```java
node.effect(BorderNodeEffect.create(
    Color.BLUE.toGradient(Color.MAGENTA),
    3F
));
```

## Reactive color/width

```java
node.effect(BorderNodeEffect.create(
    () -> hovered ? Color.RED : Color.WHITE,
    () -> hovered ? 3F : 1F
));
```

## Example — animated outline

```java
final BooleanSignal active = new BooleanSignal(false);

RectNode.create(0, 0, 200, 100)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(8F))
    .effect(BorderNodeEffect.create(
        () -> active.getOrDefault() ? Color.decode("#3b82f6") : Color.decode("#374151"),
        () -> active.getOrDefault() ? 2F : 1F
    ))
    .watch(active)
    .attach(parent);
```

## How it works

Produces a `BorderShaderPass` with priority 200 (applied after all other effects). The shader samples in 24 directions × 3 distances to detect edges, then blends the border color.

## Best practices

- **Use IN mode for cards/panels** where the border is part of the visual structure (not outside).
- **Use OUT mode for focus rings** — the border appears on top without disturbing layout.
- **Gradient borders pair well with gradient fills** — the two gradients can match or contrast.

## See also

- [Effects Overview](overview.md).
- [Rounded Effect](rounded.md) — border follows rounded corners.
- [Gradient Effect](gradient.md) — color fill gradients.
