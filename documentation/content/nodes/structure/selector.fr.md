# SelectorNode

Sélecteur d'options de type dropdown. `SelectorNode` est **abstract** — sous-classez-la pour rendre le fond des états replié et déplié. Chaque option est un `Node` enfant attaché au sélecteur ; cliquer un enfant non sélectionné le sélectionne, cliquer celui sélectionné replie la liste.

## Utilisation

```java
public class MySelector extends SelectorNode {

    public MySelector(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void drawBackground(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 6F);
    }
}
```

Attacher les options comme enfants :

```java
final MySelector picker = new MySelector(40, 40, 240, 36);
picker.direction(SelectorDirection.DOWN)
      .selected(optionRed)                // pré-sélectionne un enfant spécifique
      .onChange((sel, selectedNode) -> System.out.println("picked " + selectedNode));

optionRed.attach(picker);
optionGreen.attach(picker);
optionBlue.attach(picker);
picker.attach(parent);
```

Les enfants sont empilés : le sélectionné à `y = 0`, les autres en dessous (ou au-dessus avec `SelectorDirection.UP`) quand le sélecteur est `active`.

## API

```java
T direction(SelectorDirection direction)   // UP ou DOWN (défaut DOWN)
T active(boolean active)                   // ouvre / ferme la liste
T selected(Node selected)                  // force une sélection

Node getSelected()
boolean isActive()
SelectorDirection getDirection()
boolean isSelected(Node node)

T onChange(NodeSelectorChangeCallback<T> callback)
abstract void drawBackground(double mouseX, double mouseY)
```

`SelectorNode` n'a **pas** de setter `options(...)`, `value(String)`, `placeholder`, `backgroundColor`, `hoveredColor`, `textInfo`, `maxVisibleOptions` ni `searchable` — ces fonctionnalités n'existent pas sur cette classe. L'apparence de chaque option est du ressort du `Node` enfant.

## `SelectorDirection`

```java
SelectorDirection.UP     // les options s'ouvrent vers le haut
SelectorDirection.DOWN   // vers le bas (défaut)
```

Le helper interne `isDown()` retourne `true` pour `DOWN`.

## Voir aussi

- `ToggleNode` / `SwitchNode` — alternatives plus simples quand les options sont peu nombreuses.