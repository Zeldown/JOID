# CircleNodeEffect

Masks the node to a circle inscribed in its bounding box. `min(width, height)` is used as the diameter.

## Apply

```java
node.effect(CircleNodeEffect.create());
```

No parameters — the effect uses the node's current size at render time.

## Use cases

- **Avatars.** `RectNode` with a resource + `CircleNodeEffect`.
- **Buttons.** Circular icon buttons with `BorderNodeEffect` on top.
- **Indicators.** Status dots, notification bubbles.

## Example — avatar with border

```java
RectNode.create(0, 0, 48, 48)
    .color(Color.decode("#3b82f6"))
    .effect(CircleNodeEffect.create())
    .effect(BorderNodeEffect.create(Color.WHITE, 2F))
    .attach(parent);
```

## Example — image avatar

```java
try {
    final Resource avatar = Resource.of(getClass().getResourceAsStream("/avatar.png"));

    RectNode.create(0, 0, 48, 48)
        .effect(CircleNodeEffect.create())
        .body(wrap -> {
            ResourceNode.create(0, 0, 48, 48)
                .resource(avatar)
                .stretch(StretchType.STRETCH)
                .attach(wrap);
        })
        .attach(parent);
} catch (IOException e) { e.printStackTrace(); }
```

The circle effect applies to the container, clipping the nested image.

## How it works

Produces a `CircleShaderPass` with priority 100. The shader computes distance from the center — pixels outside the radius are transparent.

## Best practices

- **Make the parent square** for a perfect circle. A `48×48` rect gives a 48px diameter circle; `48×96` still gives 48px (min dim) but wastes space.
- **Layer border over circle** for a ring effect.

## See also

- [RoundedNodeEffect](rounded.md) — soft corners instead of full circle.
- [CircleNode](../nodes/design/circle.md) — simpler alternative when you just need a filled circle.
