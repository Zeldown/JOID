---
summary: A choice that shows every option side by side, one node per option, and selects the option under a left press.
---

A skin extends it and gives `option((value, index) -> node)`, which creates the node of each option.
The switch splits its width equally between the options, in their order.
Attaching a switch without an `option` function throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Controls](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## SwitchNode(double,double,double,double)

Creates the switch at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## option

The function that creates the node of an option from its value and its index.
The switch calls it again for every option each time the options change.

## getOptionList()

Returns the nodes of the options, in the order of the options, as a read-only list.

## layout()

Places the nodes of the options side by side, each one as wide as the switch divided by the number of options.

## mousePressed(NodeMouseEvent)

Selects the option under a left press that no one consumed, through `change(...)`, and consumes the press.

## createOptions()

Removes the nodes of the previous options and creates one node per option with the `option` function.
