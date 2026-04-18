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