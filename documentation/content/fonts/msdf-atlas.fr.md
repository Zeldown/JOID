# MSDF Atlas

Comment générer la paire `.png` + `.json` que le système de polices de JOID consomme.

## Outil : `msdf-atlas-gen`

Téléchargez depuis [github.com/Chlumsky/msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen/releases).

## Générer une police

La commande exacte utilisée pour produire les polices livrées avec JOID :

```bash
msdf-atlas-gen.exe -font font.ttf -charset charset.txt -dimensions 2048 2048 -imageout font.png -json font.json -type msdf -pxrange 24 -coloringstrategy distance
```

Pointez `-font` vers votre source TTF/OTF, mettez les caractères voulus dans `charset.txt`, et c'est tout. Les autres flags sont les défauts validés de JOID.

### Paramètres

| Flag | Rôle |
|---|---|
| `-font` | Fichier source TTF/OTF |
| `-charset` | Liste de caractères à inclure (un par ligne) |
| `-dimensions` | Taille de l'atlas en pixels |
| `-imageout` | Chemin PNG en sortie |
| `-json` | Chemin JSON en sortie (metrics de glyphes) |
| `-type msdf` | SDF multi-canal — meilleure qualité pour les polices UI |
| `-pxrange` | Portée du distance field (plus haut = plus lisse aux petites tailles, plus flou aux grandes ; 24 est un bon défaut) |
| `-coloringstrategy distance` | Algo de coloration MSDF (ne pas toucher sauf si vous savez ce que vous faites) |

## Charset

Le fichier `charset.txt` contient les caractères à inclure, un par ligne ou sur une seule ligne. Un bon set de départ :

```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
0123456789
!@#$%^&*()_+-=[]{}|;:,.<>?/~`'"\
 éèêëàâäôöûüùçÉÈÊËÀÂÄÔÖÛÜÙÇ
```

Pour CJK, utilisez un sous-ensemble — le CJK complet fait 30 000+ glyphes et ne tient pas dans un atlas 2048×2048.

## Structure de sortie

JOID attend cette disposition :

```
assets/
└── fonts/
    └── MyFont/
        ├── font.png
        └── font.json
```

Chargez-les en passant les deux streams à `FontLoader.load(pngStream, jsonStream)`.

## Schéma JSON

Structure `font.json` (simplifiée) :

```json
{
    "atlas": {
        "type": "msdf",
        "distanceRange": 24,
        "size": 32,
        "width": 2048,
        "height": 2048,
        "yOrigin": "bottom"
    },
    "metrics": {
        "emSize": 1,
        "lineHeight": 1.171875,
        "ascender": 0.9296875,
        "descender": -0.2421875,
        ...
    },
    "glyphs": [
        { "unicode": 65, "advance": 0.6, "planeBounds": {...}, "atlasBounds": {...} },
        ...
    ]
}
```

Le `FontLoader` de JOID gère à la fois `yOrigin: bottom` et `yOrigin: top`.

## Plusieurs poids

Générez un atlas par poids :

```bash
msdf-atlas-gen -font Inter-Regular.ttf -imageout Regular/font.png -json Regular/font.json ...
msdf-atlas-gen -font Inter-Bold.ttf -imageout Bold/font.png -json Bold/font.json ...
msdf-atlas-gen -font Inter-Italic.ttf -imageout Italic/font.png -json Italic/font.json ...
```

Chargez chacun séparément et liez-les à des `TextInfo` différents.

## Bonnes pratiques

- **`-pxrange 24`** est un bon défaut général.
- **`-dimensions 2048 2048`** tient la plupart des polices Latines. Surveillez l'avertissement `overflow` — si l'atlas déborde, passez à 4096 ou réduisez le charset.
- **Conservez le canal alpha du PNG.** Certains outils l'enlèvent ; JOID a besoin de RGBA.
- **Régénérez au changement de police source.** Même des ajustements mineurs (espacement, hinting) imposent un nouvel atlas.

## Voir aussi

- [Custom Fonts](custom-font.md) — chargement et utilisation des polices.
- [TextNode](../nodes/design/text.md).