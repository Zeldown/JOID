---
summary: Places its visible children one after the other in a column or a row, and takes the size of its content.
---
On the main axis, a size of `0` takes the size of the content; a declared size is a minimum without overflow, and a fixed size with an overflow.
The children keep their own position as an offset; an `ABSOLUTE` or hidden child is left out.

Learn: [Flex layout](/use/learn/describing-the-ui/flex-layout/).

## vertical(double,double,double)

Creates a column at `x`, `y`, `width` wide, whose height follows its children.

## horizontal(double,double,double)

Creates a row at `x`, `y`, `height` high, whose width follows its children.

## FlexNode(double,double,double,double)

Called by a subclass to create a column.

## FlexNode(double,double,double,double,Axis)

Called by a subclass with the direction.

## direction

`VERTICAL` for a column, `HORIZONTAL` for a row.

## align

Where the children sit across the line: `START`, `CENTER` or `END`; `null` keeps their own cross position.

Default: none, the children keep their own cross position

## margin

Space between two children, in design pixels.

## init(UI)

Places the children when the node loads.

## update()

Places the children on every frame, after they have measured their size.

## layout()

Places the children again right before they are drawn.

## drawSkeleton(double,double)

Draws nothing while the flex node waits.

## getLayoutOrder()

The children in the order they are placed; a subclass overrides it to change the order.

## layoutChild(Node,double)

Places a child at this offset along the main axis, and across it by `align`; a subclass overrides it to move a child.

## isLaidOut(Node)

If `true`, the child takes part in the layout: it is visible and not `ABSOLUTE`.
