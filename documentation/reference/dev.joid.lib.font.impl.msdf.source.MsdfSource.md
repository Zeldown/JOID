---
summary: Base class of the MSDF sources, which can override the weight and the style that the file declares.
---

```java fragment
MsdfFontLoader.load(OpenTypeMsdfSource.of("fonts/Logo.ttf").weight(FontWeight.BOLD));
```

Learn: [Fonts](/use/learn/going-further/fonts/).

## italic

If set, the face read by this source declares this style instead of the one of its file.

## weight

If set, the face read by this source declares this [[FontWeight]] instead of the one of its file.

## read()

Reads the face with `parse()`, then applies the weight and the style set on this source.

## parse()

JOID calls it from `read()` on a loader thread: read the face as its file describes it.
Throw `IOException` when it cannot be read.
