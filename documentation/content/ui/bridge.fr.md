# Bridge

Le bridge est la colle entre JOID et votre application hôte. Il détient la liste des UIs actives, dispatche les événements d'entrée, et pilote le rendu.

## Backends et `DemoWindow`

Chaque branche de backend (`impl/lwjgl-2`, `impl/lwjgl-3`, `impl/vulkan`) fournit une `DemoWindow` — une fenêtre prête à l'emploi qui :

- Enregistre les bridges de fenêtre, de rendu et d'audio de son backend (voir [Backends](backends.md)).
- Écoute souris et clavier.
- Boucle `update` → `render` → présentation.
- S'enregistre comme `UIBridge` quand vous appelez `BridgeHandler.register(window)`.

```java
final DemoWindow window = new DemoWindow();
BridgeHandler.register(window);
JOID.inst().setDevMode(true).setDemoMode(true).load();
window.run();
```

Lancez-la depuis une branche de backend avec `./gradlew runDemo`. Sur `impl/lwjgl-2`, appelez `LWJGL2Backend.register()` avant de créer la fenêtre.

## Écrire votre propre bridge

Pour embarquer JOID dans un hôte custom (jeu, outil avec une boucle principale différente), étendez `UIBridge` et implémentez `IUIBridge` :

```java
public class MyBridge extends UIBridge {

    @Override
    public void drawHover(@NonNull UI ui, @NonNull List<@NonNull String> lines, double mouseX, double mouseY) {
        // Rendre les infobulles avec le système de police de l'hôte
    }

    @Override
    public void open(@NonNull UI ui) {
        // Logique d'ouverture custom — typiquement fermer les autres puis add(ui)
    }

    @Override
    public void close(@NonNull UI ui) {
        remove(ui);
    }

    @Override
    public void add(@NonNull UI ui) {
        getUiList().add(ui);
        ui.load(/* width */ 1920, /* height */ 1080);
    }

    @Override
    public void remove(@NonNull UI ui) {
        getUiList().remove(ui);
    }

    @Override
    public boolean isOnTop(@NonNull UI ui) {
        return getUiList().ordered().getLast() == ui && ui.getData().active() && ui.getData().visible();
    }

    @Override
    public boolean canHandle(@NonNull Class<? extends UI> ui) { return true; }
    @Override
    public boolean canHandle(@NonNull UI ui) { return true; }

    @Override
    public int getIndex() { return 0; }

    @Override
    public @NonNull IUIBridge getInstance() { return this; }
}
```

Enregistrez-le une fois au démarrage :

```java
BridgeHandler.register(new MyBridge());
```

## Câbler les entrées

Votre boucle principale transmet les événements de votre bibliothèque de fenêtrage au bridge. Les boutons de souris passent par `ClickType.from(button)` et les touches par l'enum neutre `Key` ; la position de la souris est lue depuis le bridge de fenêtre, en pixels depuis le coin supérieur gauche.

```java
bridge.mousePressed(ClickType.from(button));
bridge.mouseReleased(ClickType.from(button));
bridge.mouseDragged(clickType, System.currentTimeMillis() - pressTime);
bridge.mouseScroll(wheelDelta);
bridge.keyTyped(character, key);

bridge.update();
bridge.draw();
```

Chaque `DemoWindow` de backend contient une boucle complète pour sa bibliothèque de fenêtrage — polling `Mouse` / `Keyboard` sur LWJGL 2, callbacks GLFW sur LWJGL 3 et Vulkan.

`UIBridge` gère déjà le tracking du drag et ESC-to-close ; il suffit de lui fournir les événements.

## Plusieurs bridges

Une vraie app a souvent plusieurs bridges — par exemple un pour les UIs in-world, un pour le menu principal. `BridgeHandler` route chaque `UI` vers le bridge approprié selon `canHandle(Class<? extends UI>)`.

```java
BridgeHandler.register(new MainMenuBridge());
BridgeHandler.register(new HUDBridge());
BridgeHandler.register(new WorldUIBridge());

// JOID.open() choisit automatiquement le bon bridge selon la classe d'UI.
JOID.open(new SettingsUI());  // → MainMenuBridge (car il canHandle SettingsUI)
```

Utilisez l'annotation `@UIBridge` sur vos classes d'UI pour indiquer le routage, ou override la logique `canHandle` dans chaque bridge.

## Ordre des bridges

Les bridges sont itérés dans l'ordre d'enregistrement. Le premier dont `canHandle(ui)` retourne `true` gagne. Gardez le routage déterministe.

## Bonnes pratiques

- **Un bridge par contexte de rendu.** N'essayez pas de multiplexer plusieurs contextes de rendu dans un seul bridge.
- **Gardez `drawHover` rapide.** Il tourne après chaque render de nœud. Utilisez le système de police cache de l'hôte.
- **Ne manipulez jamais `uiList` directement.** Passez par `open`/`close`/`add`/`remove` pour garder l'état interne cohérent.
- **Enregistrez les bridges avant d'ouvrir une UI.** `JOID.open()` échoue silencieusement si aucun bridge ne gère la classe d'UI.

## Voir aussi

- [UI Class](ui-class.md) — comment les UIs s'accrochent au bridge.
- [Backends](backends.md) — les bridges de fenêtre, de rendu et d'audio.
- [Transitions](transitions.md) — animations in/out pilotées par `open` / `close`.