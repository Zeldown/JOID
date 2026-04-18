# Effects Overview

Effects modify how a node renders. They come in two families, compose in priority order, and can be chained without limit.

## Applying effects

```java
node.effect(RoundedNodeEffect.create(12F));
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
node.effect(BlurNodeEffect.create(4F));
```

Effects are stored in a map keyed by class — adding a second `RoundedNodeEffect` replaces the first.

Remove one:

```java
node.clearEffect(RoundedNodeEffect.class);
```

Get one back (e.g., to read its state):

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
```

## Two families

### Pre / Post effects

Effects that bracket the node's render with `pre(node)` / `post(node)` — typically GL state changes (stencil, scissor, transform). Example: `MaskNodeEffect`, `TransformNodeEffect`.

### Shader effects

Effects that produce one or more `ShaderPass`es, composed through the [Shader Pipeline](../shaders/pipeline.md) with FBO-backed multi-pass rendering. Example: `BlurNodeEffect`, `BorderNodeEffect`, `GradientNodeEffect`, `CircleNodeEffect`, `RoundedNodeEffect`.

Identify a shader effect:

```java
effect.isShaderEffect();   // true / false
```

## Priority

Each effect has a `priority(int)` setter; higher priorities apply later. Shader passes also have internal priorities — see [Shader Pipeline](../shaders/pipeline.md).

```java
myEffect.priority(100);
```

## Built-in effects

| Effect | Family | Use |
|---|---|---|
| [RoundedNodeEffect](rounded.md) | Shader | Round corners |
| [CircleNodeEffect](circle.md) | Shader | Circular mask |
| [BlurNodeEffect](blur.md) | Shader | Gaussian blur |
| [BorderNodeEffect](border.md) | Shader | Outline with gradient support |
| [GradientNodeEffect](gradient.md) | Shader | Linear gradient fill |
| `MaskNodeEffect` | Pre/Post | Stencil mask to a shape |
| `TransformNodeEffect` | Pre/Post | GL translate/scale/rotate |

## Best practices

- **Stack shape + shader effects freely.** A `RectNode` with rounded + border + blur just works.
- **Apply effects on the innermost node possible.** A blur on a whole panel is expensive; on one label, cheap.
- **Measure before optimizing.** Multi-pass blur costs ~1-2ms at 4K on mid-tier GPUs. Don't pre-optimize.

## See also

- [Shader Pipeline](../shaders/pipeline.md) — how effects compose into GL passes.
- [Custom Shaders](../shaders/custom.md) — write your own effect.
