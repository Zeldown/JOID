---
summary: A key combination attached to a UI or a node that fires its listeners when the user presses it.
---
A shortcut fires only while it is attached, and the Alt, Control and Shift keys must be exactly as set.
By default it waits for the nodes: a focused text field that takes the key keeps it.
The shortcuts of the visible and enabled nodes come first, from the front-most, then those of the UI; the first one that matches consumes the key.

Learn: [Keyboard and shortcuts](/use/learn/interactivity/keyboard/#bind-a-shortcut).

## create(Object)

Creates a shortcut on a [[Key]] or on any value that a registered [[KeyResolver]] turns into a key.
In dev mode, a value that no resolver knows prints a warning; the shortcut fires once a resolver that knows it is registered.

## alt

If `true`, the shortcut fires only while Alt is down; if `false`, only while it is up.

## ctrl

If `true`, the shortcut fires only while Control is down; if `false`, only while it is up.

## shift

If `true`, the shortcut fires only while Shift is down; if `false`, only while it is up.

## overrideFocus

If `true`, the shortcut fires before the nodes see the key, even when a text field has the focus.

## onTrigger(EventListener)

Adds a listener that runs on the render thread when the shortcut fires.

## fireTrigger(Key)

Runs the listeners of the shortcut as if this key had fired it.

## attach(UI)

Attaches the shortcut to the UI; it leaves its previous owner first.

## attach(Node)

Attaches the shortcut to the node; it fires while the node is visible and enabled in an open UI.

## detach()

Detaches the shortcut from its UI or its node.

## owner(UI,Node)

Records the owner of the shortcut.
`UI.addShortcut` and `Node.addShortcut` call it; you attach with `attach`.

## isAttached()

If `true`, the shortcut has a UI or a node.

## isTriggeredBy(Key)

If `true`, the shortcut is attached, its binding resolves to this key and the modifier keys match.

## getBinding()

The key or the value given to `create`.

## getEventBus()

The listeners of the shortcut.

## getUi()

The UI the shortcut is attached to, or `null`.

## getNode()

The node the shortcut is attached to, or `null`.
