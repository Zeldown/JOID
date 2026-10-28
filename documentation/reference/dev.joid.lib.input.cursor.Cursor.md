---
summary: The shape of the mouse pointer that a node shows while the pointer is over it.
---

Give a node a cursor with [[Node#cursor]]; [Change the cursor over a node](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node) explains which cursor shows over a node without one and while a node is pressed.

Backend note: the shapes are the standard pointers of the system.
On LWJGL 2, JOID loads them from the native cursors of Windows, macOS or X11.

Learn: [Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node).

## DEFAULT

The arrow of the system.

## POINTER

The pointing hand that marks something to click.

## TEXT

The I-beam that marks text to edit or select; every text field shows it.

## CROSSHAIR

A thin cross, for precise picking.

## MOVE

Four arrows, for something to drag in every direction.

## NOT_ALLOWED

A crossed circle, for an action that is not available.

## RESIZE_EW

A horizontal double arrow, for resizing from a left or right edge.

## RESIZE_NS

A vertical double arrow, for resizing from a top or bottom edge.

## RESIZE_NWSE

A diagonal double arrow from the top left to the bottom right, for resizing from those corners.

## RESIZE_NESW

A diagonal double arrow from the top right to the bottom left, for resizing from those corners.
