# Hover and Tooltips

Every node tracks whether the mouse is over it, animates a hover value between 0 and 1, fires hover callbacks and can show a tooltip. Use the hover value to fade colors or decorations, the callbacks to react to the mouse entering or leaving, and `hover(...)` to attach a text or custom tooltip.

## A hover effect and a tooltip

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY, Color.GRAY)
.hoverDuration(300L)
.hoverEquation(TweenEquations.QUAD_OUT)
.hover(() -> "Opens the shop")
.attach(this);
```

![The cursor enters a dark gray rectangle: it lightens and a tooltip reading Opens the shop follows the mouse](../images/hover-tooltip.gif "The color blends in 300 ms and the tooltip follows the mouse while it stays over the node.")

The rectangle blends from `DARKGRAY` to its hovered color `GRAY` in 300 ms when the mouse enters, back when it leaves, and shows a one-line tooltip while hovered. Hovered colors of [RectNode](../nodes/visual/rect.md), [CircleNode](../nodes/visual/circle.md) and [ResourceNode](../nodes/visual/resource.md) follow the hover value.

## Hover state with isHovered

On every frame where the node is visible, it tests the mouse with `isHovered(mouseX, mouseY)`: the node must be in a UI that is on top, enabled, inside the area of a clipping parent, and the mouse within its bounds (see [Hit testing](mouse-and-keyboard.md#hit-testing-with-ishovered)). The result is stored and returned by `isHovered()`.

- A disabled node (`enabled(...)` returning `false`) is never hovered; disabling a hovered node ends its hover.
- A node that is not drawn (hidden, or outside the area of a parent with an overflow) keeps its last hover state until it is drawn again.
- `hovered(boolean hovered)` overwrites the stored state; the next drawn frame compares it with the mouse again and fires `onHoverStart` or `onHoverEnd` when they differ.

## Hover animation with hoverValue

Each node owns a `TweenAnimator` (`getHoverAnimator()`). When the node becomes hovered, it animates to `1` over `hoverDuration` milliseconds with `hoverEquation`; when the hover ends, it animates back to `0`.

`hoverValue(float value)` returns `value` multiplied by the current animator value. Use it while drawing:

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.onDraw((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(node.getX(), node.getY() + node.getHeight() - 4, node.getWidth() * node.hoverValue(1F), 4, Color.WHITE))
.attach(this);
```

![The cursor enters a dark gray rectangle and a white underline grows from its left edge](../images/hover-underline.gif "The underline width is hoverValue(1F) times the node width.")

The `onDraw` lambda runs after the node drew itself, in the same coordinate space, so the underline grows from the left edge as the mouse enters (`DrawUtils` is in `dev.joid.lib.draw`).

| Method | Default | Description |
|---|---|---|
| `hoverDuration(long hoverDuration)` | `200L` | Duration of the hover animation, in milliseconds. |
| `hoverEquation(TweenEquation equation)` | `TweenEquations.LINEAR` | Easing of the hover animation (see [Easing](../animation/easing.md)). |
| `hoverValue(float value)` | | `value` times the animator value, from `0` (not hovered) to `1` (hovered). |
| `getHoverAnimator()` | | The `TweenAnimator` behind `hoverValue`. |
| `getHoverDuration()`, `getHoverEquation()` | | Current settings. |

## Hover callbacks

| Method | Lambda arguments | Fires |
|---|---|---|
| `onHoverStart` | `(node, mouseX, mouseY)` | The frame the node becomes hovered. |
| `onHover` | `(node, mouseX, mouseY)` | Every frame while the node is hovered, after `onHoverStart`. |
| `onHoverEnd` | `(node, mouseX, mouseY)` | The frame the node stops being hovered. |

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.WHITE)
.onHoverStart((node, mouseX, mouseY) -> System.out.println("Enter at " + mouseX + ", " + mouseY))
.onHoverEnd((node, mouseX, mouseY) -> System.out.println("Leave"))
.attach(this);
```

They run while the node is drawn, before its `onAnimate`, `onMount` and `onRender` callbacks (see [Callbacks](callbacks.md#callbacks-within-a-frame)). Their default `post` does not affect the other nodes: every node evaluates its own hover.

## Text tooltips with hover

Text tooltips are lines given by suppliers. The suppliers are called on every frame the tooltip shows.

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.WHITE)
.hover(() -> "Buy for 25 coins")
.hover(() -> Arrays.asList("Left click: buy", "Right click: preview"))
.attach(this);
```

