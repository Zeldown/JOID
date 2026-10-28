---
title: "Fonts"
description: "Load the fonts of your application, let a style pick a face, and choose between MSDF and bitmap fonts."
genre: learn
sidebar:
  order: 11
---

Every text needs a font, and your application brings its own.
This page shows how to load a font family once, how a style picks one of its faces, and which kind of font to choose.

**You will learn**
- How to load a font family for the whole application
- How a style picks a face of the family
- How to load a font without blocking
- Which kind of font to choose: MSDF or bitmap

## Load a font family

[[MsdfFontLoader#load]] reads font files and builds one family, an [[MsdfFont]], from them.
Pass every face you use, regular, bold, italic, in one call:

```java file="com/example/describing/fonts/AppFonts.java" region="class"
```

The family works in any [[TextInfo]] style: `TextInfo.create(AppFonts.MONTSERRAT, 32F)` ([Text](/use/learn/describing-the-ui/text/#describe-a-style)).
Keep your fonts in one class, and load each family once, after the backend is registered ([Run code once JOID is ready](/use/learn/running/backends/#run-code-once-joid-is-ready)).
A `static final` field loads when its class is first used: use `AppFonts` only after `register()`, as the UIs do.

Each face is a `.ttf` or `.otf` file, or an `.msdf` atlas made in advance.
`load(...)` takes it as a path in the classpath or on the disk, a URL with its scheme, a `File` or an `InputStream`.

The first load of a font file generates an atlas, which takes a moment once; the next runs read it from the cache ([What happens when a font loads](/use/in-depth/msdf-text/#what-happens-when-a-font-loads)).

:::caution
JOID ships no font for your application: every style needs a font that you load.
The font that JOID draws its dev tools with is loaded only in dev mode: never build a style on it.
:::

## Let a style pick a face

A family holds several faces, each with a weight and an italic flag read from its file.
A style asks for a weight and a slant, and the family draws the face that matches:

```java file="com/example/describing/fonts/TitleUI.java" region="weights"
```

<figure>

![A dark card with three light lines: Regular 400 in regular weight, Bold 700 in bold, and Italic, slanted in a slanted regular face](/captures/describing-fonts-weights.webp)

<figcaption>
One family loaded with a Regular and a Bold face.
The italic line has no italic face to draw, so the regular face is slanted.
</figcaption>
</figure>

- When the family has no face of the weight you ask for, it draws the closest one, and dev mode prints a warning that names the missing weight and the loaded ones: load every weight you use.
- `italic(true)` draws the italic face closest to the weight; a family without an italic face slants its upright face.
- Two faces of the same weight and slant in one `load(...)` call throw an `IllegalArgumentException`.

A face whose file declares the wrong weight can be given another one: [Custom fonts and MSDF](/use/learn/going-further/fonts/) shows the MSDF sources that do it.

## Load a font without blocking

`load(...)` reads the files in the background and returns a `CompletableFuture`.
`join()` waits for it, as in `AppFonts` above.
To keep the window responsive while a large font loads, react to the future instead:

```java file="com/example/describing/fonts/LoadingUI.java" region="async"
```

The UI opens at once with an empty card, and the text appears when the font is ready.
JOID completes the future on the render thread, so the code that reacts to it may attach nodes, open and close UIs ([Threads](/use/learn/running/threads/)).

## Choose MSDF or bitmap

JOID draws two kinds of fonts:

| | MSDF font | Bitmap font |
|---|---|---|
| Made from | A `.ttf`, `.otf` or `.msdf` file | Glyphs drawn on a pixel grid |
| At any size | Sharp, even scaled, zoomed or rotated | Drawn from its pixels: crisp at its own size, made for pixel art |
| Use it for | Every interface text | Pixel-art interfaces |

`MsdfFontLoader` loads an MSDF font, the right choice in almost every case.
A bitmap font is a [[BitmapFont]] that you write for your own glyphs: [Custom fonts and MSDF](/use/learn/going-further/fonts/) shows how, and [MSDF text](/use/in-depth/msdf-text/) explains how MSDF keeps the edges sharp.

## Recap

- **JOID ships no font** for your application: load your own, once, after the backend is registered.
- **`MsdfFontLoader.load(...)`** builds a **family** from every face you pass.
- A style asks for a **weight**; the family draws the **closest face**, with a dev warning when it is missing, and slants an upright face for italic.
- `load(...)` returns a **`CompletableFuture`**: `join()` waits, `thenAccept` reacts on the render thread.
- The first load of a font file generates its atlas once, then reads it from the **cache**.
- **MSDF** fonts stay sharp at every size; **bitmap** fonts are for pixel art.

## Try it

**Add a weight.**
`AppFonts` holds Regular and Bold.
A style asks for `SEMI_BOLD` (600): which face is drawn, and what does dev mode print?

<details>
<summary>Solution</summary>

The closest face, Bold (700), is drawn.
Dev mode prints once that the weight 600 is not loaded in the family, that 700 is drawn instead, and which weights are loaded.
Load the semi-bold file in the same `load(...)` call to draw it.

</details>

**Fail visibly.**
In `LoadingUI`, show a message in the card when the font does not load.

<details>
<summary>Solution</summary>

`exceptionally` runs when the future fails; give the message a font that is already loaded.

```java fragment
.exceptionally(error -> {
	TextNode.create(50, 70).text(Text.create("No font", TextInfo.create(AppFonts.MONTSERRAT, 40F))).attach(card);
	return null;
});
```

</details>
