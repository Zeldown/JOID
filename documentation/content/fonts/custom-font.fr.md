# Custom Fonts

JOID rend le texte via des atlas MSDF (Multi-channel Signed Distance Field) — nets à n'importe quelle échelle. Chargez vos polices avec `MsdfFontLoader`, construisez un `TextInfo`, et alimentez un `Text` / `TextNode`.

## `MsdfFontLoader.load`

`MsdfFontLoader` est asynchrone — il délègue la lecture et l'upload de texture à un pool d'exécuteurs et renvoie un `CompletableFuture` qui porte le `MsdfFont` terminé.

```java
static CompletableFuture<MsdfFont> load(Object packed)
static CompletableFuture<MsdfFont> load(Object regular, Object bold)
```

Chaque handle désigne un fichier `font.msdf`, produit par le générateur décrit dans [Atlas MSDF](msdf-atlas.md). Il porte ensemble l'atlas, les métriques des glyphes et la table de crénage. Tout ce qu'un [localisateur d'asset](../resources/assets.md) reconnaît convient — un `InputStream`, un `File`, une URL, ou un handle à vous.

Chargement minimal :

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/fonts/Inter/font.msdf"))
    .thenAccept(font -> this.interFont = font);
```

Atlas regular et bold :

```java
MsdfFontLoader.load(
    getClass().getResourceAsStream("/fonts/Inter-Regular/font.msdf"),
    getClass().getResourceAsStream("/fonts/Inter-Bold/font.msdf")
).thenAccept(font -> {
    // font.getRegular() et font.getBold() sont les deux graisses MsdfFace
});
```

Avec un seul handle, la même graisse sert de regular et de bold.

Le couple `font.json` + `font.png` des anciens atlas se charge toujours. Enveloppez les deux fichiers dans un `MsdfJsonSource` et passez-le là où un handle est attendu — même à côté d'un atlas packé :

```java
MsdfFontLoader.load(MsdfJsonSource.of(json, png));
MsdfFontLoader.load(handleRegular, MsdfJsonSource.of(jsonBold, pngBold));
```

Les deux sont des `IMsdfSource`, comme tout ce que vous écrirez vous-même : le chargeur lit la source qu'on lui donne.

### Crénage

Les paires de crénage voyagent dans le fichier `font.msdf` et s'appliquent seules, au dessin comme à la mesure — `TextInfo.getWidth`, le retour à la ligne et les curseurs des champs de texte en tiennent compte. Rien à activer.

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

`MsdfFont` porte deux graisses — `regular` et `bold`. Un `TextInfo` se construit depuis la police elle-même, et le code de style `§l` bascule sur la graisse bold à l'intérieur du texte :

```java
final TextInfo info = TextInfo.create(police, 16, Color.WHITE);

Text.create("Normal §lGras", info);
```

Avec un seul atlas, les deux graisses sont identiques : `§l` n'a alors aucun effet visible.

## Chargement asynchrone

`MsdfFontLoader.load(...)` retourne immédiatement. Le future se complète sur le pool d'exécuteurs une fois la lecture et l'upload terminés : c'est vous qui choisissez comment attendre.

```java
MsdfFontLoader.load(flux).thenAccept(font -> this.police = font);                 // continuer quand c'est prêt
MsdfFontLoader.load(flux).exceptionally(erreur -> { erreur.printStackTrace(); return null; });
this.police = MsdfFontLoader.load(flux).join();                                   // bloquer, au démarrage
CompletableFuture.allOf(regular, bold, italic).join();                        // attendre toute une famille
```

Une police illisible complète le future exceptionnellement au lieu d'échouer en silence : ne jetez jamais le future renvoyé. Lancez le chargement au démarrage de l'application pour que la police soit prête avant que l'UI qui l'utilise ne s'ouvre.

## Jeu de caractères

Un atlas MSDF ne contient que les caractères déclarés dans le `charset.txt` au moment de la génération. Les glyphes absents rendent un placeholder. Générez des atlas avec la plage Latin complète + symboles pour des polices à usage général — voir [MSDF Atlas](msdf-atlas.md).

## Bonnes pratiques

- **Chargez les polices une fois, au démarrage.** Elles sont réutilisées entre toutes les UIs.
- **Cachez `TextInfo`.** Un par style logique (titre, body, code) — réutilisez par nœud.
- **Pré-dimensionnez votre atlas.** 2048×2048 tient ~500 glyphes à 48 px. Pour CJK, 4096×4096 ou plusieurs atlas par script.

## Autres implémentations de police

MSDF est une implémentation du contrat de police, pas le contrat lui-même. Tout ce qui dessine ou mesure du texte — `TextInfo`, `TextNode`, `DrawUtils` — ne connaît que deux interfaces de `be.zeldown.joid.lib.font` :

| Interface | Rôle |
|---|---|
| `IFont` | Ce que porte un `TextInfo`. Fournit son provider. |
| `IFontProvider` | Dessine et mesure une chaîne pour un `TextInfo`. |

Les classes MSDF vivent à part, dans `be.zeldown.joid.lib.font.impl.msdf`. Un backend qui a son propre rendu de texte — la police bitmap d'un jeu, l'API texte d'une plateforme — implémente ces deux interfaces dans son propre paquet `impl/<nom>`, et tous les nœuds de texte fonctionnent avec, sans changement.

## Voir aussi

- [MSDF Atlas](msdf-atlas.md) — génération des fichiers d'atlas.
- [TextNode](../nodes/design/text.md) — rendu de texte.