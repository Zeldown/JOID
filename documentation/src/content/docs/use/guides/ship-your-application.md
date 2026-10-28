---
title: "Ship your application"
description: "Package your application on the prod jar, with your fonts, a data folder of the user and what the backend needs."
genre: guide
sidebar:
  order: 8
---

During development, the dev jar inspects, reloads and warns.
The application you ship runs on the prod jar, which has none of these tools: this guide switches to it and checks what changes.

## What you will build

A package of your application that runs on the prod jar of its backend, draws every text with your own fonts, keeps its data in a folder of the user, and starts without the dev tools.

## Before you start

- The jars of JOID and their dependencies: [Installation](/use/get-started/installation/).
- What the dev jar adds: [Dev tools](/use/get-started/dev-tools/).
- The folders where JOID writes: [Where JOID stores files](/use/learn/running/files/).

## 1. Clear the warnings with the dev jar

Before you switch, run every screen with the dev jar until the error output shows no line that starts with `[JOID]`: most warnings exist only in dev mode ([Read the warnings](/use/get-started/dev-tools/#read-the-warnings)).
The lines that JOID prints on the standard output, such as the line it prints when it loads or the load time of each font, are not warnings.

**Check:** a full tour of the application prints no `[JOID]` line on the error output.

## 2. Switch to the prod jar

Give the application the prod jar, and keep the dev jar for your tests, as [Installation](/use/get-started/installation/) shows.

**Check:** run the packaged application: the line that JOID prints when it loads reads `… loaded, data in …`, without `in dev mode`.

## 3. Give every text a font of yours

The dev jar carries a font for its own tools; the prod jar carries none.
Load your fonts from the resources of your application, and give one to the window UI bridge for the tooltips, as [Replace a font](/use/guides/replace-a-font/) shows.

**Check:** every text and every tooltip shows, in your font.

## 4. Choose the data folder

The data folder defaults to `joid` in the working directory, which a launcher or a shortcut can change from one launch to the next.
Set it to a folder of the user on the backend, before `register()`:

```java fragment
final Lwjgl3Backend backend = Lwjgl3Backend
.create(window)
.dataDir(new File(System.getProperty("user.home"), ".my-application"))
.register();
```

The cache folder already defaults to the cache folder of the system ([Where JOID stores files](/use/learn/running/files/)).

**Check:** change a setting, start the application from another directory, and find the setting again.

## 5. Ship what the backend needs

Ship the libraries of your backend with their natives for each target system, as [Installation](/use/get-started/installation/) lists them, and check the prerequisites of your backend, such as the JVM option `-XstartOnFirstThread` on macOS ([Choosing a backend](/use/learn/running/backends/)).

If your application plays videos on machines without network access, put the FFmpeg natives on the classpath ([Play videos without network access](/use/get-started/installation/#play-videos-without-network-access)).

**Check:** the application starts and plays its media on each target system, from the package you ship.

## 6. Keep the files of the JOID jars

The JOID jars carry the shaders that JOID draws with (`assets/shaders/`), and their licenses: `META-INF/LICENSE`, `META-INF/NOTICE`, and the texts of the licenses of the libraries they embed in `META-INF/licenses/`.
If a step of your build repacks the jars, such as a shading or a shrinking tool, keep these folders.

**Check:** the packaged application opens its first UI, and its jar or folder still holds `META-INF/licenses/`.

## If it does not work

1. Tooltips disappeared: the window UI bridge has no `hoverInfo`, and the prod jar prints no warning about it.
2. An image or a video is missing without any message: the resource cannot be read; run the same screen with the dev jar to read the warning.
3. `IllegalStateException: The core shader … is missing from the classpath`: a packaging step dropped `assets/shaders/` of the JOID jar; keep it.
4. Settings come back to their defaults on each launch: the data folder moves with the working directory; set `dataDir(...)`.
5. The application fails at startup on macOS only: the JVM misses `-XstartOnFirstThread`.

## Reference

- [[Backend]]
- [[JOIDEnv]]
- [[WindowUIBridge]]
- [[VideoNatives]]
