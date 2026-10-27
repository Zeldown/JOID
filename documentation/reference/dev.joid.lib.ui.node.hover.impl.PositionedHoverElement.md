---
summary: Tooltip element placed at the mouse, on the node or at a fixed point, and kept inside the window.
---
Learn: [Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/#show-a-tooltip).

## PositionedHoverElement(IHoverElement,HoverElementPosition)

Called by a subclass with the element to draw and its position.

## render(Node,double,double)

Draws the element at its position plus its own offset, moved inside the window when it has a width and a height.

## detach()

Detaches the element it draws.

## getElement()

The element it draws.

## getPosition()

Where the element is placed.
