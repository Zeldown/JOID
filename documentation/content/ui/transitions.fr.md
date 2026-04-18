# Transitions

Animations d'entrée / sortie qui tournent autour de l'ouverture et de la fermeture d'une `UI`, backed par le Tween Engine.

## Structure

`Transition` est abstract et figée uniquement dans sa forme statique :

```java
public abstract class Transition {
    private final In in;
    private final Out out;
    // ...
}
```

Une transition est une paire de states — un `In`, un `Out` — chacun avec son propre `TweenAnimator`. `In` et `Out` sont elles-mêmes des sous-classes abstract de `TransitionState` :

```java
public static abstract class TransitionState {
    private final TweenAnimator animator;
    public TransitionState(float defaultValue) { ... }

    public abstract void init(UI ui);
    public abstract void start();
    public abstract void pre(UI ui, double mouseX, double mouseY);
    public abstract void post(UI ui, double mouseX, double mouseY);
}

public static abstract class In  extends TransitionState { public In()  { super(0F); } }
public static abstract class Out extends TransitionState { public Out() { super(1F); } }
```

L'animator de `In` démarre à `0F`, celui de `Out` à `1F`. `init` tourne une fois avec l'UI au moment de l'attach, `start` lance le tween, `pre`/`post` encadrent le draw de l'UI.

## Attacher une transition

Appelez `setTransition(...)` sur votre UI :

```java
public class MyUI extends UI {
    public MyUI() {
        setTransition(new PopTransition());
    }
}
```

## Intégrée : `PopTransition`

`PopTransition` scale l'UI de `0.75` à `1.0` autour de sa position d'ancre à l'entrée, et inverse à la sortie, avec `QUART_OUT` / `QUART_IN` sur `130ms`.

```java
public class PopTransition extends Transition {
    public PopTransition() {
        super(new PopInTransition(), new PopOutTransition());
    }

    public static class PopInTransition extends Transition.In {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(130F, 1F, TweenEquations.QUART_OUT).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final double scale = 0.75D + getAnimator().getValue() * 0.25D;
            GL11.glPushMatrix();
            GL11.glTranslated(ui.getData().getAnchorPositionX(), ui.getData().getAnchorPositionY(), 0);
            GL11.glScaled(scale, scale, 1D);
            GL11.glTranslated(-ui.getData().getAnchorPositionX(), -ui.getData().getAnchorPositionY(), 0);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }

    public static class PopOutTransition extends Transition.Out {
        // symétrique avec QUART_IN, value 0F
    }
}
```

## Écrire une transition custom

Une transition est toujours un appel au constructeur parent (`super(new MyIn(), new MyOut())`) plus deux inner classes qui étendent `In` et `Out`. Dans chacune, câblez le tween dans `start()` et appliquez-le dans `pre()` / `post()` :

```java
public class SlideTransition extends Transition {

    public SlideTransition() {
        super(new SlideIn(), new SlideOut());
    }

    public static class SlideIn extends Transition.In {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(300F, 1F, TweenEquations.CUBIC_OUT).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final float offset = (1F - getAnimator().getValue()) * (float) ui.getWidth();
            GL11.glPushMatrix();
            GL11.glTranslatef(offset, 0F, 0F);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }

    public static class SlideOut extends Transition.Out {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(250F, 0F, TweenEquations.CUBIC_IN).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final float offset = (1F - getAnimator().getValue()) * -(float) ui.getWidth();
            GL11.glPushMatrix();
            GL11.glTranslatef(offset, 0F, 0F);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }
}
```

Il n'y a **pas** de setter `setIn(...)` / `setOut(...)` ni de constructeur `TransitionState(equation, duration, from, to)` — la classe est construite autour des méthodes abstract ci-dessus.

## Désactiver un state

`TransitionState` expose `enable()` / `disable()` et `isEnabled()`. Le `start()` d'un state désactivé est un no-op, donc le tween ne se déclenche jamais :

```java
ui.getTransition().getIn().disable();
```

## UIs sans transition

N'appelez pas `setTransition(...)` du tout — l'UI entre et sort instantanément. Il n'y a pas de constructeur `Transition` « no-op » puisque la classe est abstract.

## Voir aussi

- [TweenAnimator](../animations/tween-animator.md) — l'animator qui pilote chaque state.
- [Easing](../animations/easing.md) — équations d'easing.
- [Bridge](bridge.md) — comment `open` / `close` pilotent les transitions.