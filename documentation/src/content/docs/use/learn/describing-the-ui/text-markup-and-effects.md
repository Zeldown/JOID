---
title: "Text markup and effects"
description: "Style parts of a string with markup, change glyphs with text effects, transform a text with modifiers, and show your own types with converters."
genre: learn
sidebar:
  order: 12
---

A style applies to a whole run of text, but a sentence often needs a bold word, an underline or a wave.
JOID gives four hooks into the text it draws: markup reads tags in the string, effects change the glyphs, modifiers rewrite the string, and converters turn your own objects into strings.
JOID ships none of them ready-made: you write the ones your interface needs, in a few lines each.

**You will learn**
- How to style parts of a string with a markup
- How to change the glyphs with a text effect
- How to transform a text with a modifier
- How to show your own types as text

## Style parts of a string

A markup is an [[ITextMarkup]]: JOID calls its `parse` method at each position of the string.
When a tag starts there, the markup changes the [[TextStyle]] of the glyphs that follow and returns the length of the tag; otherwise it returns `0`:

```java file="com/example/describing/markup/TagTextMarkup.java" region="class"
```

[[TextStyle#getBase]] gives the style of the [[TextInfo]], so a closing tag restores what the opening tag changed.
A tag changes the weight, the slant, the color, the font or the effects of what follows (the setters of [[TextStyle]]), and JOID neither draws nor measures its characters.

Give the markup to a style with [[TextInfo#markups]] or [[TextInfo#withMarkups]]:

```java file="com/example/describing/markup/MarkupUI.java" region="markup"
```

<figure>

![A dark card with four light lines: Read the terms, then accept them, with terms in bold and accept in blue and underlined; A wave moves every glyph, its letters rising and falling along a wave; LOUD AND CLEAR in capitals with LOUD in bold; 1 min 35 s](/captures/describing-markup-lines.webp)

<figcaption>
Each hook works on one stage of the text: the tags, the glyphs, the string, then your own objects.
The tags of the first line and of the third line are not drawn.
</figcaption>
</figure>

A style without its own markups uses the markups registered for the whole application with [[TextMarkup#register]], so a tag works in every text.
`markups()` with no argument turns markup off for one style: the tags then show as plain text.
A text that wraps or that is cut keeps the style that a tag opened, on every line.

`colored(false)` on the `TextInfo` ignores the colors that tags set, and draws every glyph in the color of the style.

## Change the glyphs with a text effect

A text effect is an [[ITextEffect]] with three hooks, each called once per glyph:

| Hook | When JOID calls it | Use it to |
|---|---|---|
| `background(glyph)` | Before the glyphs are drawn | Draw under the text, such as a highlight |
| `apply(glyph)` | Before the glyph is drawn | Move it, recolor it or swap its character |
| `decorate(glyph)` | After the glyphs are drawn | Draw over the text, such as an underline |

The [[ITextGlyph]] gives the position, the size, the baseline and the underline of the glyph, and lets `apply` change it with `offset`, `color` and `codepoint`:

```java file="com/example/describing/markup/WaveTextEffect.java" region="class"
```

Give effects to a style with [[TextInfo#effects]] or [[TextInfo#withEffects]], for the whole text:

```java file="com/example/describing/markup/MarkupUI.java" region="effects"
```

A markup adds an effect to part of a string with `style.effect(...)`, as `<em>` does with this underline:

```java file="com/example/describing/markup/UnderlineTextEffect.java" region="class"
```

`decorate` draws the line with [[DrawUtils]], the drawing API that [Drawing shapes and text](/use/learn/going-further/drawing/) teaches.

An effect never changes the layout: the text keeps its width and its line breaks, wherever `apply` moves the glyphs.
An effect that reads the time in `apply` animates, because JOID draws the text again on every frame.
A style with a shadow draws the glyphs twice: `apply` moves the shadow with its glyph, and `decorate` runs for the shadow too, where [[ITextGlyph#isShadow]] is `true`.

## Transform a text with a modifier

A [[TextModifier]] rewrites the string before it is drawn and measured.
[[Text#modifier]] applies one to the whole text:

```java file="com/example/describing/markup/MarkupUI.java" region="modifier"
```

The modifier receives the text of every run as one string, and leaves the tags of the markup untouched.
`TextModifier` holds the common ones: `UPPER_CASE`, `LOWER_CASE`, `CAPITALIZE`, `WORD_CAPITALIZE`, `CAMEL_CASE`, `UPPER_CAMEL_CASE` and `SNAKE_CASE`.
Any lambda from a string to a string is a modifier too: `text -> text.replace("-", " ")`.

[[TextElement#modifier]] applies a modifier to one run only.
[[Text#getRawText]] returns the text before any modifier.

## Show your own types as text

A text can be any object: JOID turns it into a string through the registered [[ITextConverter]]s, or with `toString()` when none supports it.
A converter says which objects it supports, then converts them:

```java file="com/example/describing/markup/DurationTextConverter.java" region="class"
```

Register it once, when your application starts, with [[TextConverter#register]]:

```java file="com/example/describing/markup/MarkupUI.java" region="register"
```

From then on, a `Duration` given as a text shows in your format:

```java file="com/example/describing/markup/MarkupUI.java" region="converter"
```

The last converter registered is tried first.
A string never goes through a converter.

## Recap

- An **`ITextMarkup`** reads tags in the string and changes the **`TextStyle`** of what follows; its tags are neither drawn nor measured.
- A style uses its own markups (`markups(...)`) or the markups **registered** for the application; `markups()` turns them off.
- An **`ITextEffect`** changes glyphs in three hooks, **`background`**, **`apply`** and **`decorate`**, without changing the layout.
- A **`TextModifier`** rewrites the whole text, tags kept; a run can have its own.
- An **`ITextConverter`**, registered once, turns your own objects into strings.

## Try it

**A highlight.**
Write an effect that draws a grey rectangle under each glyph, from the top of the line to its bottom.

<details>
<summary>Solution</summary>

Draw in `background`, before the glyphs: the ascender and the descender are measured from the baseline.

```java file="com/example/describing/markup/HighlightTextEffect.java" region="background"
```

</details>

**Raw tags.**
A help screen shows the syntax of your markup: `<b>bold</b>`, with its tags visible.
How do you draw it without changing the registered markups?

<details>
<summary>Solution</summary>

Give that text a style with no markup at all: `info.withMarkups()`.
Its tags show as plain text, while every other text keeps the registered markups.

</details>