![A three-line tooltip under the cursor: Buy for 25 coins, Left click: buy, Right click: preview](../images/hover-lines.gif "The lines of both suppliers form one tooltip, drawn here by the demo UI bridge.")

| Method | Description |
|---|---|
| `hover(HoverSupplier supplier)` | Adds a one-line supplier (`() -> String`). A `null` line adds nothing for that frame. |
| `hover(Supplier<List<String>> supplier)` | Adds a supplier of several lines. |
| `hoverLines(HoverSupplier supplier)`, `hoverLines(Supplier<List<String>> supplier)` | Removes every line supplier, then adds this one. |
| `clearHoverLines()` | Removes every line supplier. |

The lines of all the suppliers are concatenated in the order you added them and drawn as one tooltip. When there is no line, no text tooltip shows. `HoverSupplier` is in `dev.joid.lib.ui.node.hover`.

### Tooltips that follow a signal

To show the value of a [signal](../state/signals.md), make the node [watch](../state/watch.md) it and set the line in `onInit`, which runs again each time the signal publishes. `hoverLines(...)` removes the previous line supplier first, so the lines do not pile up:

```java
final IntegerSignal price = new IntegerSignal(25);

RectNode
.create(100, 100, 300, 80)
.color(Color.WHITE)
.onInit(node -> {
    final String line = "Buy for " + price.getOrDefault() + " coins";
    node.hoverLines(() -> line);
})
.watch(price)
.attach(this);
```

The line is built once per publish, not on every frame the tooltip shows.

### Drawing text tooltips with drawHover

JOID hands the lines to `UI.drawHover(List<String> lines, double mouseX, double mouseY)`, which calls `IUIBridge.drawHover(UI ui, List<String> lines, double mouseX, double mouseY)` of the UI's bridge. The UI bridge decides the look of text tooltips (see [UI Bridge](../integration/ui-bridge.md)); the mouse position is in UI units and the drawing happens in the UI's coordinate space. Override `drawHover` in a UI to give its tooltips another look:

```java
@Override
public void drawHover(final List<String> lines, final double mouseX, final double mouseY) {
    final double height = lines.size() * 24D + 12D;
    DrawUtils.SHAPE.drawRect(mouseX + 12D, mouseY + 12D, 260D, height, Color.BLACK);
    for (int i = 0; i < lines.size(); i++) {
        DrawUtils.TEXT.drawText(mouseX + 20D, mouseY + 18D + i * 24D, Text.create(lines.get(i), info));
    }
}
```

![The same three-line tooltip drawn as white text on a plain black box](../images/hover-draw.gif "The lines of the previous example, drawn by this drawHover override.")

`info` is a `TextInfo` (see [Text Model](../text/text-and-textinfo.md) and [Drawing Text](../drawing/text.md)).

## Custom tooltips with HoverElement

`HoverElement` (`dev.joid.lib.ui.node.hover`) draws anything as a tooltip.

| Method | Default | Description |
|---|---|---|
| `render(Node node, double mouseX, double mouseY)` | Abstract | Draws the element; `node` is the hovered node. |
| `getX()`, `getY()` | `0` | Offset of the element from its anchor. |
| `getWidth()`, `getHeight()` | `0` | Size of the element, used to place and clamp it. |

An element given directly to `hover(HoverElement)` is drawn as is, in UI coordinates. Wrap it to position it:

### Positioning with CustomHoverElement

`CustomHoverElement` (`dev.joid.lib.ui.node.hover.impl`) anchors an element.

| Factory | Anchor |
|---|---|
| `CustomHoverElement.follow(HoverElement element)` | The mouse position. |
| `CustomHoverElement.relative(HoverElement element)` | The top-left corner of the hovered node (absolute position). |
| `CustomHoverElement.fixed(HoverElement element)` | The origin of the UI, `(0, 0)`. |

The element is drawn with its top-left corner at `anchorX + getX()`, `anchorY + getY() - getHeight()`: its bottom edge sits `getY()` units below the anchor, so a negative `getY()` lifts it. When the element has a positive width and height, it is moved left to stay within the right edge of the visible area and down to stay below its top edge. `render` is then called with the origin moved to that corner, so the element draws from `(0, 0)`.

