---
summary: An event about a node, and the event of the node events that carry nothing else, such as `onInit` or `onDetach`.
---

[[Node#onInit]], [[Node#onUpdate]], [[Node#onMount]], [[Node#onDetach]], the playback events of [[ResourcePlayerNode]] and [[LineFieldNode#onSubmit]] receive a plain `NodeEvent`; the other node events extend it.

Learn: [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens), [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/).

## create(N)

Creates an event about a node, for a node that fires an event of its own.

## NodeEvent(N)

Creates an event about a node, from the constructor of a subclass.

## getNode()

Returns the node that the event concerns, typed as the node that the listener was added on.
