# Introduction

Une boîte à outils d'UI basée sur les composants, construite sur LWJGL 2. Java pur, sans CSS, sans XML, sans parseur d'exécution — le code que vous écrivez est directement la mise en page que le GPU dessine.

## Ce que c'est

JOID vous fournit un arbre de nœuds en mode retenu, un système d'état réactif construit autour de `Signal<T>`, un pipeline de shaders pour composer des effets GL, et une interface `Bridge` qui adapte la bibliothèque à n'importe quel hôte LWJGL. Vous construisez l'arbre une fois, vous le mutez à travers des signaux, et JOID s'occupe de la composition et du rendu. Pas de DSL à apprendre, pas de format de scene graph à sérialiser, pas de moteur d'exécution à démarrer — la bibliothèque est un ensemble de classes chaînables que vous câblez ensemble en Java.

## Pourquoi pas une UI pilotée par CSS

Une UI pilotée par CSS parcourt un pipeline familier à chaque changement : tokenisation de la feuille de style, analyse, construction d'un AST, résolution des sélecteurs, diff de l'arbre, puis traduction du résultat en appels de dessin. Ce pipeline existe parce que la source de vérité de l'UI est une chaîne de caractères. JOID n'a pas cette source de vérité — la source de vérité est votre code, et il compile directement en appels de méthodes qui aboutissent à la couche de dessin.

La conséquence pratique est que le compilateur devient votre linter d'UI. Renommez `RectNode` et toutes les références suivent. Câblez le mauvais type de signal dans un watcher et vous obtenez une erreur de compilation plutôt qu'une défaillance silencieuse à l'exécution. Refactorez un écran et l'IDE vous aide vraiment — il n'y a pas de sélecteur sous forme de chaîne, pas de lookup `#foo`, pas de cascade avec laquelle se battre.

## Réactivité ciblée

Le système réactif de JOID ne re-rend pas des sous-arbres. Un `Signal` notifie uniquement les nœuds qui l'observent explicitement ; rien d'autre dans l'arbre n'est parcouru ni touché.

```java
final Signal<Integer> score = new Signal<>(0);

TextNode.create("0")
    .watch(score, (node, value) -> node.text(String.valueOf(value)))
    .attach(hud);

score.set(42);
```

Le callback `watch` s'exécute, le nœud cible met à jour exactement la propriété qu'on lui a dit de mettre à jour, et le reste de l'UI reste intact. Pas de phase de diff, pas de réconciliation, pas d'AST mis en cache. `Node.watch(signal, condition, properties)` va plus loin : ne se déclenche que si un prédicat est vrai, et limite la mise à jour à des propriétés spécifiques — couleur seule, body seul, layout seul.

## Agnostique à l'hôte par conception

JOID communique avec son hôte à travers une seule interface `Bridge` — dimensions du viewport, événements d'entrée, hooks du contexte GL. Chaque hôte est une petite classe d'adaptation ; rien d'autre dans la bibliothèque ne change entre les environnements.

Le même arbre de nœuds, les mêmes effets, les mêmes signaux et les mêmes stores fonctionnent à l'intérieur d'une fenêtre LWJGL autonome, d'un panneau d'éditeur intégré à un moteur plus grand, d'un overlay de debug greffé sur un jeu, ou de tout autre runtime personnalisé qui expose un contexte OpenGL. Changez le `Bridge`, gardez l'UI. C'est la raison principale pour laquelle JOID existe comme bibliothèque séparée et autonome.

## Ce que JOID n'est pas

JOID n'est pas un runtime de navigateur : il n'y a ni DOM, ni CSS, ni sous-ensemble HTML. Ce n'est pas un mode immédiat : les nœuds sont retenus, et vous mutez l'arbre via des signaux et des setters directs. Ce n'est pas un remplacement pour tous les toolkits d'UI — pour les menus natifs de l'OS ou l'intégration à l'arbre d'accessibilité, préférez Swing ou JavaFX. JOID vise les UIs rendues et composées par le GPU qui vivent à l'intérieur d'une application OpenGL existante.

## À qui c'est destiné

Les développeurs qui intègrent des UIs dans une application basée sur LWJGL — jeux, outils, éditeurs, overlays — et qui veulent maîtriser le chemin de rendu sans embarquer un runtime web. Quiconque est à l'aise avec Java 8+ et préfère les garanties du compilateur au débogage de cascade de styles.

## Prochaines étapes

- `Installation` — Gradle, Maven, bibliothèques natives.
- `Quick Start` — une UI en moins de trente lignes.
- `Core Concepts` — le modèle mental derrière les nœuds, les effets et les signaux.