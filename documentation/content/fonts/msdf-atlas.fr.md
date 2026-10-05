# Atlas MSDF

Comment transformer un `.ttf` ou un `.otf` en fichier `font.msdf`, celui que le système de polices de JOID consomme. `MsdfFontLoader` accepte aussi directement le fichier de police et génère ce fichier à l'exécution — voir [Fichiers de police](custom-font.md#fichiers-de-police).

## Le générateur

JOID embarque son propre générateur. Il est écrit en Java pur, tourne sur n'importe quel OS, ne demande aucun binaire natif, et lit le crénage directement dans la police — aussi bien la table `kern` historique que le positionnement par paires `GPOS` qu'utilisent les polices modernes. La même police source donne le même atlas sur toutes les machines.

### Depuis une release

Téléchargez `joid-msdf-generator-X.Y.Z.zip` depuis la [page des releases](https://github.com/Zeldown/JOID/releases), décompressez-le où vous voulez et lancez le script de votre système. Java 8 ou plus récent est la seule exigence.

```bash
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
```

```bat
.\msdf.bat --font Inter-Regular.ttf --output assets\fonts\Inter-Regular
```

Les deux scripts enveloppent `java -jar joid-msdf-X.Y.Z.jar`, que vous pouvez appeler directement. La commande écrit un unique `font.msdf` dans le dossier de sortie.

| Option | Défaut | Rôle |
|---|---|---|
| `--font` | — | Fichier source `.ttf` ou `.otf` |
| `--output` | `output` | Dossier qui reçoit `font.msdf` |
| `--charset` | `[32, 563]` | Points de code à inclure, fichier ou plages en ligne |
| `--range` | `24` | Portée du champ de distance en pixels — plus haut adoucit en petit, floute en grand |
| `--width` / `--height` | `2048` | Taille de l'atlas en pixels |
| `--size` | — | Impose la taille d'em en pixels au lieu de l'ajuster à l'atlas |

La résolution des glyphes n'est pas un paramètre : le générateur cherche par dichotomie la plus grande taille d'em dont les glyphes tiennent encore dans l'atlas, et l'inscrit dans le fichier. `--size` court-circuite cette recherche et échoue si les glyphes ne tiennent pas.

### Depuis les sources

Dans un clone du dépôt, le module `:msdf` expose le même générateur sous forme de tâche Gradle :

```bash
./gradlew :msdf:generateFont -Pfont=/chemin/vers/Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
```

| Propriété | Défaut | Rôle |
|---|---|---|
| `-Pfont` | — | Fichier source `.ttf` ou `.otf` |
| `-Poutput` | `build/font` | Dossier qui reçoit `font.msdf` |
| `-Pcharset` | `msdf/charset.txt` | Points de code à inclure |
| `-Prange` | `24` | Portée du champ de distance en pixels |
| `-Pwidth` / `-Pheight` | `2048` | Taille de l'atlas en pixels |

`./gradlew msdfGenerator` empaquette ce module dans le zip de release, dans `build/distributions`.

## Jeu de caractères

`charset.txt`, livré à côté du jar dans le zip de release, accepte des plages et des points de code isolés :

```
[32, 563]
```

Cette plage couvre le latin, le latin-1, le latin étendu A et une partie de l'étendu B — le jeu qu'utilisent les polices de JOID. Plusieurs entrées se séparent par des virgules, et le tout peut se passer en ligne plutôt que dans un fichier :

```bash
./msdf.sh --font Inter-Regular.ttf --output fonts/Inter --charset "[32, 126], [160, 255], 8364"
```

Les points de code absents de la police sont ignorés, le même jeu convient donc à toutes les sources.

Pour le CJK, prenez un sous-ensemble : un jeu complet dépasse les 30 000 glyphes et ne tiendra pas dans un atlas 2048×2048.

## Le fichier `.msdf`

Un seul fichier contient tout : nom, graisse et style de la face, métriques de l'atlas, boîtes des glyphes, paires de crénage et champ de distance multicanal, le tout compressé d'un bloc. La graisse et l'indicateur d'italique viennent de la table `OS/2` de la police source : une famille se charge sans dire à JOID quel fichier est lequel, et le nom est le nom complet de la police — `Montserrat Regular` — qu'affichent les avertissements du mode dev. Seul le format du JOID en cours se charge : un fichier écrit par un ancien générateur est refusé et doit être généré à nouveau, alors qu'un fichier de police chargé directement régénère son cache tout seul. Le champ est stocké avec le même filtrage de lignes adaptatif qu'un PNG, si bien qu'une police complète pèse à peu près ce que pèserait le `.png` seul. La table de crénage est groupée par premier codepoint et écrite en deltas de taille variable, en unités de fonte : elle coûte un dixième d'une liste de paires à plat, cinquante mille paires tenant dans quinze kilo-octets.

```
assets/
└── fonts/
    └── MaPolice/
        └── font.msdf
```

Chargez-le avec un seul flux :

```java
MsdfFontLoader.load(getClass().getResourceAsStream("/assets/fonts/MaPolice/font.msdf")).thenAccept(font -> this.maPolice = font);
```

## Plusieurs graisses

Générez un atlas par graisse et par style :

```bash
./msdf.sh --font Inter-Regular.ttf --output assets/fonts/Inter-Regular
./msdf.sh --font Inter-Italic.ttf  --output assets/fonts/Inter-Italic
./msdf.sh --font Inter-Bold.ttf    --output assets/fonts/Inter-Bold
```

Puis chargez-les ensemble comme une seule famille — chaque `TextInfo` choisit sa graisse, voir [Graisses et styles](custom-font.md#graisses-et-styles) :

```java
MsdfFontLoader.load(fluxRegular, fluxItalic, fluxBold).thenAccept(font -> this.maPolice = font);
```

## Bonnes pratiques

- **Gardez la portée à 24.** C'est la valeur de toutes les polices embarquées.
- **2048×2048 suffit à la plupart des polices latines.** Si le générateur annonce une petite taille d'em, réduisez le jeu de caractères plutôt que d'agrandir l'atlas.
- **Régénérez après toute modification de la police source.** Chasse, contours et crénage sont tous figés dans le fichier.

## Voir aussi

- [Polices personnalisées](custom-font.md) — charger et utiliser une police.
- [Balisage & effets](markup-effects.md) — styler une chaîne de l'intérieur.
- [TextNode](../nodes/design/text.md).
