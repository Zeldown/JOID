# Bridge

Le bridge est la colle entre JOID et votre application hôte. Il détient la liste des UIs actives, dispatche les événements d'entrée, et pilote le rendu.

## Backends et `DemoWindow`

Chaque module de backend (`lwjgl2`, `lwjgl3`, `vulkan`) fournit une `DemoWindow` — une fenêtre prête à l'emploi qui :

- Enregistre les bridges de fenêtre, de rendu et d'audio de son backend (voir [Backends](backends.md)).
- Écoute souris et clavier.
- Boucle `update` → `render` → présentation.
- S'enregistre comme `UIBridge` quand vous appelez `BridgeHandler.UI.register(window)`.

```java
final DemoWindow window = new DemoWindow();
BridgeHandler.UI.register(window);
JOID.inst().setDevMode(true).setDemoMode(true).load();
window.run();
```

Lancez-la avec `./gradlew :lwjgl3:runDemo` (ou `:lwjgl2`, `:vulkan`). Avec LWJGL 2, appelez `dev.joid.impl.lwjgl2.Backend.register()` avant de créer la fenêtre.

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
BridgeHandler.UI.register(new MyBridge());
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

Chaque `DemoWindow` de backend contient une boucle complète pour sa bibliothèque de fenêtrage — polling `Mouse` / `Keyboard` sur LWJGL 2, callbacks GLFW dans la `DemoWindow` du module `glfw`, partagée par LWJGL 3 et Vulkan.

`UIBridge` distribue déjà chaque événement aux UIs, de celle du dessus vers le bas, et ferme une UI fermable sur ESC ; il suffit de lui fournir les événements. Le temps de drag donné à `mouseDragged` est transmis tel quel : c'est votre boucle qui le mesure.

## Échelle d'interface

Un hôte qui laisse ses utilisateurs choisir la taille de l'interface — un GUI scale de jeu, un réglage d'accessibilité — l'indique à JOID via `getInterfaceScale(UI)`. Le bridge décide des UIs concernées et renvoie un facteur normalisé, `1` valant la pleine taille :

```java
@Override
public double getInterfaceScale(final @NonNull UI ui) {
    return ui instanceof HudUI ? this.settings.getGuiScale() / (double) this.settings.getMaxGuiScale() : 1D;
}
```

L'UI fait le reste : elle se dessine autour de son ancre à cette échelle, convertit la souris, place les infobulles, dimensionne les effets, et met à jour ses signaux `scaledWidth` / `scaledHeight` quand la valeur change. Le bridge continue de transmettre la taille brute de la fenêtre et la position brute de la souris, en pixels — ne les remettez jamais à l'échelle vous-même.

## Plusieurs bridges

Une vraie app a souvent plusieurs bridges — par exemple un pour les UIs in-world, un pour le menu principal. `BridgeHandler.UI` route chaque `UI` vers le bridge approprié selon `canHandle(UI)` ; `canHandle(Class<? extends UI>)` répond aux recherches par classe, `JOID.getUI` et `JOID.isOpen(Class)`. Quand plusieurs bridges peuvent gérer la même `UI`, celui qui a le plus grand `getIndex()` l'emporte, puis le dernier enregistré.

```java
BridgeHandler.UI.register(new MainMenuBridge());
BridgeHandler.UI.register(new HUDBridge());
BridgeHandler.UI.register(new WorldUIBridge());

// JOID.open() choisit automatiquement le bon bridge selon la classe d'UI.
JOID.open(new SettingsUI());  // → MainMenuBridge (car il canHandle SettingsUI)
```

Le routage est décidé par les méthodes `canHandle` de chaque bridge.

## Ordre des bridges

Les bridges sont essayés du plus grand `getIndex()` au plus petit, puis du dernier enregistré au premier. Le premier dont `canHandle(ui)` retourne `true` gagne. Gardez le routage déterministe.

## Bonnes pratiques

- **Un bridge par contexte de rendu.** N'essayez pas de multiplexer plusieurs contextes de rendu dans un seul bridge.
- **Gardez `drawHover` rapide.** Il tourne après chaque render de nœud. Utilisez le système de police cache de l'hôte.
- **Ne manipulez jamais `uiList` directement.** Passez par `open`/`close`/`add`/`remove` pour garder l'état interne cohérent.
- **Enregistrez les bridges avant d'ouvrir une UI.** `JOID.open()` échoue silencieusement si aucun bridge ne gère la classe d'UI.

## Voir aussi

- [UI Class](ui-class.md) — comment les UIs s'accrochent au bridge.
- [Backends](backends.md) — les bridges de fenêtre, de rendu et d'audio.
- [Transitions](transitions.md) — animations in/out pilotées par `open` / `close`.