---
title: "State"
description: "Compute values from signals, rebuild the parts of the tree that come and go, keep values across UIs and launches, and change them over time."
genre: learn
sidebar:
  order: 0
---

An interface shows values that change: a total, a list, a setting, a status that arrives from a server.
In JOID, `init()` builds the tree once per opening, and the values that change live in [signals](/use/learn/interactivity/signals/).
This chapter builds on signals: it computes values from them, rebuilds the nodes that come and go, keeps values that several UIs or several launches share, and changes them over time.

## Derived values

A derived value is computed once from other signals, and behaves like a signal of its own.

```java file="com/example/state/DerivedUI.java" region="from"
```

Read [Derived values](/use/learn/state/derived-values/) to learn `map`, [[Signal#from]], `batch`, and how to run your own code when a value changes.

## Rebuilding the tree

When a change adds or removes nodes, a node builds its children again; when the data is not there yet, a node waits behind a placeholder.

```java fragment
.watch(this.items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
```

Read [Rebuilding the tree](/use/learn/state/rebuilding/) to learn `watch`, `wait` and skeletons.

## Stores and remembered properties

A store shares values between UIs and can keep them on disk; a remembered property keeps one field of a UI across launches.

```java file="com/example/state/SettingsUI.java" region="use"
```

Read [Stores and remembered properties](/use/learn/state/stores-and-properties/) to learn where a value should live and how to write a store.

## Tasks and background updates

A task runs your code after a delay or on a period, and a signal set from another thread takes its value at the next frame.

```java file="com/example/state/TimerUI.java" region="every"
```

Read [Tasks and background updates](/use/learn/state/tasks/) to learn [[Task]] and how a worker thread hands its results to the interface.
