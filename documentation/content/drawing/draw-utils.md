# DrawUtils

`DrawUtils` is the entry point for ad-hoc drawing outside the node tree. Use it in `preDraw` / `postDraw`, inside custom `Node.draw()` overrides, or in one-off prototypes. For anything that needs hover, drag, effects, or reactivity, prefer a `Node` instead.

## The four facades

`DrawUtils` is just four `public static final` singletons — no constructor, nothing to instantiate.

```java
DrawUtils.SHAPE       // DrawShape
DrawUtils.TEXT        // DrawText
DrawUtils.RESOURCE    // DrawResource
DrawUtils.MODEL       // DrawModel
```

Each facade has a dedicated page listing every method:

- `Shapes` — rectangles, rounded rects, circles, borders, lines, curves, polygons.
- `Text` — strings and rich `Text` builders with alignment and overflow.
- `Resources` — images and videos from a `Resource`.
- `Models` — 3D `IDrawableModel` instances.

## When to use it

### Overlay in `postDraw`

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 4D);
}
```

### Background in `preDraw`

```java
@Override
public void preDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(0, 0, getWidth(), getHeight(), Color.decode("#111827"));
}
```

### Custom node `draw()`

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(getX(), getY(), getWidth(), getHeight(), Color.RED);
    DrawUtils.TEXT.drawText(getX() + 8, getY() + 8, "Custom node", info,
        Align.START, Align.START);
}
```

## Pixel alignment

Coordinates are UI units, and a unit rarely covers a whole number of window pixels: 0.7115 pixel in a 1366×768 window, 0.8 under a 125 % Windows scaling. JOID keeps what it draws on the window pixels, so nothing shimmers, jumps or disappears while a UI scrolls or slides:

- **Rectangles snap their edges.** When the transform is neither rotated nor skewed, `drawRect`, `drawRoundedRect`, masks and images put each edge on the nearest window pixel: two edges at the same position always land on the same pixel, so a child that fills its parent never lets it show through. The text baseline snaps the same way. Circles, lines and polygons keep their exact geometry, and translations stay exact.
- **Thin rectangles are lines.** On an axis where a rectangle covers less than three pixels — an underline, a separator, a cursor, a border — it keeps a whole number of pixels centred on its exact position, and a line thinner than a pixel is drawn one pixel thick with a proportional opacity: it has the same thickness everywhere on the screen, keeps its weight at small sizes and never vanishes.
- **Motion moves by whole pixels.** A node whose position changes from one frame to the next — scroll, drag, animation, layout or your own code — moves by whole window pixels from where it rested, with its children, and is drawn at its exact position again once it stops. The scroll offset of an overflow node is itself rounded to whole pixels, so scrolled content does not even shift when it stops. The translations of a `Transformation` — transitions, `TransformNodeEffect` — are rounded the same way. Moving content keeps its position inside its pixels, so it never shimmers nor jumps against what surrounds it. The layout and the hit tests keep the exact positions.

Drawings that follow the position of their node move with it. Only a drawing that moves on its own inside its node — its coordinates change while the node stays still — rounds its motion itself with `IRenderBridge.quantize`, until the matrix is popped:

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
final double offset = this.slide.getValue() * 300D;
render.pushMatrix();
try {
    render.quantize(offset, 0D);
    DrawUtils.SHAPE.drawRect(getX() + offset, getY(), 40, 40, Color.WHITE);
} finally {
    render.popMatrix();
}
```

## When not to use it

- **Don't rebuild a `RectNode` by hand.** `RectNode` + `RoundedNodeEffect` is less code and integrates with hover/drag/effects.
- **Don't layout with `DrawUtils`.** Reach for `FlexNode`, `GridNode`, or `ContainerNode`.
- **Don't react to state through `DrawUtils`.** The drawing is immediate — use `Signal` + `Node.watch` to drive content.

## Accessing singletons directly

Each facade exposes its instance if you prefer bypassing `DrawUtils`:

```java
DrawShape.getInstance().drawRect(...);
DrawText.getInstance().drawText(...);
DrawResource.getInstance().drawResource(...);
DrawModel.getInstance().drawModel(...);
```

Practically identical to the `DrawUtils.XXX` shortcut — pick whichever reads better in context.

## See also

- `Shapes` — every shape primitive.
- `Text` — raw strings, `Text` builders, modifiers.
- `Resources` — image / video drawing.
- `Models` — 3D model drawing.
- `Color` — color construction and binding.