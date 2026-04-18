# Easing

Le catalogue d'équations d'easing disponibles via `TweenEquations`. Chaque équation prend un temps normalisé `t ∈ [0, 1]` et retourne une valeur qui façonne la courbe d'interpolation.

## Référence rapide

| Famille | `IN` | `OUT` | `IN_OUT` |
|---|---|---|---|
| Linear | `LINEAR` | — | — |
| Quadratic | `QUAD_IN` | `QUAD_OUT` | `QUAD_IN_OUT` |
| Cubic | `CUBIC_IN` | `CUBIC_OUT` | `CUBIC_IN_OUT` |
| Quartic | `QUART_IN` | `QUART_OUT` | `QUART_IN_OUT` |
| Quintic | `QUINT_IN` | `QUINT_OUT` | `QUINT_IN_OUT` |
| Sine | `SINE_IN` | `SINE_OUT` | `SINE_IN_OUT` |
| Expo | `EXPO_IN` | `EXPO_OUT` | `EXPO_IN_OUT` |
| Circ | `CIRC_IN` | `CIRC_OUT` | `CIRC_IN_OUT` |
| Back | `BACK_IN` | `BACK_OUT` | `BACK_IN_OUT` |
| Bounce | `BOUNCE_IN` | `BOUNCE_OUT` | `BOUNCE_IN_OUT` |
| Elastic | `ELASTIC_IN` | `ELASTIC_OUT` | `ELASTIC_IN_OUT` |

## Convention de nommage

- **`_IN`** — démarrage lent, fin rapide. Pour les mouvements d'« entrée ».
- **`_OUT`** — démarrage rapide, fin lente. Pour les mouvements de « sortie » ou de settlement.
- **`_IN_OUT`** — lent aux deux extrémités, rapide au milieu. Pour les mouvements de « trajet ».

## Choisir la bonne courbe

- **Fades & slides UI** — `CUBIC_OUT` ou `QUART_OUT`. Sensation réactive sans overshoot.
- **Attirer l'attention** — `BACK_OUT` pour un petit overshoot, `ELASTIC_OUT` pour un rebond prononcé.
- **Mouvement physique** — `BOUNCE_OUT` pour des objets qui tombent, `ELASTIC_IN_OUT` pour un effet élastique.
- **Seeking / scrubbing** — `QUAD_IN_OUT` ou `SINE_IN_OUT` — doux sans caractère.
- **N'utilisez jamais `LINEAR` pour l'UI.** Sensation robotique.

## Comparaison par exemples

```java
// Ouverture de menu — rapide à l'arrivée, settlement doux
animator.sequence(300F, 1F, TweenEquations.CUBIC_OUT).start();

// Popup qui apparaît — léger overshoot pour le polish
animator.sequence(400F, 1F, TweenEquations.BACK_OUT).start();

// Fermeture — accélération vers la sortie
animator.sequence(250F, 0F, TweenEquations.CUBIC_IN).start();

// Feedback interactif (press de bouton) — net à l'entrée, relax à la sortie
animator
    .sequence(80F, 0.95F, TweenEquations.QUAD_IN)
    .push(200F, 1F, TweenEquations.ELASTIC_OUT)
    .start();
```

`sequence(...)` et `parallel(...)` créent chacun une nouvelle timeline — utilisez `push(...)` pour ajouter un tween à la timeline qui vient d'être construite (voir [TweenAnimator](tween-animator.md)). Les durées sont des `float` millisecondes.

## Utilisation sur le hover d'un Node

```java
node.hoverEquation(TweenEquations.CUBIC_OUT);
node.hoverDuration(250L);
```

Défaut `LINEAR` — override sur la plupart des nœuds. `hoverDuration` prend un `long` (millisecondes).

## Easing custom

Étendez `TweenEquation` :

```java
public class SquareEasing extends TweenEquation {
    @Override
    public float compute(float t) {
        return t * t;
    }

    @Override
    public String toString() {
        return "Square";
    }
}

animator.sequence(400F, 100F, new SquareEasing()).start();
```

## Voir aussi

- [TweenAnimator](tween-animator.md) — API d'animation complète.
- [Transitions](../ui/transitions.md) — easing au niveau UI.