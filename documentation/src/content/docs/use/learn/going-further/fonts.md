---
title: "Custom fonts and MSDF"
description: "Write a pixel-art font, a font that draws its glyphs your own way or a font with its own text engine, and generate MSDF atlases before you ship."
genre: learn
sidebar:
  order: 7
---

[Fonts](/use/learn/describing-the-ui/fonts/) loads the `.ttf` and `.otf` files of a font, which covers most interfaces.
This page covers the other cases: letters drawn from a pixel-art atlas, glyphs drawn your own way, a text engine of your own, and MSDF atlases generated before your application ships.

**You will learn**
- How to choose the level of a custom font
- How to write a pixel-art font on a bitmap atlas
- What a glyph font and a raw font must implement
- How to set the weight of a font file yourself
- How to generate MSDF atlases before you ship

## Choose the level

Every font is an [[IFont]], and every [[TextInfo]] takes one.
Pick the highest level that fits: each level down leaves more to you.

| Level | You write | JOID does |
|---|---|---|
| Bitmap font: [[BitmapFont]] and [[BitmapTextRenderer]] | A face, the font, and where each glyph lies in its atlas | Everything a glyph font gets, plus the drawing: nearest texels, lines on the pixel grid, synthetic bold and italic |
| Glyph font: [[GlyphFont]] and [[GlyphTextRenderer]] | A face, the font, and how one glyph is drawn | Weights, italic, kerning, letter spacing, markup, text effects, shadows and measuring |
| Raw font: [[IFont]] and [[ITextRenderer]] | The layout, the drawing and the measuring of a whole line | A font that goes everywhere a `TextInfo` goes |

## Write a pixel-art font

A bitmap font draws its letters from an atlas of pixel-art cells, at the size you ask for, sharp at every scale of the window.
It takes three classes.
The face, an [[IFontFace]], describes one weight and one style of the font:

```java file="com/example/further/FontsPixelFontFace.java" region="face"
```

Every metric is a fraction of the em, measured up from the baseline: the descender and the underline are negative.
This atlas has 16 cells of 8 × 8 texels per row, from the space to `~`, so a font pixel is `1F / 8F` of the em.
`from(image)` measures each cell once, so that a narrow letter advances less than a wide one.
`hasGlyph` decides which characters exist: JOID skips the others, and a space without a glyph advances by a quarter of the em.

The font gives its family, its bitmap size (the number of font pixels per em) and its renderer:

```java file="com/example/further/FontsPixelFont.java" region="font"
```

[[FontFamily]]`.of(faces...)` takes at least one face, and never two faces of the same weight and style.

The renderer tells where each glyph lies in the atlas:

```java file="com/example/further/FontsPixelTextRenderer.java" region="renderer"
```

`getCell` returns a [[BitmapCell]] for the glyph, or `null` to draw nothing.
The renderer reads the texture of the atlas without drawing the resource, so `prepareBind()` decodes and uploads it first ([Framebuffers and models](/use/learn/going-further/framebuffers-and-models/) explains the texture of a resource).
The atlas is loaded with nearest sampling, no mipmaps and no cache, and `blocking()` decodes it on its first use, so the text draws from the first frame ([Images and media](/use/learn/describing-the-ui/media/) explains these settings).

Load the font once, then use it like any other:

```java file="com/example/further/FontsUI.java" region="load"
```

```java file="com/example/further/FontsUI.java" region="use"
```

<figure>

![GAME OVER in large pixel-art letters, then Press any key and Synthetic bold in smaller ones, the last line thicker](/captures/further-fonts-pixel.webp)

<figcaption>
The same 8 × 8 atlas at 64 and 24 design pixels.
The last line asks for a bold weight that the family does not have, and the renderer draws a synthetic bold.
</figcaption>
</figure>

A bitmap font keeps the size you ask for: at 64 design pixels and a bitmap size of 8, a font pixel is 8 design pixels wide.

| `BitmapCell` | What it sets |
|---|---|
| `BitmapCell.create(texture, texelLeft, texelTop, texelRight, texelBottom)` | The atlas and the texels of the glyph. |
| `bounds(left, top, right, bottom)` | Where the cell is drawn, in font pixels from the top of the ascender; the size of the cell in texels by default. Set it when the atlas has more than one texel per font pixel. |
| `grayscale(boolean)` | If `true`, the red channel of the atlas holds the coverage, for an atlas with one channel. |
| `bold(boolean)` | If `true`, the cell is drawn twice, one font pixel apart: a synthetic bold. |

The renderer asks for `bold` when the text asks for a heavier weight than its face.
In dev mode, JOID still warns once that the weight 700 is not loaded in the family: the synthetic bold stands in for a face that does not exist.
An italic text drawn with an upright face is slanted the same way.

## Glyph drawing

A [[GlyphFont]] holds a [[FontFamily]] of faces, and its [[GlyphTextRenderer]] lays out each line: it picks the face of each weight and style, applies the kerning, the letter spacing, the markup and the text effects, then hands you each glyph to draw.
A bitmap renderer is a glyph renderer that draws a cell of an atlas; a glyph renderer of your own draws each glyph its own way.

Its constructor names the built-in shader that draws the glyphs, a value of [[CoreShader]], and JOID compiles it on the first line it draws.
The renderer overrides these methods:

