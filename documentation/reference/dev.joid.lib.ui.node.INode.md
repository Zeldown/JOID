---
summary: The hooks that JOID calls on a node, all empty by default, that a node of your own overrides.
---
[[Node]] implements it; override only the hooks you need, and call `super` to keep the behavior of a built-in node.
Every hook runs on the render thread.

Learn: [Custom nodes](/use/learn/going-further/custom-nodes/#override-the-hooks-you-need).

## init(UI)

Called when the node is loaded into a UI, after its children and its effects and before `onInit`.

## update()

Called on every frame, after the children are updated and before the node is drawn.
Measure a size that depends on the content here, so that a layout node sees it on the same frame.

## layout()

Called on every frame, after the node is placed and before its children are drawn: place the children and the parts here with `layoutPosition` and `layoutSize`.

## draw(double,double)

Draws the node at `getX()`, `getY()`, with the mouse at this point of the canvas, once it is mounted.

## drawSkeleton(double,double)

Draws the node in place of `draw` while one of its `wait` conditions fails and it has no skeleton node.

## mousePressed(NodeMouseEvent)

Called when a mouse press reaches the node, after its `handleMousePressed` listeners.
The event can already be consumed: check it before acting.

## mouseReleased(NodeMouseEvent)

Called when a mouse release reaches the node.

## mouseDragged(NodeMouseEvent)

Called when the mouse moves with a button held.

## mouseScroll(NodeMouseScrollEvent)

Called when a wheel turn reaches the node, after the node has scrolled its own content.

## charTyped(NodeCharEvent)

Called when a typed character reaches the node.

## keyPressed(NodeKeyEvent)

Called when a key press reaches the node.

## keyReleased(NodeKeyEvent)

Called when a key release reaches the node.

## detach()

Called when the node leaves its UI, after its children: release here what `init` started.
A `detach` that throws prints a warning, and the node is detached anyway.
