# Callbacks

Chaque comportement interactif sur un `Node` est un callback. Attachez-les via des setters fluides ; gérez-les avec une lambda qui correspond à la signature `apply(...)` du callback.

## Cycle de vie (state)

```java
node.onInit((n) -> { });                                                          // NodeInitCallback
node.onRender((n, mouseX, mouseY, partialTicks) -> { });                          // NodeRenderCallback
node.onDraw((n, mouseX, mouseY, partialTicks) -> { });                            // NodeDrawCallback
node.onUpdate((n) -> { });                                                        // NodeUpdateCallback
node.onReload((n) -> { });                                                        // NodeReloadCallback
node.onAppend((n, child) -> { });                                                 // NodeAppendCallback
node.onDetach((n) -> { });                                                        // NodeDetachCallback
node.onMount((n) -> { });                                                         // NodeMountCallback
```

`onInit` se déclenche la première fois que le nœud est chargé dans une UI. `onMount` à la première frame rendue. `onDetach` quand le nœud est retiré de son parent (via `clearChildren()` ou fermeture d'UI).

## Souris

```java
node.onMousePressed((n, mouseX, mouseY, clickType) -> { });                       // NodeMousePressedCallback
node.onMouseReleased((n, mouseX, mouseY, clickType) -> { });                      // NodeMouseReleasedCallback
node.onMouseDragged((n, mouseX, mouseY, clickType, deltaTime) -> { });            // NodeMouseDraggedCallback
node.onMouseScroll((n, mouseX, mouseY, value) -> { });                            // NodeMouseScrollCallback
node.onClick((n, mouseX, mouseY, clickType) -> { });                              // alias de onMousePressed
```

## Clavier

```java
node.onKeyPressed((n, character, keyCode) -> { });                                // NodeKeyPressedCallback
```

## Scroll (overflow = SCROLL)

```java
node.onScrollUpdate((n, value) -> { });                                           // NodeScrollUpdateCallback
node.onScrollEnd((n, scrollX, scrollY) -> { });                                   // NodeScrollEndCallback
```

`onScrollUpdate` se déclenche à chaque tick de scroll avec la valeur brute de la molette ; `onScrollEnd` une fois l'animation de scroll stabilisée, avec les pourcentages finaux `(scrollX, scrollY)`.

## Drag

```java
node.onDrag((n) -> { });                                                          // NodeDragCallback
node.onSnap((n, snapNode) -> { });                                                // NodeSnapCallback
```

`onDrag` à chaque frame pendant le drag. `onSnap` quand le nœud draggé est relâché assez près d'une cible de snap enregistrée.

## Hover

```java
node.onHoverStart((n, mouseX, mouseY) -> { });                                    // NodeHoverStartCallback
node.onHoverEnd((n, mouseX, mouseY) -> { });                                      // NodeHoverEndCallback
node.onHover((n, mouseX, mouseY) -> { });                                         // NodeHoverCallback
```

`onHoverStart` se déclenche à la frame où la souris entre sur le nœud ; `onHoverEnd` à la frame où elle en sort ; `onHover` à chaque frame tant que la souris est dessus. Les trois sont câblés sur la même state machine qui pilote `hoverValue` — ils restent donc synchrones avec le fade de hover.

## Animation

```java
node.onAnimation((n, animator, value) -> { });                                    // NodeAnimationCallback
```

Se déclenche quand un tween animator attaché au nœud publie une nouvelle valeur. Vous recevez le `TweenAnimator` courant et son `float`.

## Signals

```java
node.onWatch((n, signal, properties) -> { });                                     // NodeWatchCallback
```

Se déclenche quand un signal watché tire. `properties` est le vararg `WatchProperty[]` passé à `.watch(...)`.

## Le paramètre `context` (phases PRE/POST)

Les lambdas ci-dessus atterrissent en phase **POST** de chaque callback. Pour exécuter du code avant le comportement par défaut, implémentez directement l'interface — les méthodes `pre` / `post` reçoivent toutes deux un `InternalContext` :

```java
final NodeMousePressedCallback<RectNode> myHandler = new NodeMousePressedCallback<RectNode>() {

    @Override
    public void pre(RectNode node, InternalContext context, double mouseX, double mouseY, ClickType clickType) {
        if (!shouldReact()) context.cancel();
    }

    @Override
    public void apply(RectNode node, double mouseX, double mouseY, ClickType clickType) {
        // Phase POST — skippée si le context a été annulé en pre()
    }
};
```

`InternalContext` expose `cancel()`, `cancel(Runnable)`, et `isCancelled()`. Le comportement par défaut (par ex. `CheckboxNode.checked` qui flip) tourne entre `pre` et `post`.

## `ClickType`

```java
ClickType.LEFT
ClickType.RIGHT
ClickType.MIDDLE
ClickType.BACK
ClickType.FORWARD
ClickType.OTHER
```

`ClickType.from(int button)` mappe un numéro de bouton souris LWJGL vers une de ces valeurs (`0 → LEFT`, `1 → RIGHT`, `2 → MIDDLE`, `3 → BACK`, `4 → FORWARD`, autre → `OTHER`).

## Exemples

### Double-click

```java
final long[] lastClick = { 0L };

node.onClick((n, mx, my, ct) -> {
    final long now = System.currentTimeMillis();
    if (now - lastClick[0] < 300L) { /* double click */ }
    lastClick[0] = now;
});
```

### Menu contextuel (clic droit)

```java
node.onClick((n, mx, my, ct) -> {
    if (ct == ClickType.RIGHT) openContextMenu(mx, my);
});
```

### Libérer des ressources au détachement

```java
class MyAssetNode extends Node {
    private final Thread worker;

    public MyAssetNode() {
        this.worker = new Thread(this::doWork);
        this.worker.start();
        onDetach(n -> this.worker.interrupt());
    }
}
```

### Logger la position de scroll

```java
FlexNode.vertical(0, 0, 400)
    .overflow(OverflowProperty.SCROLL)
    .body(list -> { /* contenu */ })
    .onScrollEnd((n, sx, sy) -> System.out.println("Scrolled to " + sx + ", " + sy))
    .attach(parent);
```

## Bonnes pratiques

- **Gardez les callbacks rapides.** Ils tournent sur le thread de rendu ; un traitement lourd bloque la frame.
- **Utilisez `onDetach` pour le cleanup.** Threads, sockets, ressources natives — libérez ici sous peine de leak.
- **Ne mutez pas l'arbre depuis `onUpdate`.** Utilisez des signaux — ajouter / retirer des nœuds pendant l'itération est unsafe.
- **Préférez `onClick` à `onMousePressed`** sauf si vous avez besoin de la sémantique press + release.

## Voir aussi

- [Node Fundamentals](../nodes/node-fundamentals.md) — liste complète des callbacks.
- [Hover](hover.md) — requêtes d'état de survol.
- [Drag & Drop](drag-drop.md) — callbacks de drag et cibles de snap.