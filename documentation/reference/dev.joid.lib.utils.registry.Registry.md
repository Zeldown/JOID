---
summary: Ordered list of registered values in which the most recent registration wins.
---

The registries of formats, resolvers, asset locators, markups, converters and key resolvers use it: a value registered later is tried first.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## register(T)

Puts `value` first; a value already registered moves to the front.

## unregister(T)

Removes `value`.

## find(Predicate)

Returns the first value, from the most recent, that matches `predicate`, or `null`.

## getAll()

Returns the values from the most recent, read-only; the view follows the registrations.
