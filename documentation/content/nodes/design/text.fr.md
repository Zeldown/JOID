# TextNode

Rend un objet `Text` (un ou plusieurs `TextElement`) avec une police MSDF.

## Créer

```java
TextNode.create(x, y)                              // auto-size au texte
TextNode.create(x, y, width, height)               // bounding box fixe
```

Avec `TextNode.create(x, y)`, le nœud se redimensionne au texte une fois le `Text` attaché. Avec la forme à quatre arguments, la bounding box pilote le wrap / clipping via le `mode(TextMode)` du nœud.

## API

```java
T text(Text text)                                  // le Text à rendre
T mode(TextMode mode)                              // NORMAL / OVERFLOW / SPLIT / BOX (défaut NORMAL)
T reset()                                          // verrouille les width/height courants comme taille initiale
```

Il n'y a **pas** de `text(Supplier<Text>)`, `text(Text, TextOverflow)` ou setter d'alignement sur `TextNode` lui-même — l'alignement et le suffixe d'overflow vivent sur le builder `Text`.

## `TextMode`

| Mode | Comportement |
|---|---|
| `NORMAL` | Dessine tel quel, pas de wrap. Auto-size width/height du nœud s'ils étaient `0`. |
| `OVERFLOW` | Tronque si le texte dépasse `width` ; auto-size la height du nœud. |
| `SPLIT` | Wrap en plusieurs lignes dans `width` ; le nœud grandit verticalement pour contenir toutes les lignes. |
| `BOX` | Comme `SPLIT`, mais les lignes hors de la box fixe `(y, y + height)` sont droppées. |

## Construire le `Text`

```java
final TextInfo info = TextInfo.create(myFont, 20, Color.WHITE);

TextNode.create(40, 40)
    .text(Text.create("Hello, JOID", info))
    .attach(parent);
```

Plusieurs runs avec des `TextInfo` différents passent par `Text.add(TextElement)` — il n'y a pas de méthode `.append(...)` sur `Text` :

```java
final TextInfo regular = TextInfo.create(myFont, 20, Color.WHITE);
final TextInfo emphasis = TextInfo.create(myFont, 20, Color.decode("#a78bfa")).italic(true);

TextNode.create(40, 40)
    .text(Text.create()
        .add(TextElement.create("Hello, ", regular))
        .add(TextElement.create("JOID", emphasis)))
    .attach(parent);
```

Voir la [référence du builder `Text`](../../drawing/text.md) pour toutes les factories, setters et modifiers.

## Alignement

L'alignement vit sur le `Text`, pas sur le nœud :

```java
Text.create("Centered", info, Align.CENTER, Align.CENTER);
```

Les ancres se résolvent contre la bounding box du `TextNode`.

## Suffixe d'overflow

`TextOverflow` (`NONE`, `ELLIPSIS`, `DOT`, `HYPHEN`) est aussi une propriété du `Text`, à jumeler avec `TextMode.OVERFLOW` :

```java
TextNode.create(40, 40, 200, 40)
    .mode(TextMode.OVERFLOW)
    .text(Text.create("A very long subtitle", info).overflow(TextOverflow.ELLIPSIS))
    .attach(parent);
```

## `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)

T font(IFont font)
T fontSize(float fontSize)
T letterSpacing(float letterSpacing)
T lineHeight(float lineHeight)
T color(Color color)
T colored(boolean colored)
T italic(boolean italic)
T shadow(Color shadowColor)
T shadow()                                         // par défaut color.darker(0.3F)
T shadow(float x, float y)
T copy()
```

`TextInfo` n'a pas de setter `bold(boolean)` — shippez un atlas MSDF bold comme `IFont` séparé si vous avez besoin du gras. Utilisez `italic(true)` pour le rendu italique (nécessite que la police fournisse les glyphes italiques).

## Texte réactif

Le `text(...)` du nœud prend un `Text` directement. Pour des updates réactives, couplez avec `watch(...)` :

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(0, 0)
    .text(Text.create(() -> "Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

`Text.create(Supplier<Object>, TextInfo)` se réévalue à chaque draw, et `.watch(count)` déclenche un reload à chaque changement de `count`.

## Effets

Le texte fonctionne bien avec les effets shader — gradients et bordures marchent out-of-the-box :

```java
TextNode.create(0, 0)
    .text(Text.create("Gradient", TextInfo.create(font, 48, Color.RED.toGradient(Color.BLUE))))
    .effect(BorderNodeEffect.create(Color.WHITE, 1F))
    .attach(parent);
```

> NOTE. `Color.toGradient(...)` sur un `TextInfo` est résolu nativement par le shader font — pas de pass supplémentaire, pas de FBO. Les bordures en gradient fonctionnent pareil : `BorderNodeEffect.create(Color.RED.toGradient(Color.BLUE), 1F)`.

## Bonnes pratiques

- **Cachez `TextInfo`.** En créer un par frame fait tourner le GC.
- **Préférez `Text.create(Supplier, info)` + `.watch(signal)`** à la reconstruction de toute l'UI pour des updates de texte.
- **Utilisez `DemoFont.MONTSERRAT` (chargée par `setDemoMode(true)`) en dev.** Shippez votre propre atlas MSDF en prod — voir [Custom Fonts](../../fonts/custom-font.md).

## Voir aussi

- [Custom Fonts](../../fonts/custom-font.md)
- [MSDF Atlas](../../fonts/msdf-atlas.md)
- [Drawing / Text](../../drawing/text.md) — `Text`, `TextElement`, modifiers, modes, overflow.
- [Color](../../drawing/color.md) — `Color.toGradient(...)` pour du texte et des bordures en gradient.