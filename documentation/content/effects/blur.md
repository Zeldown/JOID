# BlurNodeEffect

Gaussian blur applied to a node's rendered output. Two-pass separable — horizontal then vertical.

## Apply

```java
node.effect(BlurNodeEffect.create(8F));
```

Radius `8F` = 8 logical pixels of blur in each direction.

## Reactive radius

```java
node.effect(BlurNodeEffect.create(() -> isFocused ? 0F : 12F));
```

Or mutate at runtime:

```java
node.getEffect(BlurNodeEffect.class).radius(20F);
```

## Use cases

- **Background blur** behind a dialog.
- **Focus depth** — blur unfocused areas.
- **Loading state** on a panel.

## Example — blur when not focused

```java
final BooleanSignal focused = new BooleanSignal(false);

RectNode.create(0, 0, 600, 400)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(12F))
    .effect(BlurNodeEffect.create(() -> focused.getOrDefault() ? 0F : 6F))
    .watch(focused)
    .attach(parent);
```

## How it works

Produces two `BlurShaderPass` instances — horizontal (priority 150) + vertical (priority 151). Each pass is a full framebuffer blit with a 65-sample dynamic-step Gaussian kernel.

## Performance

Blur is the most expensive built-in effect. Cost scales with:

- **Node size** — larger node = more pixels to blur.
- **Radius** — dynamically adjusts sample count (`step = max(radius / 32, 1)`). Very large radii (>64) stay fast but lose precision.
- **Scale factor** — JOID's shader pipeline supersamples based on `viewportWidth / uiWidth`. On 4K displays with a 1920×1080 UI, pixel cost is 4×.

Benchmarks (RTX 3060, 1080p):
- 800×600 node, radius 8 : ~0.3ms
- 800×600 node, radius 32 : ~0.6ms
- 1920×1080 node, radius 8 : ~0.9ms

## Best practices

- **Blur the smallest possible region.** Don't blur the whole UI; blur the dialog background.
- **Radius ≤ 16** for real-time feedback. Higher radii are fine for static scenes.
- **Combine with `RoundedNodeEffect`** — the blur respects the rounded bounds thanks to premultiplied alpha handling.

## See also

- [Effects Overview](overview.md).
- [Shader Pipeline](../shaders/pipeline.md) — how multi-pass FBO works.
