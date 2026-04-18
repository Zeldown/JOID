# SwitchNode

Switch multi-états qui cycle à travers une liste d'états nommés. `SwitchNode` est **abstract** — vous la sous-classez pour fournir le rendu. La classe de base possède la liste d'états (`ListSignal<String>`), l'index courant (`IntegerSignal`), et re-rend le nœud quand l'un ou l'autre change.

## Utilisation

```java
public class MySwitch extends SwitchNode {

    public MySwitch(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 8F);
        DrawUtils.TEXT.drawText(getX() + getWidth() / 2, getY() + getHeight() / 2,
            getState(), info, Align.CENTER, Align.CENTER);
    }
}
```

Puis utilisez-la :

```java
new MySwitch(0, 0, 200, 40)
    .state("low", "medium", "high", "ultra")
    .index("medium")
    .onChange((node, next) -> System.out.println("switched to " + next))
    .attach(parent);
```

## API

```java
T state(String... states)
T state(List<String> states, String active)
T state(List<String> states, int index)

T index(String state)              // sauter à un état nommé
T index(int index)                 // sauter par index

String getState()                  // état actif courant
ListSignal<String> getStateList()
IntegerSignal getStateIndex()

T onChange(NodeSwitchChangeCallback<T> callback)
```

Toutes les factories `state(...)` réinitialisent l'index courant. Les deux variantes `index(...)` déclenchent le callback de changement via `executeCallback`.

## Comportement

Le nœud watch en interne `stateList` et `stateIndex` avec `WatchProperty.CLEAR_CHILDREN` + `WatchProperty.RELOAD`, donc tout changement de liste ou d'index reconstruit les enfants et relance `init`. Implémentez le cycling au click dans votre sous-classe si vous voulez que le switch avance à chaque click.

## Exemple — preset graphique

```java
public class GraphicsSwitch extends MySwitch {
    @Override
    public void mousePressed(double mx, double my, ClickType ct, InternalContext ctx) {
        if (!isHovered(mx, my)) return;
        ctx.cancel(() -> {
            int next = (getStateIndex().getOrDefault() + 1) % getStateList().size();
            index(next);
        });
    }
}
```

## Voir aussi

- `ToggleNode` — toggle à deux valeurs.
- `CheckboxNode` — booléen.
- `SelectorNode` — dropdown pour de longues listes d'options.