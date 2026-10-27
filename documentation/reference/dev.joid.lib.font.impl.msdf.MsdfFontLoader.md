---
summary: Loads an MSDF font from the files of its faces, in the background, and completes a future with the MsdfFont.
---

Load each font once, when your application starts, and keep the [[MsdfFont]] in a field:

```java fragment
MsdfFontLoader.load("fonts/Inter-Regular.ttf", "fonts/Inter-Bold.ttf").thenAccept(font -> this.font = font);
```

A TrueType or OpenType file generates its MSDF atlas on the first load and reads it from the cache folder afterwards; a `.msdf` file made by the msdf tool loads as it is.
The future completes through `runOnRenderThread` of the thread bridge, which with JOID's backends runs on the thread that finished loading ([Threads](/use/learn/running/threads/)); `join()` on the render thread waits for the load there.

Learn: [Fonts](/use/learn/describing-the-ui/fonts/), [MSDF text](/use/in-depth/msdf-text/).

## load(Object[])

Reads every face in parallel and completes with one [[MsdfFont]] that holds them all.
Each argument is an [[IMsdfSource]], or anything [[Asset#of]] accepts (a path, a `File`, a URL, a stream, an [[Asset]]): its first bytes pick [[OpenTypeMsdfSource]] or [[BinaryMsdfSource]].
The weight and the style of each face come from the file; wrap the file in a source and call `weight(...)` or `italic(...)` to override them.
The future completes exceptionally with `IllegalArgumentException` when no face is given, two faces share a weight and a style, or a file cannot be read or generated.
In dev mode, the time each face took is logged.
