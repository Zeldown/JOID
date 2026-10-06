# Effects

A node effect (`NodeEffect`, package `dev.joid.lib.ui.node.effect`) changes how a node is rendered without changing the node itself: rounded corners, circular cut, border, blur, shadow, mask, transform. Use effects to style any node, built-in or custom, with the same few lines.

```java
@Override
public void init() {
    RectNode
    .create(100, 100, 300, 200)
    .color(Color.WHITE)
    .effect(RoundedNodeEffect.create(16F))
    .effect(BorderNodeEffect.create(Color.BLACK, 2F))
    .attach(this);
}
```

![A white rounded rectangle with a thin black border](../images/effects-basic.png "Two effects stacked on one RectNode: rounded corners and a border that follows them.")

The built-in effects are in `dev.joid.lib.ui.node.effect.impl`:

| Effect | Kind | What it does |
| --- | --- | --- |
| [RoundedNodeEffect](rounded.md) | Shader | Rounds the corners. |
| [CircleNodeEffect](circle.md) | Shader | Cuts the largest centered circle. |
| [BorderNodeEffect](border.md) | Shader | Draws an outline along the edge of the drawn pixels. |
| [BlurNodeEffect](blur.md) | Shader | Gaussian blur of the node's own rendering. |
| [ShadowNodeEffect](shadow.md) | Render state | Draws a soft shadow or a glow under the node. |
| [MaskNodeEffect](mask.md) | Render state | Clips the rendering to a rectangle or to the shape of an image. |
| [TransformNodeEffect](transform.md) | Render state | Translates, scales or rotates the rendering. |

## Applying effects with effect

`Node.effect(...)` adds an effect and returns the node, so it chains like any other setter. It has two overloads:

| Method | Description |
| --- | --- |
| `effect(NodeEffect<? super T, ?> effect)` | Adds `effect`. The effect is typed by the node (an effect declared for `RectNode` on a `RectNode`) or by one of its parent classes (`Node`, as the built-in effects). |
| `effect(Function<T, NodeEffect<? super T, ?>> factory)` | Calls `factory` immediately with the node and adds the effect it returns. Use it when the effect needs the node. |

```java
RectNode
.create(100, 100, 300, 200)
.color(Color.WHITE)
.effect(node -> RoundedNodeEffect.create(() -> node.hoverValue(24F)))
.effect(node -> BorderNodeEffect.create(Color.BLACK, 2F).fill(false))
.attach(this);
```

The setters of the effects (`radius`, `fill`, `color`, `priority`, `scope`...) return the effect, so a configured effect goes straight into `effect(...)`, without the function form: `.effect(BorderNodeEffect.create(Color.BLACK, 2F).fill(false))`.

A node holds at most one effect per class: adding an effect whose class is already present replaces the previous one. Different classes stack freely.

## Reading and removing effects

| Method | Description |
| --- | --- |
| `getEffect(Class<T> type)` | The effect of that exact class, as an `Optional`, empty when the node has none. |
| `hasEffect(Class<?> type)` | Whether the node has an effect of that exact class. |
| `removeEffect(Class<?> type)` | Removes the effect of that class. Returns the node. |
| `clearEffects()` | Removes every effect. Returns the node. |

Pass the class literal of the effect, built-in effects included, without cast:

```java
final RectNode card = RectNode.create(100, 100, 300, 200).color(Color.WHITE).effect(RoundedNodeEffect.create(16F)).effect(BorderNodeEffect.create(Color.BLACK, 2F));

card.getEffect(RoundedNodeEffect.class).ifPresent(rounded -> rounded.radius(8F));

if (card.hasEffect(BorderNodeEffect.class)) {
    card.removeEffect(BorderNodeEffect.class);
}
```

To keep the effect, type the `Optional`: `final Optional<RoundedNodeEffect<Node>> rounded = card.getEffect(RoundedNodeEffect.class);`.

`Node.copy()` gives the copy the same effect instances as the original.