```java
final HoverElement badge = new HoverElement() {

    @Override
    public void render(final Node node, final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRect(0, 0, 160, 40, Color.BLACK);
    }

    @Override
    public double getX() {
        return 12;
    }

    @Override
    public double getY() {
        return -8;
    }

    @Override
    public double getWidth() {
        return 160;
    }

    @Override
    public double getHeight() {
        return 40;
    }

};

RectNode.create(100, 100, 300, 80).color(Color.WHITE).hover(CustomHoverElement.follow(badge)).attach(this);
```

![A black badge that follows the cursor above a white rectangle](../images/hover-element.gif "The badge is drawn 12 units right of the mouse, its bottom edge 8 units above it.")

`getElement()` and `getPosition()` return the wrapped element and its `CustomHoverElement.HoverElementPosition` (`FOLLOW`, `FIXED`, `RELATIVE`).

### Nodes as tooltips with NodeHoverElement

`NodeHoverElement` uses a node as the tooltip. The node's `x` and `y` are the offset (`getX()`, `getY()`) and its width and height the size, with the same placement rules as `CustomHoverElement`.

| Factory | Anchor |
|---|---|
| `NodeHoverElement.follow(Node node)` | The mouse position. |
| `NodeHoverElement.relative(Node node)` | The top-left corner of the hovered node. |
| `NodeHoverElement.fixed(Node node)` | The origin of the UI. |

```java
final RectNode tooltip = RectNode.create(12, -8, 220, 60).color(Color.BLACK).body(card -> {
    TextNode.create(10, 10).text(Text.create("Iron sword", info)).attach(card);
});

RectNode.create(100, 100, 300, 80).color(Color.WHITE).hover(NodeHoverElement.follow(tooltip)).attach(this);
```

![A black card with the text Iron sword that follows the cursor](../images/hover-node.gif "The tooltip node is placed like a CustomHoverElement: x = 12, y = -8 from the mouse.")

The tooltip node is not part of the node tree: it is loaded into the UI of the hovered node the first time it shows (its `onInit` fires then) and drawn with its children each time it shows.

### Managing tooltip elements

| Method | Description |
|---|---|
| `hover(HoverElement element)` | Adds an element. |
| `hoverElements(HoverElement element)` | Removes every element, then adds this one. |
| `clearHoverElements()` | Removes every element. |
| `clearHover()` | Removes every element and every line supplier. |

The elements of a node are drawn in the order you added them, then its text tooltip. `DefaultHoverElement` (`new DefaultHoverElement(List<String> lines)`) is the element JOID builds for the text lines; adding one yourself shows fixed lines through `drawHover`.

## Which tooltip shows

- Tooltips show only in the UI that is on top, after every node of that UI is drawn, above them.
- At most one node shows its tooltip per frame. JOID searches the top-level nodes from front to back and, inside a node, its children before the node itself: the deepest hovered node with tooltip content shows it. A hovered child without a tooltip lets its parent show its own. The first hovered top-level node ends the search, even without a tooltip, so the nodes behind it show nothing.
- The tooltip search ignores `enabled(...)`: a disabled node still shows its tooltip, as long as it is visible and the mouse is over it.

## Hover API reference

| Method | Description |
|---|---|
| `isHovered()` | Hover state of the last drawn frame. |
| `isHovered(double mouseX, double mouseY)` | Hit test with the enabled check. |
| `isHovered(double mouseX, double mouseY, boolean checkEnabled)` | Hit test; `false` skips the enabled check. |
| `hovered(boolean hovered)` | Overwrites the stored hover state. |
| `hoverValue(float value)` | `value` scaled by the hover animation. |
| `hoverDuration(long)`, `hoverEquation(TweenEquation)` | Hover animation settings. |
| `hover(...)`, `hoverLines(...)`, `hoverElements(...)` | Tooltips. |
| `clearHover()`, `clearHoverLines()`, `clearHoverElements()` | Remove tooltips. |
| `onHoverStart`, `onHover`, `onHoverEnd` | Hover callbacks. |

## See also

- [Callbacks](callbacks.md)
- [Mouse and Keyboard](mouse-and-keyboard.md)
- [UI Bridge](../integration/ui-bridge.md)
- [RectNode](../nodes/visual/rect.md)
- [Easing](../animation/easing.md)