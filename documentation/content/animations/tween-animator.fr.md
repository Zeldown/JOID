# TweenAnimator

Wrapper haut niveau autour de l'Universal Tween Engine. Anime une valeur `float` unique dans le temps avec n'importe quelle courbe d'easing — tweens simples, séquences, groupes parallèles.

## Créer

```java
TweenAnimator animator = TweenAnimator.create();        // démarre à 0F
TweenAnimator animator = TweenAnimator.create(0F);      // valeur initiale explicite
```

## Démarrer un tween

```java
animator.sequence(400F, 100F).start();
```

Tween de la valeur courante vers `100F` sur `400ms` avec easing `LINEAR` par défaut. **Les durations sont en `float` millisecondes**, pas `long`.

## Easing

```java
animator.sequence(400F, 100F, TweenEquations.CUBIC_OUT).start();
```

Voir `Easing` pour le catalogue complet.

## Chaînage

`sequence(...)` et `parallel(...)` **construisent une timeline neuve** — chaque appel remplace toute timeline existante sur l'animator. Pour étendre la timeline courante, utilisez `push(...)` :

```java
animator
    .sequence(400F, 100F, TweenEquations.CUBIC_OUT)
    .push(200F, 50F, TweenEquations.CUBIC_IN)
    .push(300F, 100F, TweenEquations.BACK_OUT)
    .start();
```

`sequence(...)` crée une timeline séquentielle (chaque tween attend le précédent). `parallel(...)` crée une timeline parallèle (tous les tweens tournent simultanément). `push(...)` append à la timeline qui vient d'être créée.

## Callback de complétion

```java
animator
    .sequence(400F, 100F)
    .setCallback(tween -> System.out.println("done"))
    .start();
```

`setCallback(Consumer<BaseTween<?>>)` enregistre un callback `TweenCallback.END` — se déclenche une fois quand la timeline termine. Il n'y a pas de callback par frame ; lisez `animator.getValue()` depuis votre boucle de draw.

## Lire la valeur

```java
final float current = animator.getValue();
```

Utilisez-la dans un override de `draw()`, un callback de watch, ou liez-la via un lambda supplier quand l'API en accepte un.

## Cadence de mise à jour

L'animator a besoin d'appels `update()` à chaque frame pour avancer :

```java
animator.update();            // utilise le delta wall-clock depuis le dernier appel
animator.update(deltaMs);     // delta explicite (× speed)
```

`TweenAnimator.setSpeed(float)` scale le delta effectif — `1F` normal, `2F` deux fois plus rapide, `0F` pause.

## Exemple — fade in

```java
final TweenAnimator fade = TweenAnimator.create(0F);
fade.sequence(500F, 1F, TweenEquations.CUBIC_OUT).start();

RectNode.create(0, 0, 200, 100)
    .color(() -> Color.WHITE.copyAlpha(fade.getValue()))
    .attach(parent);
```

## Exemple — chaîne wobble

```java
final TweenAnimator wobble = TweenAnimator.create(0F);
wobble
    .sequence(150F, 10F, TweenEquations.CUBIC_OUT)
    .push(150F, -10F, TweenEquations.CUBIC_IN_OUT)
    .push(150F, 5F, TweenEquations.CUBIC_IN_OUT)
    .push(150F, 0F, TweenEquations.CUBIC_IN)
    .start();

node.x(() -> baseX + wobble.getValue());
```

## Pistes parallèles

Utilisez plusieurs instances `TweenAnimator` pour des axes indépendants — chacune a son `TweenManager` :

```java
final TweenAnimator x = TweenAnimator.create(0F);
final TweenAnimator y = TweenAnimator.create(0F);

x.sequence(500F, 200F, TweenEquations.CUBIC_OUT).start();
y.sequence(300F, 100F, TweenEquations.BOUNCE_OUT).start();
```

## Intégration avec le hover des Node

Chaque `Node` a un animator de hover intégré :

```java
node.hoverDuration(200L);
node.hoverEquation(TweenEquations.CUBIC_OUT);

float t = node.hoverValue(1F);   // 0.0 → 1.0 pendant le fade de hover
```

## Voir aussi

- `Easing` — catalogue d'équations d'easing.
- `Transitions` — animations in/out au niveau UI.