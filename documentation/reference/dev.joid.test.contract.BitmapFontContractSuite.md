---
summary: The contract tests of the drawing of bitmap fonts by a render bridge: whole texels and the same ink at every scale.
---

Learn: [The suites](/build/testing-a-backend/#the-suites).

## BitmapFontContractSuite()

Creates the suite, for a subclass.

## startBackend()

Creates the snapshot backend at the first test of the class, at 128×64 pixels, and resets the matrices and the state before each test.

## stopBackend()

Destroys the snapshot backend after the last test of the class.

## drawsWholeTexelsAtAWholeNumberOfPixels()

Checks that a glyph scaled by 1, 2 or 0.5 covers whole texels, without blur.

## givesEveryTexelColumnTheSameInkAtAFractionalScale()

Checks that at a fractional scale every column of texels of a glyph gets the same amount of ink.

## drawsTheSameGlyphFromEveryAtlas()

Checks that a glyph gives the same pixels from any atlas that holds it.

## createBackend()

JOID calls it once, at the first test of the class.
Return a new snapshot backend of your backend, not created yet.