| Method | JOID calls it |
|---|---|
| `drawGlyph(glyph)` | For each glyph of the line, between `begin()` and `end()`. Required. |
| `begin()`, `end()` | Before and after the glyphs of a line: `begin()` binds the shader, `end()` unbinds it. An override calls `super`. |
| `isGridAligned()` | Returns `true` to start lines and baselines on the pixel grid, as pixel-art fonts need; `false` by default. |

A line with a shadow runs these methods twice: once for the shadow, then once for the text.
Inside `drawGlyph`, `getShader()` returns the compiled shader, `getGrid()` the [[PixelGrid]] of the frame, and `bindColor(color)` sets the color of the glyph, a gradient included.

Draw a [[TextGlyph]] from its getters, read in `drawGlyph`: the text effects change them before the call.
`getX()` and `getBaseline()` give the pen position in design pixels, `getOffsetX()` and `getOffsetY()` the offset that an effect adds, `getCodepoint()` the character and `getColor()` the color, the shadow color in the shadow pass; [[TextGlyph]] lists the metrics of the face.

## Know what a raw font must do

When text is not a row of glyphs on a baseline, implement [[IFont]] and its [[ITextRenderer]] yourself.
Markup, text effects, shadows, letter spacing and weights are then yours to implement.

| `ITextRenderer` method | Contract |
|---|---|
| `drawText(x, y, text, info)` | Draws one line with its top-left corner at `x`, `y`, and returns its [[FontBounds]]. |
| `getWidth(text, info)` | The width of the line, equal to what `drawText` draws, or alignment and wrapping go wrong. |
| `getHeight(text, info)`, `getLineHeight(info)` | The height of the line and the distance between two lines. |
| `getBaseline(info)` | The distance from the top of the line to its baseline; the line height by default. |

The overload of `drawText` that also takes the bounds of the whole run receives them so that a gradient spans every line of a text; by default it ignores them.

## Set the weight of a font file

[[MsdfFontLoader]] reads the weight and the italic flag of each file from the file itself.
When a file says the wrong thing, wrap it in a source and say it yourself:

```java file="com/example/further/FontsSources.java" region="override"
```

[[OpenTypeMsdfSource#of]] reads a `.ttf`, `.otf` or `.ttc` file, and [[BinaryMsdfSource#of]] a `.msdf` atlas.
Both extend [[MsdfSource]], which takes `weight(...)` and `italic(...)`.
`load(...)` takes any [[IMsdfSource]] next to files, streams and paths: a source of your own implements `read()` and returns the [[MsdfFontFace]].

## Generate MSDF atlases before you ship

The first time a font file loads on a machine, JOID generates its MSDF atlas and keeps it in the [cache folder](/use/learn/running/files/) ([MSDF text](/use/in-depth/msdf-text/) explains the atlas).
To skip that delay, generate the atlas before you ship, with the MSDF generator ([[MsdfGenerator]]): the jar `joid-tool-msdf-<version>.jar` of each release, which runs on Java 8 and needs nothing else.

```sh fragment
java -jar joid-tool-msdf-<version>.jar --font Montserrat-Regular.ttf --output fonts/Montserrat-Regular
java -jar joid-tool-msdf-<version>.jar --font Montserrat-Bold.ttf --output fonts/Montserrat-Bold
```

Each run writes one `font.msdf` file into its output folder: the weight and the style of the face, the glyph metrics, the kerning pairs and the distance field.
It loads like a font file, one file per face of the family:

```java file="com/example/further/FontsSources.java" region="atlas"
```

| Option | Default | Description |
|---|---|---|
| `--font` | Required | The `.ttf` or `.otf` file of one face. |
| `--output` | `output` | The folder that receives `font.msdf`. |
| `--charset` | `[32, 563]` | The characters to include: codepoints and ranges such as `[32, 126], [160, 255], 8364`, inline or in a file. |
| `--width`, `--height` | `2048` | The size of the atlas, in pixels. |
| `--range` | `24` | The distance range, in atlas pixels: higher is smoother at small sizes. |
| `--size` | Fitted | The em size, in atlas pixels; by default, the largest that fits the atlas. |

For scripts with thousands of characters, include only the ones your texts use: the more characters, the smaller each one in the atlas.

## Recap

- Pick the highest level: **bitmap**, then **glyph**, then **raw** font.
- An **`IFontFace`** gives its metrics as fractions of the em, measured up from the baseline.
- A **bitmap font** needs a face, a `BitmapFont` with its bitmap size, and a renderer whose **`getCell`** finds each glyph in the atlas.
- A **glyph renderer** draws each `TextGlyph` in `drawGlyph`; JOID lays out the line, the markup and the effects.
- A **raw font** measures exactly what it draws.
- **`OpenTypeMsdfSource`** sets the weight or the italic flag of a file, and the **MSDF generator** writes the atlases before you ship.

## Try it

**A monospaced pixel font.**
Give every glyph of the pixel font the same advance of 8 font pixels.

<details>
<summary>Solution</summary>

In `getAdvance`:

```java fragment
return this.hasGlyph(codepoint) ? 1F : 0F;
```

The em is 8 font pixels, so an advance of `1F` is 8 font pixels: narrow letters now sit alone in their cell.

</details>

**Latin only.**
Generate an atlas with the printable ASCII characters only.

<details>
<summary>Solution</summary>

```sh fragment
java -jar joid-tool-msdf-<version>.jar --font Montserrat-Regular.ttf --output fonts/Latin --charset "[32, 126]"
```

Fewer characters leave more room to each one in the atlas, so each glyph is drawn from more pixels.

</details>
