---
summary: Action that `Node.watch(...)` runs on its node each time the watched signal changes.
---

Pass one or more to [[Node#watch]]: `watch(items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)` removes the children, then runs the body again, so the node rebuilds its subtree from the current value.
A lambda `(node, signal) -> ...` gives an action of your own.
When an action throws, JOID prints a warning and the other actions of the watch still run.

Learn: [Rebuilding the tree](/use/learn/state/rebuilding/).

## apply(Node,Signal)

Runs the action on `node` after `signal` changed, on the render thread, while JOID follows the reads like in a body.

## BODY

Runs the body that the node received with `body(...)` again; it does nothing on a node without body.
It does not remove the children that the previous body added: put `CLEAR_CHILDREN` before it.

## CLEAR_CHILDREN

Removes every child of the node.
