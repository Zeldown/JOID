# How Fonts Work

This page explains what a font is in JOID, how a family picks the face to draw, and how the MSDF fonts that JOID ships keep text sharp at any size. Read it before [Adding Your Own Fonts](adding-fonts.md) to choose how you provide your fonts; you do not need any font rendering background.

## The short version

```java
final MsdfFont inter = MsdfFontLoader.load(
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Regular.ttf"),
    Fonts.class.getResourceAsStream("/assets/fonts/Inter-Bold.ttf")
).join();

final TextInfo body = TextInfo.create(inter, 18, Color.WHITE);
final TextInfo title = TextInfo.create(inter, FontWeight.BOLD, 32, Color.WHITE);
```

- `MsdfFontLoader.load(...)` reads font files and returns a family, an `MsdfFont`.
- A [`TextInfo`](../text/text-and-textinfo.md) refers to the family and asks for a weight and italic flag; the family chooses the face that draws it.
- The first load of a `.ttf` file turns it into an MSDF atlas (a texture of distance fields) and caches it; the next loads read the cache.

The rest of the page explains each of these pieces.

## The font abstraction: IFont and IFontProvider

Everything that draws or measures text (`TextNode`, `DrawUtils.TEXT`, `TextInfo.getWidth`, the text fields) knows only two interfaces of `dev.joid.lib.font`:

| Interface | Role |
|---|---|
| `IFont` | What a `TextInfo` holds. Its only method, `getFontProvider()`, returns the provider that draws it. |
| `IFontProvider` | Draws one line of text (`drawText`) and measures it (`getWidth`, `getHeight`, `getLineHeight`). |

A font is therefore any object that can hand over a provider. JOID ships one implementation, the MSDF font, and you can write your own (see [Custom Font Implementations](custom-fonts.md)).

```
TextInfo ──> IFont ──getFontProvider()──> IFontProvider ──> draws and measures
```

## Glyph fonts with GlyphFont

Most fonts are made of glyphs: one shape per character, placed one after the other on a baseline. `dev.joid.lib.font.impl.glyph` implements everything such fonts share, so an implementation only has to say how one glyph is drawn:

| Concept | Type | What it is |
|---|---|---|
| Face | `IFontFace` | One weight and style of a font, usually one file: Inter Bold, Inter Italic. It knows its characters, their advances, kerning and metrics. |
| Family | `FontFamily<F>` | The faces of one font, like a CSS `font-family` made of `@font-face` rules. |
| Font | `GlyphFont<F>` | The `IFont` built on a family: `getFace(weight, italic)` and `getFamily()`. |
| Provider | `GlyphFontProvider<F>` | Lays out the glyphs (advances, kerning, letter spacing, markup, effects, shadow) and calls the implementation for each glyph. |

The MSDF classes extend them: `MsdfFont` is a `GlyphFont<MsdfFontFace>`, drawn by the shared `MsdfFontProvider`.

```
MsdfFont (GlyphFont)
└── FontFamily<MsdfFontFace>
    ├── MsdfFontFace  Inter Regular  400
    ├── MsdfFontFace  Inter Italic   400 italic
    └── MsdfFontFace  Inter Bold     700
```

A family refuses to be empty and refuses two faces of the same weight and style (`IllegalArgumentException`).

### Choosing a face with FontFamily.resolve

A `TextInfo` (or markup) asks for a weight and an italic flag, and the family resolves the face to draw:

1. The faces of the requested style (italic or upright) are the candidates. When the family has no face of that style, every face is a candidate.
2. Among the candidates, the face with the closest weight wins.
3. At equal distance, a request of 400 or above 500 takes the heavier face, any other request takes the lighter face.

With only Light (300) and Bold (700) loaded:

| Requested | 100 | 200 | 300 | 400 | 500 | 600 | 700 | 800 | 900 |
|---|---|---|---|---|---|---|---|---|---|
| Drawn | 300 | 300 | 300 | 300 | 300 | 700 | 700 | 700 | 700 |

![A sentence requested in the nine weights from a family with only Light and Bold: 100 to 500 draw Light, 600 to 900 draw Bold](../images/font-resolve.png "MsdfFont.create(Montserrat Light, Montserrat Bold) asked for each FontWeight.")

The style comes before the weight: with Regular upright and Bold italic loaded, an italic Regular request draws the Bold italic face.

