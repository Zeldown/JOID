---
summary: A tooltip element that draws itself while its node is hovered.
---
Add one with [[Node#hover]]; [[NodeHoverElement]] and [[CustomHoverElement]] place an element at the mouse, on the node or at a fixed point.
A lambda draws at the mouse, in the coordinates of the canvas.

Learn: [Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/#show-a-tooltip).

## render(Node,double,double)

Draws the element for the hovered node, with the mouse at this point of the canvas.

## detach()

Called when the element is removed or its node leaves the UI.

## getX()

Horizontal offset of the element from its position.

## getY()

Vertical offset of the element from its position.

## getWidth()

Width of the element; with a height, it keeps the element inside the window.

## getHeight()

Height of the element; with a width, it keeps the element inside the window.
