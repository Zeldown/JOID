---
summary: Base of every effect, an object added to a node that changes how the node is drawn, never its layout nor the area that receives the mouse.
capture: reference-effects-underline
alt: A white card on a light grey background, with a dark grey bar under it, as wide as the card.
---

A `NodeEffect` changes how its node is drawn: it draws around the node, changes the matrix or the render state for it, or processes its pixels with shader passes.
[[Node#effect]] adds one to any node, and a node holds one effect per class.
Use the built-in effects first: [[RoundedNodeEffect]], [[CircleNodeEffect]], [[BorderNodeEffect]], [[ShadowNodeEffect]], [[BlurNodeEffect]], [[MaskNodeEffect]] and [[TransformNodeEffect]].
Extend `NodeEffect` for a treatment that none of them gives, such as a bar under a node:

```java file="com/example/reference/effects/UnderlineUI.java" region="underline"
```

`draw` runs `body`, which draws the node with its children, then draws the bar under it.

Every frame, JOID keeps the effects whose `shouldApply` returns `true` and orders them by priority: the lowest priority wraps the others, and its passes run first.
The type parameter limits the nodes that take the effect: a `NodeEffect<RectNode>` goes on a [[RectNode]] and its subclasses only.

Learn: [Custom effects](/use/learn/going-further/custom-effects/), [Effects](/use/learn/style-and-motion/effects/).

## NodeEffect()

Called by a subclass for an effect of priority `0`.

## NodeEffect(int)

Called by a subclass for an effect of the given priority.

## priority

Place of the effect among the effects of its node: a lower priority starts earlier and wraps the effects of higher priority, and its shader passes run before theirs.
Two effects of the same priority keep the order in which they were added.

## scope

What the shader passes of the effect process: `SELF`, the drawing of the node alone, or `CHILDREN`, the node with its children and its layers.
It has no effect on `draw`, which always wraps the node and its children.

## draw(T,Runnable)

Hook that JOID calls on every frame while the effect applies, in the coordinates of the parent: the node sits at `getX()`, `getY()`.
An override draws before or after `body`, or changes the matrix or the render state around it and restores them; it runs `body` once, or the node disappears.
By default, it runs `body` alone.

## shouldApply(T)

Hook that JOID calls on every frame before the node draws: `false` skips `draw` and the shader passes for this frame.
The built-in effects return `false` when they would change nothing, such as a border of width `0`.

## toShaderPass(T)

Hook that returns the single [[IShaderPass]] that processes the pixels of the node on this frame, or `null` for an effect without pass.
JOID calls it on every frame while the effect applies, so the pass can carry the current values of the effect.
An effect with passes draws its node off screen first ([Performance](/use/in-depth/performance/#effects-that-render-off-screen)).

## toShaderPasses(T)

Hook that returns every pass of the effect for this frame, in order.
By default, it returns the pass of `toShaderPass`, or no pass; override it for an effect that needs several passes, such as a blur in two directions.

## copy()

Returns a shallow copy of the effect: the copy shares the values and the suppliers of the original.
[[Node#copy]] copies the effects of a node with it.

## init(T,UI)

Hook that JOID calls when the node joins a UI, and when the effect is added to a node that is already in one.
Override it to create what the effect needs for this node; it does nothing by default.

## detach(T)

Hook that JOID calls when the node leaves its UI, and when the effect is removed or replaced on a node that is in one.
Override it to release what `init` created; it does nothing by default.
An instance added to several nodes receives it once for each node.
