# ProgressNode

A progress bar that fills in one of four directions. Renders either with flat colors (background + foreground) or with paired `Resource` textures.

## Create

```java
ProgressNode.create(x, y, width, height)
    .color(Color.decode("#1f2937"), Color.decode("#3b82f6"))
    .progress(0.5F)
    .attach(parent);
```

## API

```java
T progress(float value)                           // 0.0 → 1.0
T progress(float min, float max, float value)     // value normalized to [0, 1]
T direction(ProgressDirection direction)          // fill direction

T color(Color background, Color foreground)       // both colors at once
T background(Color color)
T foreground(Color color)

T resource(Resource background, Resource foreground)
T background(Resource resource)
T foreground(Resource resource)
```

### `ProgressDirection`

```java
LEFT_TO_RIGHT     // default
RIGHT_TO_LEFT
TOP_TO_BOTTOM
BOTTOM_TO_TOP
```

## Color vs resource rendering

If both `background` and `foreground` resources are set, they're drawn with `DrawUtils.RESOURCE`, the foreground being masked to the current progress fraction (`UI.mask(...)`). Otherwise the node falls back to two `DrawUtils.SHAPE.drawRect` calls with the two colors.

## Reactive progress

`ProgressNode` is a regular `Node`, so bind a signal through `.watch(...)` and update it externally:

```java
final Signal<Float> loading = new Signal<>(0F);

ProgressNode.create(0, 0, 300, 8)
    .color(Color.decode("#1f2937"), Color.decode("#3b82f6"))
    .progress(loading.getOrDefault())
    .watch(loading, (node, value) -> node.progress(value))
    .effect(RoundedNodeEffect.create(4F))
    .attach(parent);
```

## Example — XP bar with label

```java
final Signal<Float> xp = new Signal<>(0.73F);

RectNode.create(0, 0, 400, 24)
    .color(Color.decode("#111827"))
    .effect(RoundedNodeEffect.create(12F))
    .body(bar -> {
        ProgressNode.create(2, 2, 396, 20)
            .color(Color.decode("#0f172a"), Color.decode("#10b981"))
            .progress(xp.getOrDefault())
            .watch(xp, (n, v) -> n.progress(v))
            .effect(RoundedNodeEffect.create(10F))
            .attach(bar);

        TextNode.create(bar.dw(2), bar.dh(2))
            .text(() -> Text.create(
                String.format("%.0f%%", xp.getOrDefault() * 100F),
                TextInfo.create(myFont, 14, Color.WHITE),
                Align.CENTER, Align.CENTER
            ))
            .watch(xp)
            .attach(bar);
    })
    .attach(parent);
```

## See also

- `Signals` — reactive value binding.
- `TweenAnimator` — animating `progress` over time.