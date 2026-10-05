# Custom Fonts

JOID rend le texte via des atlas MSDF (Multi-channel Signed Distance Field) — nets à n'importe quelle échelle. Chargez une famille de polices avec `MsdfFontLoader`, construisez un `TextInfo`, choisissez une graisse, et alimentez un `Text` / `TextNode`.

## `MsdfFontLoader.load`

`MsdfFontLoader` est asynchrone — il lit toutes les faces de la famille en parallèle sur un pool d'exécuteurs et renvoie un `CompletableFuture` qui porte le `MsdfFont` terminé.

```java
static CompletableFuture<MsdfFont> load(Object... faces)
```

Chaque handle désigne un fichier `font.msdf`, produit par le générateur décrit dans [Atlas MSDF](msdf-atlas.md). Il porte ensemble l'atlas, les métriques des glyphes, la table de crénage, ainsi que la graisse et le style de la face. Tout ce qu'un [localisateur d'asset](../resources/assets.md) reconnaît convient — un `InputStream`, un `File`, une URL, ou un handle à vous.

Chargement minimal :

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"))
    .thenAccept(font -> this.inter = font);
```

Une famille entière, dans n'importe quel ordre — un handle par graisse et par style :

```java
MsdfFontLoader.load(
    getClass().getResourceAsStream("/fonts/Inter-Light/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Italic/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Bold/font.msdf")
).thenAccept(font -> this.inter = font);
```

Une police, un seul enregistrement : chaque `TextInfo` construit dessus choisit sa graisse. Deux faces de même graisse et de même style, une liste vide ou un fichier illisible complètent le future en erreur, avec un message qui nomme le problème.

### Sources

Un handle devient un `MsdfBinarySource`. Construisez la source vous-même quand une face doit être présentée autrement que ce que déclare son fichier — une police aux métadonnées fausses, ou une famille assemblée à partir de fichiers sans rapport :

```java
MsdfFontLoader.load(
    handleRegular,
    MsdfBinarySource.of(handleBold).weight(FontWeight.BOLD),
    MsdfBinarySource.of(handlePenche).italic(true)
);
```

Le couple `font.json` + `font.png` des anciens atlas se charge toujours via un `MsdfJsonSource`. Ces fichiers ne déclarent aucune graisse : ils démarrent en regular et droits — réglez le style sur la source :

```java
MsdfFontLoader.load(handleRegular, MsdfJsonSource.of(jsonBold, pngBold).weight(FontWeight.BOLD));
```

Ce sont tous des `IMsdfSource`, comme tout ce que vous écrirez vous-même : le chargeur lit la source qu'on lui donne.

### Crénage

Les paires de crénage voyagent dans le fichier `font.msdf` et s'appliquent seules, au dessin comme à la mesure — `TextInfo.getWidth`, le retour à la ligne et les curseurs des champs de texte en tiennent compte. Rien à activer.

## Graisses et styles

Comme une `font-family` CSS assemblée à partir de règles `@font-face`, un seul `MsdfFont` contient toutes les faces d'une police, et chaque texte choisit sa graisse. `FontWeight` nomme les neuf graisses, de `THIN` (100) à `BLACK` (900) :

```java
final TextInfo corps = TextInfo.create(InternalFont.MONTSERRAT, 16, Color.WHITE);
final TextInfo titre = TextInfo.create(InternalFont.MONTSERRAT, FontWeight.BOLD, 28, Color.WHITE);
final TextInfo citation = corps.copy().weight(FontWeight.LIGHT).italic(true);
```

Quand la famille n'a pas de face de la graisse demandée, la face à la graisse la plus proche est dessinée. Deux faces à égale distance sont départagées comme en CSS : 400 prend la plus grasse, 500 et en dessous prennent la plus maigre, au-dessus de 500 la plus grasse. Avec Light (300) et Bold (700) chargées :

| Demandée | 100 | 200 | 300 | 400 | 500 | 600 | 700 | 800 | 900 |
|---|---|---|---|---|---|---|---|---|---|
| Dessinée | 300 | 300 | 300 | 300 | 300 | 700 | 700 | 700 | 700 |

En mode dev (`JOID.inst().setDevMode(true)`), le premier repli de chaque graisse est signalé une fois dans la console :

```
[JOID] The font weight 600 is not loaded in this font family, 700 is drawn instead (loaded: 300, 700)
```

L'italique préfère les faces italiques de la famille. Une famille sans italique garde ses faces droites et les penche, donc `italic(true)` se voit toujours.

`FontWeight.of(int)` convertit n'importe quelle graisse numérique en la graisse nommée la plus proche : `FontWeight.of(600)` vaut `SEMI_BOLD`, `FontWeight.of(650)` vaut `BOLD`.

## Polices embarquées

`InternalFont.MONTSERRAT` est chargée au démarrage de JOID et contient les neuf graisses de Montserrat. `DemoFont.MONTSERRAT` pointe vers la même famille quand JOID tourne avec `setDemoMode(true)`, à côté de `DemoFont.BATUPHAT` et `DemoFont.SPACE_GROTESK`. Pratique pour bootstrapper les snippets du quick-start et les UIs de démo — shippez votre propre atlas en production.

## Construire un `TextInfo`

```java
TextInfo.create(IFont font, float fontSize)
TextInfo.create(IFont font, float fontSize, Color color)
TextInfo.create(IFont font, FontWeight weight, float fontSize)
TextInfo.create(IFont font, FontWeight weight, float fontSize, Color color)
```

Setters (chaînables, retournent le même `TextInfo`) :

```java
T font(IFont font)
T fontSize(float fontSize)
T weight(FontWeight weight)         // REGULAR par défaut
T letterSpacing(float letterSpacing) // fraction de la taille de police, -0.02F = -2 % sur Figma
T lineHeight(float lineHeight)      // fraction de la taille de police, 1.5F = 150 %, 0 garde celle de la police (Auto)
T color(Color color)
T colored(boolean colored)          // false ignore les couleurs posées par le balisage
T italic(boolean italic)
T markups(ITextMarkup... markups)   // remplace les balisages enregistrés, aucun les désactive
T effects(ITextEffect... effects)   // effets appliqués à tout le texte
T shadow()                          // shadowColor = this.color.darker(0.3F)
T shadow(Color color)               // couleur d'ombre explicite
T shadow(float x, float y)          // décalage d'ombre en unités logiques
T copy()
```

```java
final TextInfo info = TextInfo.create(maPolice, 24, Color.WHITE)
    .weight(FontWeight.SEMI_BOLD)
    .italic(true)
    .shadow(Color.BLACK)
    .shadow(1F, 1F);
```

Le balisage et les effets permettent à une seule chaîne de changer de graisse, de couleur ou de décoration en cours de route — voir [Balisage & effets](markup-effects.md).

## Utilisation dans les nodes

```java
TextNode.create(0, 0)
    .text(Text.create("Hello", info))
    .attach(parent);
```

Ou via `DrawUtils.TEXT` pour un dessin ponctuel :

```java
DrawUtils.TEXT.drawText(x, y, "Hello", info, Align.START, Align.START);
```

## Chargement asynchrone

`MsdfFontLoader.load(...)` rend la main tout de suite. Le future se complète sur le pool d'exécuteurs une fois toutes les faces lues, à vous de choisir comment attendre :

```java
MsdfFontLoader.load(regular, bold).thenAccept(font -> this.font = font);         // continuer une fois prête
MsdfFontLoader.load(regular, bold).exceptionally(error -> { error.printStackTrace(); return null; });
this.font = MsdfFontLoader.load(regular, bold).join();                           // bloquer, au démarrage
```

Une police illisible complète le future en erreur au lieu d'échouer en silence : ne jetez jamais le future renvoyé. Lancez le chargement au démarrage de l'application pour que la police soit prête avant l'ouverture de l'UI qui l'utilise.

## Jeu de caractères

Un atlas MSDF ne contient que les caractères déclarés dans le `charset.txt` au moment de la génération. Les caractères absents de l'atlas sont ignorés. Générez vos atlas avec la plage latine complète + symboles pour une police généraliste — voir [Atlas MSDF](msdf-atlas.md).

## Bonnes pratiques

- **Chargez chaque famille une seule fois, au démarrage.** Elle est réutilisée par toutes les UIs.
- **Mettez les `TextInfo` en cache.** Un par style logique (titre, corps, code) — réutilisé par node.
- **Embarquez les graisses que vous utilisez.** Chaque face est un atlas ; la famille ramène les autres à la plus proche.

## Autres implémentations de police

MSDF est une implémentation du contrat de police, pas le contrat lui-même. Tout ce qui dessine ou mesure du texte — `TextInfo`, `TextNode`, `DrawUtils` — ne connaît que deux interfaces de `dev.joid.lib.font` :

| Interface | Rôle |
|---|---|
| `IFont` | Ce que porte un `TextInfo`. Fournit son provider. |
| `IFontProvider` | Dessine et mesure une chaîne pour un `TextInfo`. |

Une police faite d'images de glyphes obtient le reste gratuitement depuis `dev.joid.lib.font.impl.glyph` : décrivez une face avec `IFontFace`, étendez `GlyphFont` et `GlyphFontProvider`, et dessinez un seul glyphe dans `drawGlyph`. Les familles, la résolution des graisses, le crénage, l'espacement, le balisage, les effets et les ombres viennent des classes de base. Les classes MSDF de `dev.joid.lib.font.impl.msdf` sont construites exactement ainsi.

## Voir aussi

- [Balisage & effets](markup-effects.md) — styler une chaîne de l'intérieur.
- [Atlas MSDF](msdf-atlas.md) — générer les fichiers d'atlas.
- [TextNode](../nodes/design/text.md) — afficher du texte.