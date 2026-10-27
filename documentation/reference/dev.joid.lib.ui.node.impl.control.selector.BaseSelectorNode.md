---
summary: The behavior of a drop-down list without its look: a choice whose list opens on a left press and closes on the next one.
---

Extend [[SelectorNode]] for a look made of a field and one node per option; extend `BaseSelectorNode` only to build the look entirely yourself.
While the list is open, a left press anywhere closes it; the list closes too when the selector is detached.

Learn: [Controls](/use/learn/interactivity/controls/#choose-from-a-drop-down-list), [UI kit](/use/learn/going-further/ui-kit/#draw-a-control-entirely-your-own-way).

## BaseSelectorNode(double,double,double,double)

Creates the selector at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## getOptionRank(V)

Returns the row of an option in the open list: `0` for the selected option, then the other options in their order, from `1`.

## active(boolean)

Gives the list its start state: `true` is open.

## active(Signal)

Binds the open state to a signal both ways: the list follows the signal, and a press writes the new state into it.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## open()

Opens the list as if the user pressed the selector: `beforeOpen` can refuse it, then `onOpen` fires.

## close()

Closes the list as if the user pressed it: `beforeClose` can refuse it, then `onClose` fires.

## beforeOpen(EventListener)

Adds a listener that runs before the list opens on a press or `open()`; `cancel()` keeps it closed.

## beforeClose(EventListener)

Adds a listener that runs before the list closes on a press or `close()`; `cancel()` keeps it open.

## onOpen(EventListener)

Adds a listener that runs each time the list opens, whatever its source.

## onClose(EventListener)

Adds a listener that runs each time the list closes, whatever its source.

## isActive()

Returns `true` while the list is open.

## mousePressed(NodeMouseEvent)

On a left press, opens the closed list when the press is over the selector, or closes the open list wherever the press is; a press over the selector is consumed.

## detach()

Closes the list when the selector leaves the tree.
