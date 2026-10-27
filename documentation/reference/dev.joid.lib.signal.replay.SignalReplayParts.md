---
summary: Text of a concatenation whose parts are known values and holes left by local variables.
---

The replay deduces each hole from the text that the setter received.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## add(Object)

Adds a known part at the end.

## addHole(String,String)

Adds a hole for the local variable `key`, described by `description` in the warnings.

## hasHoles()

If `true`, at least one part is a hole.

## getDescription()

Returns the description of the first hole, or `null` without holes.

## result()

Returns the joined text when there is no hole, otherwise these parts.

## solve(String)

Returns the text of each hole that makes the parts equal to `expected`, or `null` when they cannot.

## isLive()

If `true`, a part depends on a signal.

## setLive(boolean)

Sets whether a part depends on a signal.
