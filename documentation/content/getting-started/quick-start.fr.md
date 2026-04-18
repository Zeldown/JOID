# Quick Start

Ce tutoriel met une fenêtre JOID à l'écran en moins de 30 lignes, puis montre comment y ajouter de vrais composants. Il suppose que vous avez terminé l'[Installation](installation.md).

## Une fenêtre minimale

JOID embarque une `DemoWindow` prête à l'emploi qui ouvre une fenêtre LWJGL 2, s'enregistre comme bridge d'UI, et lance la boucle principale. Pour votre propre hôte vous implémenteriez `UIBridge` vous-même (voir [Bridge](../ui/bridge.md)), mais pour démarrer :

```java
public static void main(final String[] args) {
    JOID.inst().setDevMode(true).setDemoMode(true).load();
    try {
        final DemoWindow window = new DemoWindow();
        BridgeHandler.register(window);
        window.run();
    } catch (final LWJGLException e) {
        e.printStackTrace();
    }
}
```

Les deux flags sur `JOID.inst()` activent les gestes de debug (`setDevMode`) et chargent la police `DemoFont` embarquée (`setDemoMode`) pour que vous ayez une police utilisable sans devoir shipper votre propre atlas. Les deux valent `false` par défaut — voir [Bootstrap](core-concepts.md#bootstrap) pour la liste complète des flags et ce qu'ils activent.

L'exécution ouvre une fenêtre noire 1920×1080. Appuyez sur ESC pour fermer.

## Votre première UI

Une classe d'UI étend `UI` et implémente `init()` — appelée une seule fois à l'ouverture. Attachez-y vos nœuds.

```java
public class MyFirstUI extends UI {

    @Override
    public void init() {
        RectNode.create(760, 440, 400, 200)
            .color(Color.BLUE.toGradient(Color.MAGENTA))
            .effect(RoundedNodeEffect.create(20F))
            .effect(BorderNodeEffect.create(Color.WHITE, 2F))
            .attach(this);
    }
}
```

Ouvrez-la depuis votre main :

```java
JOID.open(new MyFirstUI());
```

Vous avez maintenant un rectangle en dégradé avec bords arrondis et bordure blanche, centré à l'écran.

## Ajouter de l'interaction

Chaque nœud a `onClick`, `onHover` et des dizaines d'autres callbacks. Chaînez-les directement :

```java
RectNode.create(760, 440, 400, 200)
    .color(Color.BLUE, Color.CYAN)                // normal, hovered
    .effect(RoundedNodeEffect.create(20F))
    .onClick((node, mouseX, mouseY, clickType) -> {
        System.out.println("Clicked at " + mouseX + ", " + mouseY);
    })
    .hover(() -> "Click me!")
    .attach(this);
```

`color(normal, hovered)` interpole automatiquement en fonction de l'état de survol. `hover(() -> ...)` fournit une infobulle — chaîne, liste de chaînes ou nœud complet.

## Texte et layouts

Enveloppez les enfants dans un nœud de layout pour les organiser :

```java
FlexNode.horizontal(40, 40, 80).margin(20).body(flex -> {
    RectNode.create(0, 0, 200, 80).color(Color.RED).attach(flex);
    RectNode.create(0, 0, 200, 80).color(Color.GREEN).attach(flex);
    RectNode.create(0, 0, 200, 80).color(Color.BLUE).attach(flex);
}).attach(this);
```

`FlexNode.horizontal(x, y, height)` positionne ses enfants automatiquement en ligne, en gérant la marge et le dépassement.

Pour du texte, utilisez `TextNode` avec le builder `Text` :

```java
TextNode.create(100, 100)
    .text(Text.create("Hello JOID", TextInfo.create(myFont, 32, Color.WHITE)))
    .attach(this);
```

Les polices sont chargées via des atlas MSDF — voir [Custom Fonts](../fonts/custom-font.md).

## État réactif

Plutôt que recalculer manuellement, liez les nœuds à des observables `Signal<T>` :

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(100, 100)
    .text(() -> Text.create("Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(this);

RectNode.create(100, 200, 100, 40)
    .color(Color.GRAY)
    .onClick((n, mx, my, ct) -> count.set(count.getOrDefault() + 1))
    .attach(this);
```

Cliquer sur le rectangle incrémente `count` ; le texte se recharge automatiquement via `.watch(count)`.

## Lectures suivantes

- [Core Concepts](core-concepts.md) — le modèle mental des nœuds, signaux et rendu.
- [UI Class](../ui/ui-class.md) — cycle de vie complet, annotations, raccourcis clavier.
- [Node Fundamentals](../nodes/node-fundamentals.md) — position, taille, effets, callbacks.