## Dynamic values with suppliers

The values of the built-in effects (radius, sides, color, width, mask bounds, transformation) also accept a `Supplier`, read again each frame. Bind them to the hover progress, a signal, an animator or any state:

```java
RectNode
.create(100, 100, 300, 200)
.color(Color.WHITE)
.effect(node -> BorderNodeEffect.create(Color.BLACK, 1F).color(() -> Color.BLACK.to(Color.BLUE, node.hoverValue(1F))).width(() -> 1F + node.hoverValue(2F)))
.attach(this);
```

![The cursor hovers a white rectangle whose border thickens and turns blue](../images/effects-hover.gif "The border color and width follow the hover progress.")

## Effect order and priority

Every effect has a priority (`priority(int)`, default `0`). The node runs its effects by ascending priority; effects of equal priority keep the order in which they were added, and a replaced effect keeps its place. The node sorts its effects again on every frame, so a priority changed after `effect(...)` applies from the next frame.

Effects come in two kinds, rendered differently:

| Kind | Effects | How it renders |
| --- | --- | --- |
| Render state | `ShadowNodeEffect`, `MaskNodeEffect`, `TransformNodeEffect` | `pre(...)` runs before the node renders, in priority order, and `post(...)` after, in the reverse order, so the effects nest. They wrap the whole render of the node: its own drawing, its children and the shader effects. |
| Shader | `RoundedNodeEffect`, `CircleNodeEffect`, `BorderNodeEffect`, `BlurNodeEffect` | Each effect produces shader passes. The node is drawn into an offscreen framebuffer, then the passes run one after the other on the result. |

Between render-state effects, the order matters. A `TransformNodeEffect` that runs before a `MaskNodeEffect` transforms the mask with the node; a mask that runs first stays in place while the content moves under it:

```java
final MaskNodeEffect<Node> mask = MaskNodeEffect.create(300D, 100D);
mask.priority(1);

RectNode
.create(100, 100, 300, 200)
.color(Color.WHITE)
.effect(mask)
.effect(TransformNodeEffect.create(new RotateOperation(10D, Rotation.ROLL, Vector.create(250D, 200D))))
.attach(this);
```

![Left: a tilted white band, the mask turned with the card; right: the mask stays horizontal and cuts the tilted card](../images/effects-order.png "Left, the snippet (mask priority 1, after the transform); right, the same card with mask.priority(-1), so the mask runs before the transform.")

Here the transform (priority `0`) runs first although it was added last, so the mask turns with the card.

The shader passes run in the order of their own pass priority, not of the effect priority:

| Pass priority | Effect |
| --- | --- |
| 100 | `RoundedNodeEffect`, `CircleNodeEffect` |
| 150, 151 | `BlurNodeEffect` (horizontal, then vertical) |
| 200 | `BorderNodeEffect` |

So the shape is cut first, then blurred, then outlined: a border always follows rounded corners, whatever order you add the effects in. The effect priority only decides between passes of the same pass priority.

## Scope with NodeEffectScope

`scope(NodeEffectScope)` chooses what a shader effect applies to:

| Scope | Shader passes apply to |
| --- | --- |
| `NodeEffectScope.SELF` (default) | The node's own drawing only. Children and layers are drawn unaffected. |
| `NodeEffectScope.CHILDREN` | The whole render of the node: its own drawing, its children and its layers, composed together. |

`NodeEffectScope` is nested in `NodeEffect`: `import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;`.

```java
final RectNode selfScoped = RectNode.create(0, 0, 200, 120).color(Color.RED).effect(CircleNodeEffect.create().scope(NodeEffectScope.SELF));
final RectNode childrenScoped = RectNode.create(0, 0, 200, 120).color(Color.RED).effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN));
```

With `SELF`, a child that overflows the circle stays visible; with `CHILDREN`, the circle cuts the child too. Use `CHILDREN` to round a card together with its content.

