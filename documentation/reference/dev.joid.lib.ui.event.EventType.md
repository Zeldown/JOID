---
summary: The key of one kind of event in an event bus, such as the click of a node.
---

A node or a UI declares one `EventType` per event, in a static field, and passes it to `listen` and `fire`.
Two calls of `create` with the same name give two different types.

Learn: [Custom nodes](/use/learn/going-further/custom-nodes/#fire-an-event-of-your-own), [Events](/use/learn/interactivity/events/).

## create(String)

Creates a type.
`name` is the name of the listener method, such as `onReset`, printed in the warning of a listener that fails.

## getName()

Returns the name given to `create`.
