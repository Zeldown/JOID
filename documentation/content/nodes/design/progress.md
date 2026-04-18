# ProgressNode

A horizontal or vertical progress bar. Good for loading indicators, health bars, and skill gauges.

## Create

```java
ProgressNode.create(x, y, width, height)
    .color(Color.BLUE)
    .progress(0.5F)
    .attach(parent);
```

## API

```java
node.color(Color);                   // fill color
node.color(Color, Color);            // fill + hover
node.color(Color, Color, Color);     // background + fill + hover
node.progress(float);                // 0.0 → 1.0
node.progress(Supplier<Float>);      // reactive
node.vertical(boolean);              // true = bottom-up, default false (horizontal)
node.reverse(boolean);               // invert direction
```

## Reactive progress

```java
final FloatSignal loading = new FloatSignal(0F);

ProgressNode.create(0, 0, 300, 8)
    .color(Color.decode("#1f2937"), Color.decode("#3b82f6"))
    .progress(() -> loading.getOrDefault())
    .watch(loading)
    .effect(RoundedNodeEffect.create(4F))
    .attach(parent);

// Animate loading elsewhere:
TweenAnimator.create(0F).sequence(2000L, 1F).callback(loading::set).start();
```

## Example — skill bar with label

```java
final FloatSignal xp = new FloatSignal(0.73F);

RectNode.create(0, 0, 400, 24)
    .color(Color.decode("#111827"))
    .effect(RoundedNodeEffect.create(12F))
    .body(bar -> {
        ProgressNode.create(2, 2, 396, 20)
            .color(Color.decode("#10b981"))
            .progress(() -> xp.getOrDefault())
            .watch(xp)
            .effect(RoundedNodeEffect.create(10F))
            .attach(bar);

        TextNode.create(bar.dw(2), bar.dh(2))
            .text(() -> Text.create(
                String.format("%.0f%%", xp.getOrDefault() * 100F),
                TextInfo.create(myFont, 14, Color.WHITE),
                Align.CENTER, Align.CENTER
            ))
            .watch(xp)
            .anchor(Align.CENTER)
            .attach(bar);
    })
    .attach(parent);
```

## Best practices

- **Clamp progress.** Negative or >1 values render unexpectedly. Clamp in the supplier: `Math.max(0F, Math.min(1F, value))`.
- **Combine with rounded effects.** A raw rectangle progress bar looks crude; `RoundedNodeEffect` lifts it.

## See also

- [Signals](../../state/signals.md) — `FloatSignal` for reactive progress.
- [TweenAnimator](../../animations/tween-animator.md) — animate progress smoothly.
