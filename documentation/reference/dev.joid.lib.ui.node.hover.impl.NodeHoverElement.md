---
summary: Tooltip made of a node, loaded and drawn while its owner is hovered.
---
The node is loaded into the UI of the hovered node the first time it shows, updated and drawn on every frame of the hover, and detached with the element.
Its position becomes an offset from the place of the tooltip.

Learn: [Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/#show-a-tooltip).

## follow(Node)

Shows the node at the mouse.

## fixed(Node)

Shows the node at its own position on the canvas.

## relative(Node)

Shows the node at its own position inside the hovered node.

## NodeHoverElement(Node,HoverElementPosition)

Called by a subclass with the node and its position.
