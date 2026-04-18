# Hover

Tooltips and hover state. JOID tracks hover automatically and exposes it to callbacks, colors, and tooltip nodes.

## Hover state

```java
node.hovered();                   // boolean: is mouse currently over?
node.hoverValue(float max);       // 0 → max, interpolated over hoverDuration
node.hoverDuration(long ms);      // fade duration (default 200ms)
node.hoverEquation(TweenEquation); // easing for the fade (default LINEAR)
```

`hoverValue` is the meat — use it to lerp colors, scales, borders:

```java
RectNode.create(0, 0, 100, 50)
    .color(() -> Color.decode("#1f2937").to(Color.decode("#3b82f6"), node.hoverValue(1F)))
    .attach(parent);
```

Or use the built-in shortcut:

```java
node.color(Color.decode("#1f2937"), Color.decode("#3b82f6"));
```

which does exactly the same thing internally.

## Tooltips via `hover(...)`

Simple string:

```java
node.hover(() -> "Click me!");
```

Multiple lines:

```java
node.hover(() -> Arrays.asList(
    "Save document",
    "Shortcut: Ctrl+S"
));
```

Conditional:

```java
node.hover(() -> isDirty() ? "Save (unsaved changes)" : null);   // null = no tooltip
```

## Custom tooltip nodes

For richer tooltips (icons, colors, layouts):

```java
node.hover(CustomHoverElement.follow(new MyTooltipNode()));
```

Three positioning modes via `HoverElementPosition`:

```java
CustomHoverElement.follow(element)      // follows the mouse
CustomHoverElement.relative(element)    // relative to the node
CustomHoverElement.fixed(element)       // fixed at (0, 0)
```

`HoverElement` is an interface you can implement:

```java
HoverElement tooltip = new HoverElement() {
    @Override
    public void render(Node node, double mx, double my) {
        DrawUtils.SHAPE.drawRect(0, 0, 200, 60, Color.decode("#1f2937"));
        DrawUtils.TEXT.drawText(8, 8, Text.create("Rich content", info));
    }

    @Override public double getWidth() { return 200; }
    @Override public double getHeight() { return 60; }
};

node.hover(CustomHoverElement.follow(tooltip));
```

The `getWidth` / `getHeight` are used by the screen-bounds clamp to ensure the tooltip doesn't fall off the screen.

## Node-as-tooltip

Treat another node as the tooltip content:

```java
final RectNode tooltipNode = RectNode.create(0, 0, 200, 60)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F));

node.hover(NodeHoverElement.follow(tooltipNode));
```

The tooltip node is rendered in the hover overlay, not as a child — it won't affect layout.

## Screen-bounds clamping

When a tooltip would overflow the right edge of the screen, JOID shifts it left. When it would overflow the top, it shifts down. Enabled automatically for `FOLLOW` and `RELATIVE` positions.

## Hover callbacks

```java
node.onHover((n, mouseX, mouseY) -> { });      // fired each frame while hovered
```

For enter/leave semantics, track state yourself:

```java
boolean wasHovered = false;

node.onUpdate((n, ctx) -> {
    final boolean now = n.hovered();
    if (now && !wasHovered) onEnter();
    if (!now && wasHovered) onLeave();
    wasHovered = now;
});
```

## Best practices

- **Use strings for simple tooltips.** Don't build a full node for a 1-line hint.
- **Cache tooltip nodes.** Create once, reuse the instance across `node.hover(...)`.
- **Keep tooltip content small.** 200×80 px is a soft cap; bigger feels intrusive.
- **Match tooltip style to app theme.** `Color.decode("#1f2937")` for dark, `#f3f4f6` for light.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md) — hover state API.
- [Callbacks](callbacks.md) — hover callback details.
