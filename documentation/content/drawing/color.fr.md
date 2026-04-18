# Color

Le type de couleur de JOID. Supporte RGBA simple, gradients, patterns dynamiques (rainbow, loading) et décodage depuis des chaînes hex.

## Construction

```java
Color red = new Color(1F, 0F, 0F, 1F);
Color rgba = new Color(255, 128, 0, 200);
Color fromInt = new Color(0xFFCC0000);
```

## Constantes

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

La méthode `decode` est le go-to pour les chaînes hex et plus :

```java
Color.decode("#FF0000");                                         // hex RGB
Color.decode("#FF0000CC");                                        // hex RGBA
Color.decode("rgb(255, 0, 0)");
Color.decode("rgba(255, 0, 0, 200)");
Color.decode("rainbow");                                          // pattern dynamique
Color.decode("loading");                                          // pattern dynamique
Color.decode("gradient(#FF0000, #0000FF, 0, 0, 1, 0)");         // gradient
```

## Gradients

Créer un gradient via `toGradient` :

```java
Color blueToGreen = Color.BLUE.toGradient(Color.GREEN);                           // horizontal
Color cyanToMagenta = Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F));  // vertical
```

Le `Vector4f(startX, startY, endX, endY)` définit la direction du gradient en coords normalisées (0.0 à 1.0).

Vérifier si une couleur est un gradient :

```java
if (myColor.isGradient()) { ... }
```

## Patterns dynamiques

```java
Color.RAINBOW       // cycle à travers les teintes au fil du temps
Color.LOADING       // gris pulsant pour états skeleton
```

Utilisez-les comme n'importe quelle couleur — le renderer appelle `.update()` à chaque frame pour avancer le pattern :

```java
RectNode.create(0, 0, 200, 60).color(Color.RAINBOW).attach(parent);
```

Créer vos propres patterns en étendant `Color` avec un consumer `update` :

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

Interpoler entre deux couleurs :

```java
Color mid = Color.RED.to(Color.BLUE, 0.5F);         // à mi-chemin
```

Gère les 4 combinaisons gradient/flat :

- flat → flat : lerp RGBA standard.
- flat → gradient : la couleur flat anime vers le start/end du gradient.
- gradient → flat : symétrique.
- gradient → gradient : chaque composante (start, end, direction) lerp indépendamment.

Utilisé automatiquement par `RectNode.color(normal, hovered)`.

## Manipulation

```java
Color darker = color.darker();               // 50% plus foncé
Color darker20 = color.darker(0.2F);         // 20% plus foncé
Color brighter = color.brighter();           // 20% plus clair
Color brighter50 = color.brighter(0.5F);     // 50% plus clair
Color copy = color.copy();
Color withAlpha = color.copyAlpha(0.5F);
Color tinted = color.multiply(Color.RED);    // multiplication composante par composante
Color additive = color.addToCopy(otherColor);
```

## Conversion HSB

```java
float[] hsb = Color.RGBtoHSB(r, g, b, null);
Color fromHSB = new Color(java.awt.Color.HSBtoRGB(h, s, b));
```

## Binding en GL

Pour des draws custom :

```java
color.bind();                                             // glColor4f(r, g, b, a)
DrawUtils.SHAPE.drawRawRect(x, y, w, h);                 // applique la couleur courante
Color.reset();                                            // restaure la couleur précédente

// Ou wrappé :
color.bind(() -> {
    DrawUtils.SHAPE.drawRawRect(x, y, w, h);
}, canvas);
```

## Encodage

```java
String hex = color.encode();     // "#FF0000CC"
int rgb = color.getRGB();        // 0xCCFF0000 (AARRGGBB)
```

## Bonnes pratiques

- **Préférez `Color.decode("#...")` au byte-math manuel.** Cohérent avec les conventions CSS.
- **Utilisez `Color.WHITE.copyAlpha(x)` pour les overlays transparents.** Plus propre que hardcoder 4 floats.
- **Mettez les patterns en cache.** `Color.RAINBOW` est partagé ; n'en créez pas un nouveau par frame.
- **Utilisez les gradients via `toGradient`** — le pipeline de shaders s'occupe du reste.

## Voir aussi

- [Shapes](shapes.md).
- [Gradient Effect](../effects/gradient.md).
- [DrawUtils](draw-utils.md).