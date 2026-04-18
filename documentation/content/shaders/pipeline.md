# Shader Pipeline

JOID's shader pipeline composes multi-pass GL effects on top of a node's rendered output. It handles framebuffer pooling, projection, blending, and nested expansion — you just provide a list of `ShaderPass` objects.

## The `ShaderPipeline` class

```java
ShaderPipeline.render(node, passes, baseDraw);
```

- `node` — the node being rendered (supplies position and size).
- `passes` — `List<ShaderPass>` — sorted by priority, executed in order.
- `baseDraw` — a `Runnable` that paints the raw content (the node's geometry).

Alternate forms when you don't have a `Node`:

```java
ShaderPipeline.render(x, y, width, height, passes, baseDraw);
ShaderPipeline.render(x, y, width, height, baseDraw, pass1, pass2, ...);
```

## How it works

1. **Sort passes by `priority()`** — low first.
2. **Fast path** — if exactly 1 pass, no expansion, and `supportsDirectBind()` is true, bind the shader directly and draw. No FBO.
3. **Multi-pass path**:
   - Render `baseDraw` into FBO A (sized for the node + max expansion).
   - For each pass except the last: bind pass shader → blit FBO A onto FBO B using that shader → swap A and B.
   - Bind the last pass → blit onto the screen → unbind.
4. FBO pool is keyed by `(pipelineDepth, width, height)` so nested calls get separate framebuffers.

## Writing a pass

A pass implements `ShaderPass`:

```java
public class MyShaderPass implements ShaderPass {

    @Override
    public void bindDirect(Node node) {
        // Bind your shader for the simple path (no intermediate texture).
    }

    @Override
    public void bindForTexture(Node node) {
        // Bind your shader knowing the input will be the previous FBO's texture.
    }

    @Override
    public void unbind() {
        MyShader.inst().unbind();
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public float expansion() {
        return 0F;  // or `radius` for blur, `width` for border
    }

    @Override
    public boolean supportsDirectBind() {
        return true;  // false = force FBO path (required if baseDraw swaps shaders, like text)
    }
}
```

## Priority cheat-sheet

Built-in passes:

| Pass | Priority |
|---|---|
| `GradientShaderPass` | 0 |
| `RoundedShaderPass` | 100 |
| `CircleShaderPass` | 100 |
| `BlurShaderPass` (horizontal) | 150, 152, … |
| `BlurShaderPass` (vertical) | 151, 153, … |
| `BorderShaderPass` | 200 |

Pick priorities that respect this order when writing custom passes:
- Color / gradient first.
- Shape masking next.
- Blur after.
- Outline last.

## Expansion

When a pass like `BlurShaderPass` or `BorderShaderPass` returns `expansion() > 0`, the FBO is enlarged on all sides by that amount. This prevents clipping near the edges of the node.

## Cleanup

Call at app shutdown:

```java
ShaderPipeline.cleanup();
```

Frees pooled framebuffers. The `VideoAudioPlayer` shutdown hook also fires; wire your own for the shader pipeline if embedding JOID in a long-running host.

## Best practices

- **Don't fight the system.** `effect(...)` on a node builds the pass list automatically; use it before reaching for `ShaderPipeline.render(x, y, w, h, ...)` directly.
- **`supportsDirectBind() = false`** is a must for passes that need to read the framebuffer (e.g., gradients over text).
- **Keep expansion tight.** Over-expanding wastes pixels.

## See also

- [Effects Overview](../effects/overview.md).
- [Custom Shaders](custom.md) — writing your own pass.
