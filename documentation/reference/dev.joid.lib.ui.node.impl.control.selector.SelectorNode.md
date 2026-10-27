---
summary: A drop-down list made of a `field` and one node per option, with the selected option on top and the others in rows under it or above it.
---

A skin extends it and gives `field(...)` and `option(value -> node)`.
Each row has the height of the selector; while the list is open, the selector grows to show every option, and the field covers the whole list.
A left press on an option selects it through `change(...)` and closes the list.
Attaching a selector without a field or without an `option` function throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Controls](/use/learn/interactivity/controls/#choose-from-a-drop-down-list).

## SelectorNode(double,double,double,double)

Creates the selector at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin; the height is the height of one row.

## field

The node drawn behind the options, over the selected row while the list is closed and over every row while it is open, with a `zindex` of `-1`; required.

## option

The function that creates the node of an option from its value.
The selector calls it again for every option each time the options change.

## direction

The side where the list opens: `DOWN` under the selected row, `UP` above it, near the bottom of the screen.

## isSelected(Node)

Returns `true` when a node is the node of the selected option.

## getSelected()

Returns the node of the selected option, or `null` before the options exist.

## getOptionList()

Returns the nodes of the options, in the order of the options, as a read-only list.

## update()

When the list opens downward, gives the selector the height of every row while it is open, and of one row while it is closed; a list that opens upward keeps the height of one row.

## layout()

Places the field and one row per option: the selected one first, the others in the order of `getOptionRank`, shown only while the list is open.

## mousePressed(NodeMouseEvent)

While the list is open, selects the option under a left press that no one consumed, then opens or closes the list as its base class does.

## createOptions()

Removes the nodes of the previous options and creates one node per option with the `option` function.
