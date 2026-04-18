# Color

JOID's color type. Supports plain RGBA, gradients, dynamic patterns (rainbow, loading), and decoding from hex strings.

## Construct

```java
Color red = new Color(1F, 0F, 0F, 1F);
Color rgba = new Color(255, 128, 0, 200);
Color fromInt = new Color(0xFFCC0000);
```

## Constants

```java
Color.WHITE
Color.BLACK
Color.RED
Color.GREEN
Color.BLUE
Color.YELLOW
Color.CYAN
Color.MAGENTA
Color.GRAY
Color.DARKGRAY
Color.LIGHTGRAY
Color.PINK
Color.ORANGE
Color.TRANSPARENT
```

## Decode

The `decode` method is the go-to for hex strings and more:

```java
Color.decode("#FF0000");                                         // hex RGB
Color.decode("#FF0000CC");                                        // hex RGBA
Color.decode("rgb(255, 0, 0)");
Color.decode("rgba(255, 0, 0, 200)");
Color.decode("rainbow");                                          // dynamic pattern
Color.decode("loading");                                          // dynamic pattern
Color.decode("gradient(#FF0000, #0000FF, 0, 0, 1, 0)");         // gradient
```

## Gradients

Create gradients via `toGradient`:

```java
Color blueToGreen = Color.BLUE.toGradient(Color.GREEN);                           // horizontal
Color cyanToMagenta = Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F));  // vertical
```

The `Vector4f(startX, startY, endX, endY)` defines the gradient direction in normalized coords (0.0 to 1.0).

Check if a color is a gradient:

```java
if (myColor.isGradient()) { ... }
```

## Dynamic patterns

```java
Color.RAINBOW       // cycles through hues over time
Color.LOADING       // pulsing gray for skeleton states
```

Use them like any other color — the renderer calls `.update()` each frame to advance the pattern:

```java
RectNode.create(0, 0, 200, 60).color(Color.RAINBOW).attach(parent);
```

Create your own patterns by extending `Color` with an `update` consumer:

```java
Color pulse = new Color(1F, 1F, 1F, 1F, c -> {
    float t = (float) ((Math.sin(System.currentTimeMillis() / 500D) + 1D) / 2D);
    c.r = t;
    c.g = 1F - t;
    c.b = 0F;
    c.a = 1F;
});
```

## Transitions

Interpolate between two colors:

```java
Color mid = Color.RED.to(Color.BLUE, 0.5F);         // halfway
```

Handles all 4 gradient/flat combinations:

- flat → flat: standard RGBA lerp.
- flat → gradient: flat color animates toward the gradient's start/end.
- gradient → flat: symmetric.
- gradient → gradient: each component (start, end, direction) lerps independently.

Used automatically by `RectNode.color(normal, hovered)`.

## Manipulation

```java
Color darker = color.darker();               // 50% darker
Color darker20 = color.darker(0.2F);         // 20% darker
Color brighter = color.brighter();           // 20% brighter
Color brighter50 = color.brighter(0.5F);     // 50% brighter
Color copy = color.copy();
Color withAlpha = color.copyAlpha(0.5F);
Color tinted = color.multiply(Color.RED);    // component-wise multiply
Color additive = color.addToCopy(otherColor);
```

## HSB conversion

```java
float[] hsb = Color.RGBtoHSB(r, g, b, null);
Color fromHSB = new Color(java.awt.Color.HSBtoRGB(h, s, b));
```

## Binding in GL

For custom draw calls:

```java
color.bind();                                             // glColor4f(r, g, b, a)
DrawUtils.SHAPE.drawRawRect(x, y, w, h);                 // applies the current color
Color.reset();                                            // pop previous color

// Or wrapped:
color.bind(() -> {
    DrawUtils.SHAPE.drawRawRect(x, y, w, h);
}, canvas);
```

## Encode

```java
String hex = color.encode();     // "#FF0000CC"
int rgb = color.getRGB();        // 0xCCFF0000 (AARRGGBB)
```

## Best practices

- **Prefer `Color.decode("#...")` over manual byte math.** Matches CSS conventions.
- **Use `Color.WHITE.copyAlpha(x)` for transparent overlays.** Cleaner than hardcoding 4 floats.
- **Cache patterns.** `Color.RAINBOW` is shared; don't create fresh ones per frame.
- **Use gradients via `toGradient`** — the shader pipeline handles the rest.

## See also

- [Shapes](shapes.md).
- [Gradient Effect](../effects/gradient.md).
- [DrawUtils](draw-utils.md).