`scope(...)` and `priority(...)` return the effect with its own class, so the setters of the effect chain after them: `RoundedNodeEffect.create(16F).scope(NodeEffectScope.CHILDREN).top(false)`. Render-state effects ignore the scope, they always wrap the whole render.

## Applying effects conditionally

`shouldApply(T node)` is checked every frame (and when the node is initialized); an effect that returns `false` is skipped. The built-in effects always return `true`; override it in [your own effects](custom-effects.md). A custom node can also override `Node.shouldApplyEffect(NodeEffect<Node, ?> effect)`, which defaults to `effect.shouldApply(this)`.

To toggle a built-in effect, add and remove it, or drive its value with a supplier (a `RoundedNodeEffect` radius of `0F` draws square corners).

## Effects and the shader pipeline

Shader effects go through the [Shader Pipeline](../shaders/pipeline.md):

- The node (or the whole subtree with `CHILDREN`) is drawn into a framebuffer sized to its rectangle in screen pixels, enlarged on each side by the largest expansion of its passes: the blur radius for `BlurNodeEffect`, the border width plus 2 for `BorderNodeEffect`, nothing for the others. A blur or an outer border can therefore draw outside the node's rectangle, but anything drawn outside that enlarged area (a child overflowing the node with `CHILDREN`, for instance) is cut off.
- Each pass but the last renders into a second framebuffer; the last pass draws the result into the current target (the screen, or the framebuffer of an enclosing effect).
- Framebuffers are pooled by size and nesting depth: a node with effects inside another node with effects works.
- A node with a zero width or height is drawn without its shader effects.
- If a shader failed to load (JOID prints `[JOID] Unable to load the shader ...`), its pass does nothing and the node is drawn without that effect.

## Effects and interaction

Effects only change the pixels. Layout, hovering and clicks keep using the node's rectangle: the corners of a circle-cut node still react to the mouse, a translated node is still clicked at its original place, and a masked-out area still receives events.

## Reference

### Node methods

| Method | Description |
| --- | --- |
| `effect(NodeEffect<? super T, ?> effect)` | Adds or replaces the effect of the same class. |
| `effect(Function<T, NodeEffect<? super T, ?>> factory)` | Same, with an effect built from the node. |
| `getEffect(Class<T> type)` | The effect of that class, as an `Optional`. |
| `hasEffect(Class<?> type)` | Whether an effect of that class is present. |
| `removeEffect(Class<?> type)` | Removes the effect of that class. |
| `clearEffects()` | Removes every effect. |
| `shouldApplyEffect(NodeEffect<Node, ?> effect)` | Overridable filter, defaults to `effect.shouldApply(this)`. |

### NodeEffect

| Method | Description |
| --- | --- |
| `priority(int priority)` | Sets the priority (default `0`). Lower runs first. Returns the effect. |
| `getPriority()` | The priority. |
| `scope(NodeEffectScope scope)` | Sets the scope (default `SELF`). Returns the effect. |
| `getScope()` | The scope. |
| `isShaderEffect()` | `true` for shader effects. |
| `shouldApply(T node)` | Whether the effect applies this frame. Default `true`. |
| `init(T node, UI ui)` | Called when the node is loaded into its UI. |
| `pre(T node, double mouseX, double mouseY)`, `post(...)` | Render-state hooks. |
| `toShaderPass(T node)`, `toShaderPasses(T node)` | Shader passes of a shader effect. |

The hooks are described in [Custom Effects](custom-effects.md).

### NodeEffectScope

| Value | Description |
| --- | --- |
| `SELF` | Shader passes apply to the node's own drawing. Default. |
| `CHILDREN` | Shader passes apply to the node, its children and its layers. |

## See also

- [RoundedNodeEffect](rounded.md)
- [BorderNodeEffect](border.md)
- [ShadowNodeEffect](shadow.md)
- [Custom Effects](custom-effects.md)
- [Shader Pipeline](../shaders/pipeline.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [Hover and Tooltips](../interactions/hover.md)