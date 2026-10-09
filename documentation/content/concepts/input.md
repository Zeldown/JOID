# Input and Callbacks

Nodes react to the user through callbacks: small lambdas you register with the `on...` methods, such as the `onClick` of the Quick Start or `onHoverStart`, and UIs add keyboard shortcuts. This page shows the common callbacks, how an event travels through the node tree, and keybinds. The ready-made controls (text fields, checkboxes, sliders...) come later, in [Input Controls](../essentials/controls.md).

## Reacting to clicks with onClick

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.hoveredColor(Color.GRAY)
.onClick((node, mouseX, mouseY, clickType) -> System.out.println("Clicked with " + clickType))
.attach(this);
```

![The cursor enters a dark gray rectangle, which lightens, and clicks it](../images/ess-input-click.gif "The hovered color shows the node under the mouse; the press fires onClick (the ring marks the pressed button).")

`onClick` fires when a mouse button is pressed over the node. The lambda receives the node, the mouse position in canvas units and the button: a `MouseButton` (`dev.joid.lib.input.mouse`) with `LEFT`, `RIGHT`, `MIDDLE`, `BACK`, `FORWARD`, and helpers such as `clickType.isRight()`:

```java
RectNode
.create(100, 200, 300, 80)
.color(Color.DARKGRAY)
.onClick((node, mouseX, mouseY, clickType) -> {
	if (clickType.isRight()) {
		System.out.println("Context menu at " + mouseX + ", " + mouseY);
	}
})
.attach(this);
```

Every `on...` method adds a callback and returns the node, so they chain. Registering the same method twice keeps both callbacks. Hidden and disabled nodes never receive `onClick`.

## How events travel

An input event travels through the tree with a context that any node can cancel to consume it. The children are asked before their parent, the front-most first, so the deepest node under the mouse wins: a button with `onClick` inside a card receives the click, and the `onClick` of the card does not fire. After the nodes come the hooks of the UI (`mousePressed`, `keyPressed`...), and an event nobody consumed goes on to the UIs below.

![A press goes through the PRE phase of a parent, its children front first, then the POST phase where onClick consumes it, then the UI hooks, then the UIs below if nobody consumed it](../images/ess-diagram-events.png "The path of a mouse press.")

Each callback has two phases: PRE runs before the children and the behavior of the node, POST after them. Your lambdas run in POST. To act first, for example to block the clicks on a panel while it loads, implement the callback interface and override its `pre` method: [Callbacks](../interactions/callbacks.md) shows how.

## Hover callbacks and tooltips

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.hoveredColor(Color.GRAY)
.onHoverStart((node, mouseX, mouseY) -> System.out.println("Enter"))
.onHoverEnd((node, mouseX, mouseY) -> System.out.println("Leave"))
.hover(() -> "Opens the shop")
.attach(this);
```

![The cursor enters a rectangle and a tooltip reading Opens the shop follows it](../images/ess-input-hover.gif "onHoverStart fires as the mouse enters, the tooltip follows the mouse, onHoverEnd fires as it leaves.")

- `onHoverStart` and `onHoverEnd` fire on the frame the mouse enters and leaves the node; `onHover` fires on every frame in between.
- `hover(...)` adds a tooltip. The supplier is read while the tooltip shows, so its text can change; return a `List<String>` for several lines. Your UI bridge draws the text tooltips.
- `isHovered()` tells at any time whether the node is under the mouse.

## Keyboard shortcuts with keybind

For shortcuts, register a keybind on the UI in `init()`. `Key` is in `dev.joid.lib.input.key`:

```java
super.keybind(() -> System.out.println("Saved"), Key.LEFT_CONTROL, Key.S);
super.keybind(() -> System.out.println("Help"), Key.F1);
```

A keybind runs when one of its keys is pressed while all of them are down; the order does not matter. To test a key anywhere else, for example in a click callback, use `Key.LEFT_SHIFT.isDown()` or `UI.isCtrlKeyDown()`, `UI.isShiftKeyDown()` and `UI.isAltKeyDown()`.

## Pitfalls

- `onMousePressed`, `onMouseReleased`, `onMouseDragged`, `onMouseScroll` and `onKeyPressed` are listeners: they receive every event of their kind, wherever the mouse is, without consuming it. For a click on a node, use `onClick`.
- The mouse coordinates of a callback are canvas units, relative to the canvas and not to the node: compare them with `getAbsoluteX()` and `getAbsoluteY()` (the position of the node on the canvas), not with `getX()` (its position in its parent).

## See also

- Next: [Signals and Reactivity](signals.md)
- [Callbacks](../interactions/callbacks.md): every callback, the PRE and POST phases, `DispatchContext`, the exact order.
- [Mouse and Keyboard](../interactions/mouse-and-keyboard.md): `MouseButton`, every `Key`, keybinds, the input hooks of the UI.
- [Hover and Tooltips](../interactions/hover.md): the hover animation, custom tooltips.
- [Drag and Drop](../interactions/drag-drop.md): draggable nodes, areas, snapping.
- [TextFieldNode](../nodes/input/text-field.md): accepting, formatting and validating input.