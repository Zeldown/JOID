---
summary: An RGBA color with channels on 1, which can also hold a linear gradient or change on every frame.
---

Every color setter of JOID takes a `Color`.
Create one from a hex string, from channels, or take a named constant:

```java fragment
final Color ink = Color.decode("#999999");
final Color veil = new Color(0F, 0F, 0F, 0.5F);
final Color fade = Color.decode("#DDDDDD").toGradient(Color.WHITE);
```

The `withX`, `darker`, `brighter`, `multiply`, `add`, `scale`, `to` and `toGradient` methods return a new color and leave this one unchanged.
The `red`, `green`, `blue` and `alpha` setters change this color in place; the named constants are locked and throw `IllegalStateException` there, so take a `copy()` first.

An animated color carries an update function ([[Color#RAINBOW]], [[Color#LOADING]], or your own), which JOID applies each time it draws the color.
A color derived from an animated color stays animated: `Color.RAINBOW.scale(0.5F)` is a darker rainbow.
Two colors are equal when their channels, their gradient and their update function are equal; the update function compares by identity.

Learn: [Colors and styling](/use/learn/style-and-motion/styling/).

## rgb(int)

Creates an opaque color from a packed `0xRRGGBB` value; the top byte is ignored.

## argb(int)

Creates a color from a packed `0xAARRGGBB` value, alpha in the top byte.

## decode(String)

Parses a color from text, with or without `#`: `#RRGGBB` (opaque), `#RRGGBBAA` (alpha last), `rgb(r, g, b)` with channels from 0 to 255, `rgba(r, g, b, a)` with an alpha on 1, `gradient(from, to)` for a left-to-right gradient or `gradient(from, to, x1, y1, x2, y2)` with a direction (see `toGradient`), and `rainbow` or `loading` for a copy of the animated constants.
Throws `NumberFormatException` for any other text.

## RAINBOW()

Returns the color of the rainbow cycle at this instant of the JOID clock: a full turn of hues every 3 seconds.
The result is a plain color that does not change: pass the constant [[Color#RAINBOW]] to a setter instead, so that the color cycles by itself.

## RAINBOW(long)

Returns the color of the rainbow cycle at `time`, in milliseconds of the JOID clock.

## LOADING()

Returns the color of the loading pulse at this instant: a dark gray that brightens and darkens slightly, one cycle every 2 seconds.
The result is a plain color: pass the constant [[Color#LOADING]] to a setter instead, so that the color pulses by itself.

## Color(FloatBuffer)

Reads red, green, blue and alpha on 1 from the current position of the buffer, which advances by four floats.

## Color(Color)

Converts a `java.awt.Color`.

## Color(ColorGradient)

Creates a gradient color from a [[ColorGradient]]; its own channels are those of the start color.

## Color(int,int,int)

Creates an opaque color from red, green and blue from 0 to 255.

## Color(float,float,float)

Creates an opaque color from red, green and blue on 1.

## Color(int,int,int,int)

Creates a color from red, green, blue and alpha from 0 to 255.

## Color(float,float,float,float)

Creates a color from red, green, blue and alpha on 1.
Every channel is clamped between `0F` and `1F`.

## Color(float,float,float,float,UnaryOperator)

Creates an animated color: each time JOID draws it, JOID passes this color to `update` and draws the color that it returns.
A channel that you set later with `red`, `green`, `blue` or `alpha` overrides that channel on every frame.

## red

The red channel: `getRed()` reads it from 0 to 255, `getRedF()` on 1.
`red(float)` sets it in place, on 1, and throws `IllegalStateException` on a locked color such as a named constant.

## blue

The blue channel: `getBlue()` reads it from 0 to 255, `getBlueF()` on 1.
`blue(float)` sets it in place, on 1, and throws `IllegalStateException` on a locked color.

## alpha

The opacity: `getAlpha()` reads it from 0 to 255, `getAlphaF()` on 1, where `0F` is fully transparent.
`alpha(float)` sets it in place, on 1, and throws `IllegalStateException` on a locked color.

## green

The green channel: `getGreen()` reads it from 0 to 255, `getGreenF()` on 1.
`green(float)` sets it in place, on 1, and throws `IllegalStateException` on a locked color.

## bind()

Sets the color of the render state to the current frame of this color.
Draws without a color of their own, such as images and [[Tessellator]] batches without vertex colors, are tinted by it.

## bind(Runnable,BoundingBox)

Runs `runnable` with this color applied, then restores the previous color of the render state.
For a gradient, it binds the gradient shader instead, spread over `canvas` in the coordinates you draw in.

## bind(Runnable,BoundingBox,boolean)

Same as `bind(Runnable, BoundingBox)`; if `hasTexture` is `true`, a gradient multiplies the bound texture instead of replacing it, which tints an image.

## getRGB()

Returns the color packed as `0xAARRGGBB`.

## getRedF()

Returns the red channel on 1.

## getBlueF()

Returns the blue channel on 1.

## getAlphaF()

Returns the alpha channel on 1.

## getGreenF()

Returns the green channel on 1.

## to(Color,float)

Returns the color between this one and `target` at `progress` on 1: `0F` gives a copy of this color, `1F` a copy of `target`, alpha included.
Two gradients blend stop by stop and point by point; a plain color blends with each stop of a gradient.
When either side is animated, the result is animated and blends the current frame of both sides.

## toGradient(Color)

Returns a left-to-right gradient from this color to `target`.

## toGradient(Color,Point,Point)

Returns a gradient from this color to `target` along the line from `startPoint` to `endPoint`.
The points are on 1 of the drawn area: `(0, 0)` is its top-left corner and `(1, 1)` its bottom-right corner, so `(0, 0)` to `(0, 1)` runs from top to bottom.

## isGradient()

If `true`, this color holds a [[ColorGradient]] and draws as a gradient.

## toString()

Returns the channels from 0 to 255 and the `#RRGGBBAA` form, for logs.

## getGradient()

Returns the gradient of this color, or `null` for a plain color.

## encode()

Returns the color as `#RRGGBBAA` in upper case, the format that `decode` reads back.

## darker()

Returns this color darkened by half (`darker(0.5F)`).

## darker(float)

Returns this color with red, green and blue multiplied by `1 - scale`; alpha is unchanged.

## brighter()

Returns this color brightened by 20 % (`brighter(0.2F)`).

## brighter(float)

Returns this color with red, green and blue multiplied by `1 + scale`, clamped to `1F`; alpha is unchanged.

## multiply(Color)

Returns this color multiplied channel by channel, alpha included, by `c`.

## add(Color)

Returns the sum of this color and `c` channel by channel, alpha included, clamped to `1F`.

## scale(float)

Returns this color with every channel, alpha included, multiplied by `value`.

## RGBtoHSB(float[])

Returns the hue, saturation and brightness of this color, each on 1, in `hsbvals` when it is not `null`, otherwise in a new array.

## RGBtoHSB(int,int,int,float[])

Converts red, green and blue from 0 to 255 to hue, saturation and brightness on 1, written in `hsbvals` when it is not `null`, otherwise in a new array.

## withRed(float)

Returns a copy with the red channel set to `red`, on 1.

## withBlue(float)

Returns a copy with the blue channel set to `blue`, on 1.

## withAlpha(float)

Returns a copy with the opacity set to `alpha`, on 1.
On a gradient, both stops scale by the same factor, so their relative opacity is kept.

## withGreen(float)

Returns a copy with the green channel set to `green`, on 1.

## copy()

Returns an unlocked copy that keeps the gradient and the update function.
Copy a named constant before you change it with `red`, `green`, `blue` or `alpha`.

## reset()

Sets the color of the render state back to opaque white, the color that leaves textures and vertex colors unchanged.

## update()

Returns the frame of this color at this instant: the color itself when it is not animated, otherwise a fixed color computed by its update function.

## RED

Opaque red, `#FF0000FF`.

## BLUE

Opaque blue, `#0000FFFF`.

## GRAY

Opaque mid gray, `0.5F` on every channel.

## CYAN

Opaque cyan, `#00FFFFFF`.

## PINK

Opaque light pink: red `1F`, green and blue `0.7F`.

## WHITE

Opaque white, `#FFFFFFFF`.

## GREEN

Opaque green, `#00FF00FF`.

## BLACK

Opaque black, `#000000FF`.

## YELLOW

Opaque yellow, `#FFFF00FF`.

## ORANGE

Opaque orange: red `1F`, green `0.5F`, blue `0F`.

## MAGENTA

Opaque magenta, `#FF00FFFF`.

## DARKGRAY

Opaque dark gray, `0.3F` on every channel.

## LIGHTGRAY

Opaque light gray, `0.7F` on every channel.

## TRANSPARENT

Fully transparent black: draws nothing.

## RAINBOW

An animated color that cycles through the hues every 3 seconds, at 80 % saturation and brightness; its alpha is kept.

## LOADING

An animated color that pulses slightly around a dark gray every 2 seconds, the look of a loading placeholder; its alpha is kept.
