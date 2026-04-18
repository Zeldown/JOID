# Shapes

`DrawShape` est la façade pour chaque primitif de forme que JOID peut rendre hors de l'arbre de nœuds. Accessible via `DrawUtils.SHAPE` ou directement via `DrawShape.getInstance()` — les deux retournent le même singleton. Chaque méthode est `void` et écrit immédiatement dans le contexte GL courant.

## Rectangles

### `drawRect`

```java
void drawRect(double x, double y, double width, double height, Color color)
```

Rectangle axe-aligné rempli. Transparence via `color.a`. Routé en interne par `drawPolygon` avec quatre points de coin.

```java
DrawUtils.SHAPE.drawRect(0, 0, 200, 100, Color.WHITE);
DrawUtils.SHAPE.drawRect(0, 0, 200, 100, new Color(1F, 1F, 1F, 0.2F));
```

### `drawRoundedRect`

```java
void drawRoundedRect(double x, double y, double width, double height, Color color, float radius)
void drawRoundedRect(double x, double y, double width, double height, Color color, float radius,
                     boolean roundedLeft, boolean roundedTop,
                     boolean roundedRight, boolean roundedBottom)
```

Rectangle à coins arrondis, rendu via `RoundedShader` — le même shader qui back `RoundedNodeEffect`. La variante à quatre coins désactive l'arrondi sur des côtés spécifiques (onglets, layouts avec entailles).

```java
DrawUtils.SHAPE.drawRoundedRect(40, 40, 120, 60, Color.decode("#1f2937"), 8F);

DrawUtils.SHAPE.drawRoundedRect(40, 40, 120, 60, Color.decode("#1f2937"),
    8F, true, true, false, false);
```

### `drawRawRect`

```java
void drawRawRect(double x, double y, double width, double height)
```

Draw de quad sans binder une couleur. Utile quand un shader est déjà actif et doit peindre la sortie — utilisé en interne par `RectNode` quand des gradients sont en jeu.

```java
Color.WHITE.bind();
DrawUtils.SHAPE.drawRawRect(x, y, w, h);
Color.reset();
```

## Cercles

### `drawCircle`

```java
void drawCircle(double centerX, double centerY, Color color, double radius)
```

Disque anti-aliasé découpé par `CircleShader` dans un quad allant de `(centerX - radius, centerY - radius)` à `(centerX + radius, centerY + radius)`.

```java
DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 8D);
```

## Bordures

### `drawBorder`

```java
void drawBorder(double x, double y, double x2, double y2, Color color)
void drawBorder(double x, double y, double x2, double y2, Color color, double stroke)
```

Quatre rectangles fins dessinés **à l'extérieur** de la box définie par `(x, y) → (x2, y2)`. À utiliser pour des contours nets 1-pixel.

```java
DrawUtils.SHAPE.drawBorder(10, 10, 210, 110, Color.decode("#374151"), 1D);
```

### `drawFilledBorder`

```java
void drawFilledBorder(double x, double y, double x2, double y2, Color color)
void drawFilledBorder(double x, double y, double x2, double y2, Color color, double stroke)
```

Même géométrie que `drawBorder`, mais les coins sont remplis — le contour forme un cadre plein sans trous aux coins.

```java
DrawUtils.SHAPE.drawFilledBorder(10, 10, 210, 110, Color.decode("#374151"), 2D);
```

## Lignes

### `drawLine`

```java
void drawLine(Color color, Vector2d... points)
void drawLine(Color color, float stroke, Vector2d... points)
```

Line strip connectant chaque paire consécutive de points. `GL_LINE_SMOOTH` est activé pendant l'appel. La variante avec stroke positionne `glLineWidth` autour du draw.

```java
DrawUtils.SHAPE.drawLine(Color.WHITE,
    new Vector2d(0, 0),
    new Vector2d(100, 50),
    new Vector2d(200, 0)
);

DrawUtils.SHAPE.drawLine(Color.WHITE, 2F,
    new Vector2d(0, 0), new Vector2d(300, 0)
);
```

### `drawDashedLine`

```java
void drawDashedLine(Color color, int pattern, float stroke, Vector2d... points)
```

Line strip utilisant `GL_LINE_STIPPLE` avec un pattern dash/gap fixe (`0xAAAA`). Le paramètre `pattern` est le facteur stipple — plus grand = tirets plus longs.

```java
DrawUtils.SHAPE.drawDashedLine(Color.decode("#4ade80"), 2, 1F,
    new Vector2d(0, 50), new Vector2d(300, 50)
);
```

### `drawCurvedLine`

```java
void drawCurvedLine(Color color, Vector2d start, Vector2d end, Vector2d control)
void drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d end, Vector2d control)

void drawCurvedLine(Color color, Vector2d start, Vector2d startControl,
                    Vector2d end, Vector2d endControl)
void drawCurvedLine(Color color, float stroke, Vector2d start, Vector2d startControl,
                    Vector2d end, Vector2d endControl)
```

Courbe de Bézier approximée par des segments — quadratique (un point de contrôle) ou cubique (deux). Le nombre de segments augmente avec la distance linéaire entre extrémités.

```java
DrawUtils.SHAPE.drawCurvedLine(Color.WHITE,
    new Vector2d(0, 0), new Vector2d(200, 0), new Vector2d(100, 100)
);

DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 2F,
    new Vector2d(0, 0), new Vector2d(100, 0),
    new Vector2d(100, 100), new Vector2d(200, 100)
);
```

## Polygones

### `drawPolygon`

```java
void drawPolygon(Color color, Vector2d... points)
```

Polygone rempli avec un nombre arbitraire de sommets, donnés en varargs `Vector2d`.

```java
DrawUtils.SHAPE.drawPolygon(Color.decode("#a78bfa"),
    new Vector2d(100, 100),
    new Vector2d(200, 100),
    new Vector2d(150, 180)
);
```

### `drawShape`

```java
void drawShape(int mode, Color color, Vector2d... points)
```

Plus bas niveau : n'importe quel mode GL (`GL_TRIANGLES`, `GL_QUADS`, `GL_LINE_LOOP`, …) avec des sommets arbitraires. Toutes les autres méthodes de forme finissent par appeler `drawShape`.

```java
DrawUtils.SHAPE.drawShape(GL11.GL_LINE_LOOP, Color.RED,
    new Vector2d(0, 0), new Vector2d(100, 0),
    new Vector2d(100, 100), new Vector2d(0, 100)
);
```

## Utilitaires

### `bindEmptyTexture`

```java
static void bindEmptyTexture()
```

Bind une texture 1×1 blanche créée paresseusement pour que les quad draws suivants utilisent les couleurs des sommets sans échantillonner une texture obsolète. Appelé automatiquement par `drawShape` et `drawRawRect` — vous l'invoquez rarement directement.

## Voir aussi

- `DrawUtils` — point d'entrée pour les quatre façades.
- `Color` — construction, gradients, transitions, binding.
- `RoundedNodeEffect`, `CircleNodeEffect` — équivalents au niveau nœud des shaders rounded/circle.