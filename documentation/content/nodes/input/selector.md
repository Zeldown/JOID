# SelectorNode

`SelectorNode` (`dev.joid.lib.ui.node.impl.structure.selector`) is a dropdown: its children are the options, it shows the selected one, a click opens the list of the others and a click on one selects it. It is abstract: you subclass it to draw the background, and you attach any nodes as options.

## Creating a selector

```java
public class DropdownNode extends SelectorNode {

    protected DropdownNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static DropdownNode create(final double x, final double y, final double width, final double height) {
        return new DropdownNode(x, y, width, height);
    }

    @Override
    public void drawBackground(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
    }

}
```

Then, in `UI.init()` (`font` is an `IFont` you loaded):

```java
final TextInfo info = TextInfo.create(font, 20F, Color.WHITE);

DropdownNode
.create(810, 400, 300, 50)
.onChange((selector, selected) -> System.out.println("Difficulty: " + ((TextNode) selected).getText().getText()))
.body(selector -> {
    for (final String difficulty : new String[] { "Easy", "Normal", "Hard" }) {
        TextNode
        .create(0, 0, 300, 50)
        .text(Text.create(difficulty, info, Align.CENTER, Align.CENTER))
        .attach(selector);
    }
})
.attach(this);
```

![The cursor opens a black dropdown showing Easy, then picks Hard](../../images/selector-pick.gif "A click on the selected option opens the list; a click on another option selects it and closes the list.")

- The first option, "Easy", is selected and shown. A click on it opens the list below; a click on "Hard" selects it, calls `onChange` and closes the list.
- Call `onChange` before `body(...)`: `body` returns a plain `Node`.

See [Custom Nodes](../custom-nodes.md) for the constructor and factory contract.

## Options and layout

Every child of the selector is an option, in the order you attach them. On each frame the selector lays them out:

- every option gets `x = 0`, the selector's initial width and its initial height (the values given to the constructor);
- the selected option sits at `y = 0`;
- the other options are stacked one initial height apart, below (`SelectorDirection.DOWN`, the default) or above (`SelectorDirection.UP`), in their order;
- their visibility becomes "visible while the list is open, or when selected", replacing any `visible(...)` rule you gave them.

The selector's own height follows: with `DOWN` it grows to cover the open list (so `drawBackground` covers it too) and goes back to the initial height when closed; with `UP` it keeps the initial height and the list opens outside it.

`drawBackground(double mouseX, double mouseY)` is abstract and called on each draw, after the layout. The selector draws nothing and lays nothing out while it has no option.

## Clicking

| Event | Effect |
| --- | --- |
| Closed, press on the selected option | Opens the list. The press is consumed. |
| Closed, press elsewhere | Nothing. |
| Open, press on another option | Selects it, calls `onChange`, closes the list. The press is consumed. |
| Open, press on the selected option or outside the options | Closes the list without consuming the press. |

- Any mouse button works. There is no keyboard control.
- When nothing is selected, the first option becomes selected on the first draw; the selector ignores presses before that.

> WARNING: The options receive the press before the selector. An option with its own click callback (`onClick`) consumes the press, and the selector then neither opens nor selects. Leave the options without click callbacks and react in `onChange`.

> TIP: The open list is drawn with the selector, so attach the selector after the nodes the list overlaps (or give it a higher z-index): it then draws above them and receives the press first.

## Selecting from code

| Method | Effect |
| --- | --- |
| `selected(Node)` | Selects an option without calling `onChange`. Pass one of the selector's children. |
| `active(boolean)` | Opens (`true`) or closes (`false`) the list. |
| `direction(SelectorDirection)` | Opens the list downwards (`DOWN`, default) or upwards (`UP`). |

`getSelected()` returns the selected option (`null` until the first draw if you did not select one), `isSelected(Node)` tells whether a node is the selected option, `isActive()` whether the list is open and `getDirection()` the direction.

## onChange

`onChange(NodeSelectorChangeCallback<T>)` takes `(node, selected)`, where `selected` is the newly selected option; `node.getSelected()` already returns it. It runs only for a selection made with the mouse. Cancelling the context in the `pre(...)` phase keeps the previous option selected and leaves the list open (see [Callbacks](../../interactions/callbacks.md)). The callback interface is in `dev.joid.lib.ui.node.impl.structure.selector.callback`.

## Reference

### SelectorNode

| Method | Default | Description |
| --- | --- | --- |
| `SelectorNode(double x, double y, double width, double height)` | | Protected constructor. The width and height are the size of each option. |
| `drawBackground(double mouseX, double mouseY)` | | Abstract. Draws the background. |
| `direction(SelectorDirection)` | `DOWN` | Side the list opens to. |
| `active(boolean)` | `false` | Opens or closes the list. |
| `selected(Node)` | first option | Selected option. |
| `onChange(NodeSelectorChangeCallback<T>)` | | Adds a callback `(node, selected)`. |
| `getSelected()` | | Selected option. |
| `isSelected(Node)` | | Whether the node is the selected option. |
| `isActive()` | | Whether the list is open. |
| `getDirection()` | | Opening direction. |

`draw` and `mousePressed` are final. Every setter returns the node itself, typed by the generic return of the fluent API.

### SelectorDirection

`SelectorNode.SelectorDirection` is a nested enum.

| Constant / method | Description |
| --- | --- |
| `UP` | The list opens above the selector. |
| `DOWN` | The list opens below the selector. |
| `isDown()` | `true` for `DOWN`. |

## See also

- [SwitchNode](switch.md)
- [Node Fundamentals](../node-fundamentals.md)
- [Callbacks](../../interactions/callbacks.md)
- [Custom Nodes](../custom-nodes.md)