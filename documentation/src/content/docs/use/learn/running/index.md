---
title: "Running your UI"
description: "Run a JOID interface: backends, your own loop, threads, files and tests."
genre: learn
sidebar:
  order: 0
---

A UI needs a window, a loop and a graphics library to appear on screen.
JOID leaves the window and the loop to your application and runs inside them through a backend.
This chapter covers everything around your UIs: starting JOID, living in your loop, threads, files on disk and tests.

## Choosing a backend

A backend runs JOID on LWJGL 3, Vulkan or LWJGL 2.
You create the window, register the backend once, and call `frame()` in your loop.

```java file="com/example/running/RunningApp.java" region="register"
```

Read [Choosing a backend](/use/learn/running/backends/) to learn which backend fits, what `register()` does, and how to style the window bridge.

## Embedding JOID

Your loop can draw a scene of its own, keep its own input handlers, and host several UI bridges with priorities.

```java fragment
BridgeHandler.UI.register(panels, 10);
```

Read [Embedding JOID](/use/learn/running/embedding/) to learn how input and drawing share the window with your application.

## Threads

JOID runs on one thread, the one that registers the backend and calls `frame()`.
Work done elsewhere comes back to it:

```java fragment
ThreadUtils.runOnRenderThread(() -> label.text(Text.create(result, info)));
```

Read [Threads](/use/learn/running/threads/) to learn what other threads may call and how to bring their results back.

## Where JOID stores files

JOID writes stores and remembered fields in a data folder, and font atlases and natives in a cache folder.

```java fragment
Lwjgl3Backend.create(window).dataDir(new File(home, "data")).register();
```

Read [Where JOID stores files](/use/learn/running/files/) to learn where the folders are, how to move them and how to keep files of your own in the cache.

## Testing your UIs

The testkit renders a UI offscreen, plays clicks and keys on it, and returns the pictures.

```java fragment
runner.execute("ui " + MenuUI.class.getName(), "moveto 960 540 300", "press LEFT", "release LEFT", "shot clicked");
```

Read [Testing your UIs](/use/learn/running/testing/) to learn how to write screen tests that give the same pixels on every run.
