# Atlas MSDF

Comment transformer un `.ttf` ou un `.otf` en fichier `font.msdf`, celui que le système de polices de JOID consomme.

## Le générateur

JOID embarque son propre générateur dans le module `:msdf`. Il est écrit en Java pur, tourne sur n'importe quel OS, ne demande aucun binaire natif, et lit le crénage directement dans la police — aussi bien la table `kern` historique que le positionnement par paires `GPOS` qu'utilisent les polices modernes.

```bash
./gradlew :msdf:generateFont -Pfont=/chemin/vers/Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
```

Il écrit un unique `font.msdf` dans le dossier `-Poutput`.

### Paramètres

| Propriété | Défaut | Rôle |
|---|---|---|
| `-Pfont` | — | Fichier source `.ttf` ou `.otf` |
| `-Poutput` | `build/font` | Dossier qui reçoit `font.msdf` |
| `-Pcharset` | `msdf/charset.txt` | Points de code à inclure |
| `-Prange` | `24` | Portée du champ de distance en pixels — plus haut adoucit en petit, floute en grand |
| `-Pwidth` / `-Pheight` | `2048` | Taille de l'atlas en pixels |

La résolution des glyphes n'est pas un paramètre : le générateur cherche par dichotomie la plus grande taille d'em dont les glyphes tiennent encore dans l'atlas, et l'inscrit dans le fichier.

### Régénérer les polices livrées avec JOID

`msdf/fonts.txt` associe chaque atlas embarqué à son fichier source. Pointez la tâche vers un dossier contenant ces fichiers :

```bash
./gradlew :msdf:rebuildFonts -Pfonts=/chemin/vers/les/sources
```

## Jeu de caractères

`charset.txt` accepte des plages et des points de code isolés :

```
[32, 563]
```

Cette plage couvre le latin, le latin-1, le latin étendu A et une partie de l'étendu B — le jeu qu'utilisent les polices de JOID. Les points de code absents de la police sont ignorés, le même jeu convient donc à toutes les sources.

Pour le CJK, prenez un sous-ensemble : un jeu complet dépasse les 30 000 glyphes et ne tiendra pas dans un atlas 2048×2048.

## Le fichier `.msdf`

Un seul fichier contient tout : métriques de l'atlas, boîtes des glyphes, paires de crénage et champ de distance multicanal, le tout compressé d'un bloc. Le champ est stocké avec le même filtrage de lignes adaptatif qu'un PNG, si bien qu'une police complète pèse à peu près ce que pèserait le `.png` seul, les métriques et la table de crénage venant en prime.

```
assets/
└── fonts/
    └── MaPolice/
        └── font.msdf
```

Chargez-le avec un seul flux :

```java
FontLoader.load(getClass().getResourceAsStream("/assets/fonts/MaPolice/font.msdf"), font -> this.maPolice = font);
```

## Atlas hérités

Le couple `font.json` + `font.png` produit par [msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen) se charge toujours, via les surcharges à `FontInputStream` :

```java
FontLoader.load(new FontInputStream(fluxJson, fluxPng), font -> this.maPolice = font);
```

Ces atlas ne portent aucun crénage : `msdf-atlas-gen` ne lit que la table `kern`, que la plupart des polices modernes ne fournissent plus.

## Plusieurs graisses

Générez un atlas par graisse, puis chargez chacun séparément et associez-les à des `TextInfo` différents :

```bash
./gradlew :msdf:generateFont -Pfont=Inter-Regular.ttf -Poutput=assets/fonts/Inter-Regular
./gradlew :msdf:generateFont -Pfont=Inter-Bold.ttf    -Poutput=assets/fonts/Inter-Bold
```

Une graisse normale et une grasse peuvent aussi être réunies dans un seul `CustomFont`, celui dans lequel puise le style `§l` :

```java
FontLoader.load(fluxRegular, fluxBold, font -> this.maPolice = font);
```

## Bonnes pratiques

- **Gardez la portée à 24.** C'est la valeur de toutes les polices embarquées.
- **2048×2048 suffit à la plupart des polices latines.** Si le générateur annonce une petite taille d'em, réduisez le jeu de caractères plutôt que d'agrandir l'atlas.
- **Régénérez après toute modification de la police source.** Chasse, contours et crénage sont tous figés dans le fichier.

## Voir aussi

- [Polices personnalisées](custom-font.md) — charger et utiliser une police.
- [TextNode](../nodes/design/text.md).
