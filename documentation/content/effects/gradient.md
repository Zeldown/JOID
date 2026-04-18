# GradientNodeEffect

Applies a linear gradient as post-processing on a node — works on any content, including text and images.

## Apply

```java
node.effect(GradientNodeEffect.create(Color.RED, Color.BLUE));
```

Default direction is horizontal (left to right).

## Custom direction

The direction is a `Vector4f(startX, startY, endX, endY)` in the node's normalized coordinate space (0.0 → 1.0):

```java
// horizontal (default)
GradientNodeEffect.create(Color.RED, Color.BLUE, new Vector4f(0F, 0F, 1F, 0F));

// vertical
GradientNodeEffect.create(Color.CYAN, Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F));

// diagonal
GradientNodeEffect.create(Color.YELLOW, Color.RED, new Vector4f(0F, 0F, 1F, 1F));
```

## Reactive colors

```java
node.effect(GradientNodeEffect.create(
    () -> startColorSignal.get(),
    () -> endColorSignal.get()
));
```

## Use cases

- **Gradient text** without a second shader.
- **Multi-color progress bars** with gradient fills.
- **Themed panels** — subtle color shifts on cards.

## Example — gradient text

```java
TextNode.create(0, 0)
    .text(Text.create("GRADIENT", TextInfo.create(myFont, 48, Color.WHITE)))
    .effect(GradientNodeEffect.create(Color.decode("#3b82f6"), Color.decode("#a855f7")))
    .attach(parent);
```

Behind the scenes, `GradientShaderPass` forces the FBO path so the text is rasterized first, then the gradient is applied as a texture-colored overlay.

## How it works

Produces a `GradientShaderPass` with priority 0 (applied first, before shape/border passes). The shader:

1. Computes the normalized position within the node's canvas.
2. Projects it onto the direction vector to get `t ∈ [0, 1]`.
3. Mixes `startColor` and `endColor` by `t`.
4. Multiplies with the existing pixel (`texColor.rgb * gradientColor.rgb`) if coming from an FBO texture.

## `Color.toGradient()` vs `GradientNodeEffect`

Both create gradients, but differently:

| Approach | When to use |
|---|---|
| `Color.toGradient(otherColor)` | On a `RectNode`'s `color()` — the renderer picks `GradientShaderPass` automatically. |
| `GradientNodeEffect.create(...)` | On any node (text, image, custom) — applies gradient as a **post-process** coloring. |

For `RectNode`, both produce the same visual result. For other nodes, only `GradientNodeEffect` works.

## Best practices

- **Use 2-color gradients.** More than 2 isn't supported directly — chain multiple effects or stack nodes.
- **Pick colors with similar luminosity** unless you want a visible band at the midpoint.
- **Combine with `BorderNodeEffect`** for double gradients (fill + outline).

## See also

- [Effects Overview](overview.md).
- [Color](../drawing/color.md) — `toGradient()` API.