When the drawn weight is not the requested one, dev mode prints a warning once per requested weight (see [Missing weight warnings](adding-fonts.md#missing-weight-warnings)).

### Italic without an italic face

When the face drawn is upright but the text asks for italic, the glyphs are sheared by 0.2 (about 11°), so `italic(true)` always shows. An italic face is drawn as is. A real italic face has its own letter shapes; load it when the typography matters.

## MSDF fonts explained

### The problem with bitmap text

The simplest way to draw text is to render each character once into a small image and paste that image wherever the character appears. It looks right at the size it was rendered at, and only there: enlarged, the pixels turn blurry or blocky; reduced, thin strokes vanish. A UI that scales with the window, animates or zooms would need one image set per size.

### Signed distance fields

A signed distance field (SDF) stores, instead of the color of each pixel, its distance to the outline of the character: positive inside, negative outside (or the opposite), zero exactly on the edge. When the texture is enlarged, the graphics card interpolates between neighboring texels, and distances interpolate well: the zero line, which is the outline, stays a clean curve. The shader then turns each screen pixel into coverage from the distance: fully inside, fully outside, or a smooth edge one pixel wide. The result is sharp at any size, scale and rotation from one small texture.

![The letters Ag in Montserrat Bold at 16, 64 and 256 pixels](../images/msdf-sizes.png "The same atlas draws every size: 16, 64 and 256 pixels.")

### Why multi-channel

A single distance rounds off sharp corners, because near a corner the distance to the outline is the distance to a rounded shape. A multi-channel signed distance field (MSDF) stores three distances in the red, green and blue channels, each computed from a different subset of the outline edges. The shader takes the median of the three values, which rebuilds sharp corners. This is why the `.msdf` atlases are RGB images.

![A 4x magnified crop of the 256-pixel g: a smooth curve and a sharp square corner with a one-pixel soft edge](../images/msdf-zoom-large.png "A crop of the 256-pixel g magnified 4×: the curve stays smooth and the corner stays square.")

The JOID generator colors the edges of each outline between the three channels, computes the three distances for every texel, then corrects the texels where the channels clash with their neighbors or where the median falls on the wrong side of the outline, so no stray artifacts appear when the field is sampled.

### The atlas

All the glyphs of a face are packed into one texture, the atlas, together with the data needed to place them:

| Part | Content |
|---|---|
| Image | RGB distance fields of every drawable glyph, 2048×2048 pixels by default. |
| Em size | Size of one em in atlas pixels. The generator picks the largest size that fits all the glyphs. |
| Distance range | How far from the outline the field still holds a distance, 24 atlas pixels by default. |
| Glyphs | For each character: its advance, its bounds on the baseline and its rectangle in the atlas. |
| Metrics | Line height, ascender, descender, underline position and thickness. |
| Kerning | The spacing adjustments of character pairs. |

![Colored distance fields of five glyphs of Montserrat Bold above the shapes rebuilt from the median of their channels](../images/msdf-atlas.png "A crop of the real Montserrat Bold atlas from the MSDF cache (2× scale), and below it the median of the three channels thresholded at the outline.")

A `.ttf` or `.otf` file holds outlines, not distance fields, so its atlas must be generated once. `MsdfFontLoader` does it at runtime and keeps the result in a cache; the [MSDF Generator](msdf-generator.md) does it ahead of time into a `font.msdf` file you ship. `MsdfFontFace.getAtlas()` returns the `MsdfAtlas` of a loaded face (`getWidth()`, `getHeight()`, `getSize()`, `getDistanceRange()`).

### How JOID draws a glyph

1. The provider lays out the line: advance of each character, kerning, letter spacing, markup, effects.
2. Each glyph is one textured quad that samples its rectangle of the atlas (linear filtering, no mipmaps).
3. The MSDF shader converts the distance range into screen pixels for the current scale, samples the field four times per pixel, and turns the median distance into coverage. Colors and gradients are applied in the same shader.
4. When the transform is axis-aligned, the baseline and the x-height of the text land on whole window pixels, which keeps small text sharp; rotated or skewed text keeps its exact geometry.

![The word Ag at 16 pixels magnified 10 times: crisp stems with soft one-pixel edges](../images/msdf-zoom-small.png "16-pixel text magnified 10×: the stems and the baseline sit on whole pixels.")

The MSDF font shader must compile on the backend: drawing throws `IllegalStateException` "The msdf font shader is not usable" otherwise.

### Trade-offs

| Strength | Cost |
|---|---|
| One atlas serves every size, scale and rotation. | An atlas holds a fixed set of characters; a character outside it is skipped. |
| Sharp corners, unlike a single-channel SDF. | One 2048×2048 texture per face by default. |
| One quad per glyph and one shader, cheap to draw. | Generating an atlas takes seconds per face (cached, or done ahead of time with the generator). |
| Colors, gradients, shadows and effects at no extra texture cost. | The more characters share an atlas, the smaller the em size, and the less precise fine details of the outlines become. |

## Kerning

Some pairs of letters look too far apart with their plain advances: `AV`, `To`, `Ye`. Kerning is a per-pair adjustment, stored in the font, that moves the second letter closer (or further).

- The atlas keeps the kerning of the font: the `GPOS` pair positioning, or the legacy `kern` table when `GPOS` gives no pair (details on [MSDF Generator](msdf-generator.md#kerning)).
- Only pairs of two characters of the atlas are kept.
- Kerning applies when text is drawn and when it is measured, scaled to the font size, so measured widths match what is drawn.
- Kerning restarts when markup switches to another face.

`MsdfFontFace.getKerning(previous, current)` returns the value of a pair as a fraction of the em.

## Missing characters

A character missing from the face is skipped: it is neither drawn nor measured, and kerning continues from the previous character. An atlas generated at runtime covers the characters 32 to 563: Basic Latin, Latin-1, Latin Extended-A and part of Latin Extended-B. For other scripts or symbols, generate an atlas with your own charset (see [Charsets](msdf-generator.md#charsets)).

## Bundled fonts

The `dev` jars ship fonts for the developer tools and the demo UIs, under the SIL Open Font License. The `prod` jars contain none of them: ship your own fonts.

| Field | Faces | Loaded when |
|---|---|---|
| `InternalFont.MONTSERRAT` (`dev.joid.internal.font`) | Montserrat, the nine upright weights | `JOID.load()` in dev or demo mode |
| `DemoFont.MONTSERRAT` (`dev.joid.demo`) | The same family | `JOID.load()` in demo mode |
| `DemoFont.PACIFICO` | Pacifico Regular | `JOID.load()` in demo mode |
| `DemoFont.PLAYFAIR_DISPLAY` | Playfair Display | `JOID.load()` in demo mode |

They are `.ttf` files, so their atlases are generated into the [MSDF cache](adding-fonts.md#the-msdf-cache-with-msdffontcache) on the first launch. `DemoFont.isLoaded()` tells whether the demo fonts are ready. See [Developer Tools](../getting-started/dev-tools.md).

## Choosing how to provide a font

| You want | Use | Page |
|---|---|---|
| To get started, with Latin text and a few faces | Load the `.ttf`/`.otf`/`.ttc` files at runtime with `MsdfFontLoader`. The first launch on each machine generates the atlases (seconds per face), later launches read the cache. | [Adding Your Own Fonts](adding-fonts.md) |
| A fast first launch, a release build, other characters (Cyrillic, Greek, CJK, symbols) or another atlas size | Generate `font.msdf` atlases with the MSDF Generator and ship them; load them with the same `MsdfFontLoader.load(...)`. | [MSDF Generator](msdf-generator.md) |
| A face whose weight or style metadata is wrong, or a family assembled from unrelated files | Wrap the handle in `MsdfOpenTypeSource` or `MsdfBinarySource` and override the weight or italic flag. | [Adding Your Own Fonts](adding-fonts.md#overriding-a-face-with-msdfsource) |
| A pixel-art or sprite font, or glyphs drawn another way | Extend `GlyphFont` and `GlyphFontProvider`; families, kerning, markup and effects come for free. | [Custom Font Implementations](custom-fonts.md) |
| A completely different text engine | Implement `IFont` and `IFontProvider`. | [Custom Font Implementations](custom-fonts.md) |

## See also

- [Adding Your Own Fonts](adding-fonts.md)
- [MSDF Generator](msdf-generator.md)
- [Custom Font Implementations](custom-fonts.md)
- [Text and TextInfo](../text/text-and-textinfo.md)
- [Styling Text](../text/styling-text.md)