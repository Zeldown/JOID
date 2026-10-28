---
title: "Replace a font"
description: "Load one font family, define the text styles of your application from it, and use them in every UI and tooltip."
genre: guide
sidebar:
  order: 4
---

JOID ships no font for your application: every text needs a font that you load.
This guide loads one family once, defines the text styles of the application from it, and uses them in every UI and in the tooltips.

## What you will build

<figure>

![A dark card with the title Settings in bold, a line of regular text and a blue Save button in dark text; the pointer rests on the button, and a dark tooltip that reads Saves the settings appears under it](/captures/guides-font-card.webp)

<figcaption>
One family in two weights, for the title, the body text, the button and the tooltip.
</figcaption>
</figure>

## Before you start

- Text nodes and text styles: [Text](/use/learn/describing-the-ui/text/).
- Loading a font and its weights: [Fonts](/use/learn/describing-the-ui/fonts/).
- The font files (`.ttf` or `.otf`), one per weight, in the resources of your application.
  This guide uses Montserrat Regular and Bold.

## 1. Load the font once

Load every face of the family in one call to [[MsdfFontLoader#load]], in a constant that the whole application shares:

```java file="com/example/guides/FontGuideStyle.java" region="font"
```

`join()` waits until the faces are ready.
The first launch on a machine generates a cache for each face, which takes a few seconds; later launches read it ([What happens when a font loads](/use/in-depth/msdf-text/#what-happens-when-a-font-loads)).

**Check:** run any UI that reads `FontGuideStyle.FONT`: the dev jar prints `[JOID] Font … generated into the msdf cache` for each face on the first run, then `read from the msdf cache`.

## 2. Define the styles of the application

Define each [[TextInfo]] once, next to the font, and lock it with `lock()`:

```java file="com/example/guides/FontGuideStyle.java" region="styles"
```

A locked style refuses its setters, and a variant is a `with...` copy ([Describe a style](/use/learn/describing-the-ui/text/#describe-a-style)).

**Check:** with the red of [[Color]], `FontGuideStyle.BODY.color(Color.RED)` throws an `IllegalStateException`, and `FontGuideStyle.BODY.withColor(Color.RED)` returns a red copy.

## 3. Use the styles in your UIs

Every text takes one of the styles, and the dark text of the button starts from a copy:

```java file="com/example/guides/FontGuideUI.java" region="use"
```

**Check:** the title is bold, the body text regular, and the text of the button dark.

## 4. Use the font for the tooltips

The window UI bridge draws tooltips only once it has a text style.
Give it one after the backend is registered, for example when your first UI opens:

```java file="com/example/guides/FontGuideUI.java" region="tooltips"
```

[Style the window UI bridge](/use/learn/running/backends/#style-the-window-ui-bridge) lists the other settings of the tooltips.

**Check:** hover the Save button: its tooltip shows in Montserrat.

## If it does not work

1. The first access to `FontGuideStyle` throws an `ExceptionInInitializerError` caused by `IllegalArgumentException: The asset … is not found in the classpath nor as a file`: the path is wrong.
   Check that the files are in the resources, under that path from their root (a leading `/` changes nothing).
2. `join()` throws a `CompletionException` that says the atlas cannot be generated: the file is not a valid TrueType or OpenType font, or the cache folder cannot be written.
3. A text shows in another weight, and the dev jar prints `[JOID] The font weight … is not loaded in the family of …`: the style asks for a weight that the family lacks; load that face, or use a loaded weight.
4. No tooltip appears, and the dev jar prints `[JOID] WindowUIBridge draws no tooltip, set its hoverInfo(TextInfo) to draw them`: the window UI bridge has no text style yet.
5. A setter on a shared style throws `IllegalStateException: This TextInfo is locked`: the style is locked on purpose; call its `with...` copy instead.

## Reference

- [[MsdfFontLoader]]
- [[TextInfo]]
- [[FontWeight]]
- [[WindowUIBridge]]
