# Licence

JOID est publié sous **licence Apache 2.0**. Le texte complet est à la racine du dépôt, dans [LICENSE](https://github.com/Zeldown/JOID/blob/main/LICENSE), et il est embarqué dans chaque JAR sous `META-INF/LICENSE`.

## Ce que vous pouvez faire

Utiliser JOID dans un produit commercial, une application propriétaire, un jeu payant, un outil interne ou un projet open source. Le forker, le modifier, l'embarquer, vendre ce que vous construisez avec. Rien à demander, aucun partage de revenus, aucune restriction sur le type d'interface que vous dessinez.

## Ce que vous devez

Uniquement si vous redistribuez JOID lui-même — seul, modifié, ou embarqué dans votre propre artefact. La section 4 de la licence demande quatre choses :

| Obligation | En pratique |
|---|---|
| Joindre la licence | Gardez `META-INF/LICENSE` dans le JAR que vous livrez, ou placez-en une copie à côté de votre binaire. |
| Conserver les mentions | Ne retirez pas les mentions de copyright, de brevet, de marque et d'attribution de ce que vous redistribuez. |
| Signaler vos modifications | Vous avez modifié un fichier de JOID ? Dites-le — une ligne de changelog ou un en-tête suffit. |
| Transmettre le NOTICE | Reproduisez le contenu de `META-INF/NOTICE` dans votre propre fichier de mentions, votre documentation ou votre écran de crédits. |

Utiliser JOID comme bibliothèque, sans redistribuer JOID lui-même, ne vous demande rien.

## Brevets et marque

Chaque contributeur vous accorde une licence de brevet sur sa contribution. Engager un contentieux en brevet au sujet de JOID met fin à la vôtre.

Le nom **JOID** n'est pas concédé : donnez un autre nom à votre fork. Le citer dans une mention d'attribution est attendu et autorisé.

## Composants tiers

| Composant | Licence | Où |
|---|---|---|
| Universal Tween Engine | Apache-2.0 | embarqué dans `lib/animation/tweenengine` |
| msdfgen, l'algorithme | MIT | réimplémenté en Java dans `msdf/`, livré dans son propre zip |
| LWJGL | BSD-3-Clause | déclaré par votre application |
| JavaCV | Apache-2.0 | déclaré par votre application, pour la vidéo |
| Builds FFmpeg | leurs propres termes | déclarés par votre application, pour la vidéo |

Les JARs de JOID n'embarquent aucune bibliothèque tierce : les versions que vous déclarez sont celles qui tournent. Voir [Installation](installation.md).
