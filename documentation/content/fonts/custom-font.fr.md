# Custom Fonts

JOID rend le texte via des atlas MSDF (Multi-channel Signed Distance Field) — nets à n'importe quelle échelle. Chargez vos polices avec `FontLoader`, construisez un `TextInfo`, et alimentez un `Text` / `TextNode`.

## `FontLoader.load`

`FontLoader` est asynchrone — il délègue le parsing JSON et l'upload de texture à un pool d'exécuteurs et remet le `CustomFont` terminé à un callback.

```java
static void load(FontInputStream regular, Consumer<CustomFont> callback)
static void load(FontInputStream regular, FontInputStream bold, Consumer<CustomFont> callback)
```

Un `FontInputStream` apparie les métadonnées JSON et l'atlas PNG :

```java
public FontInputStream(InputStream data, InputStream texture)            // data = .json, texture = .png
```

Chargement minimal :

```java
FontLoader.load(
    new FontInputStream(
        getClass().getResourceAsStream("/fonts/Inter/font.json"),
        getClass().getResourceAsStream("/fonts/Inter/font.png")
    ),
    customFont -> this.interFont = customFont
);
```

Atlas regular et bold :

```java
FontLoader.load(
    new FontInputStream(regularJson, regularPng),
    new FontInputStream(boldJson, boldPng),
    customFont -> {
        // customFont.getRegular() et customFont.getBold() sont des Font
    }
);
```

Avec l'overload à un argument, la même police est stockée en regular et en bold sur le wrapper `CustomFont`.

## `DemoFont.MONTSERRAT` embarqué

`DemoFont.MONTSERRAT` est chargé automatiquement quand JOID démarre avec `setDemoMode(true)`. Pratique pour bootstrapper les snippets du quick-start et les UIs de démo — shippez votre propre atlas en production.

## Construire un `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)
```

Setters (chaînables, retournent le même `TextInfo`) :

```java
T font(IFont font)
T fontSize(float fontSize)
T letterSpacing(float letterSpacing)
T lineHeight(float lineHeight)
T color(Color color)
T colored(boolean colored)
T italic(boolean italic)
T shadow()                          // shadowColor = this.color.darker(0.3F)
T shadow(Color color)               // couleur d'ombre explicite
T shadow(float x, float y)          // offset de l'ombre en unités logiques
T copy()
```

Il n'y a **pas** de setter `bold(boolean)`, `shadowColor(Color)` ou `shadowOffset(double, double)` — le gras s'obtient en changeant d'`IFont` (chargez un atlas bold et utilisez `customFont.getBold()`) ; l'ombre se configure via les overloads `shadow(...)`.

```java
final TextInfo info = TextInfo.create(customFont.getRegular(), 24, Color.WHITE)
    .italic(true)
    .shadow(Color.BLACK)
    .shadow(1F, 1F);
```

## Utiliser dans les nœuds

```java
TextNode.create(0, 0)
    .text(Text.create("Hello", info))
    .attach(parent);
```

Ou via `DrawUtils.TEXT` pour un dessin ad-hoc :

```java
DrawUtils.TEXT.drawText(x, y, "Hello", info, Align.START, Align.START);
```

## Variantes bold

`CustomFont` possède deux `Font` — `regular` et `bold`. Créez deux `TextInfo`, un par graisse :

```java
final TextInfo regularInfo = TextInfo.create(customFont.getRegular(), 16, Color.WHITE);
final TextInfo boldInfo    = TextInfo.create(customFont.getBold(),    16, Color.WHITE);

Text.create()
    .add(TextElement.create("Normal ", regularInfo))
    .add(TextElement.create("Bold",    boldInfo));
```

Si vous n'avez qu'un seul atlas, `CustomFont` fallback sur la police regular dans les deux cas.

## Chargement asynchrone

`FontLoader.load(...)` retourne immédiatement ; le callback se déclenche sur le pool d'exécuteurs une fois le parsing et l'upload terminés. Lancez le chargement au démarrage de l'application pour que la police soit prête avant que l'UI qui l'utilise ne s'ouvre.

## Jeu de caractères

Un atlas MSDF ne contient que les caractères déclarés dans le `charset.txt` au moment de la génération. Les glyphes absents rendent un placeholder. Générez des atlas avec la plage Latin complète + symboles pour des polices à usage général — voir [MSDF Atlas](msdf-atlas.md).

## Bonnes pratiques

- **Chargez les polices une fois, au démarrage.** Elles sont réutilisées entre toutes les UIs.
- **Cachez `TextInfo`.** Un par style logique (titre, body, code) — réutilisez par nœud.
- **Pré-dimensionnez votre atlas.** 2048×2048 tient ~500 glyphes à 48 px. Pour CJK, 4096×4096 ou plusieurs atlas par script.

## Voir aussi

- [MSDF Atlas](msdf-atlas.md) — génération des fichiers d'atlas.
- [TextNode](../nodes/design/text.md) — rendu de texte.