# CheckboxNode

Checkbox à deux états (cochée / non cochée). `CheckboxNode` est **abstract** — sous-classez-la pour fournir le rendu, la classe de base gérant la logique click-to-toggle et le callback de changement.

## Utilisation

```java
public class MyCheckbox extends CheckboxNode {

    public MyCheckbox(final double x, final double y, final double size) {
        super(x, y, size, size);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 4F);
        if (isChecked()) {
            DrawUtils.SHAPE.drawRoundedRect(getX() + 4, getY() + 4,
                getWidth() - 8, getHeight() - 8, Color.decode("#3b82f6"), 2F);
        }
    }
}
```

Puis utilisez-la :

```java
new MyCheckbox(0, 0, 24)
    .checked(true)
    .onChange((cb, next) -> System.out.println("checked: " + next))
    .attach(parent);
```

## API

```java
T checked(boolean value)
T onChange(NodeCheckboxChangeCallback<T> callback)

boolean isChecked()
```

Le setter `checked(boolean)` et le callback `onChange` constituent toute la surface publique. Tout le visuel est à la charge du `draw()` de votre sous-classe.

## Comportement

`mousePressed` est override sur la classe de base : quand le curseur est sur le nœud et que l'événement n'est pas annulé, `checked` est inversé et le callback de changement est déclenché avec la **nouvelle** valeur.

## Exemple — ligne de préférences

```java
final BooleanSignal notifications = new BooleanSignal(true);

FlexNode.horizontal(0, 0, 32).margin(12).body(row -> {
    new MyCheckbox(0, 0, 24)
        .checked(notifications.getOrDefault())
        .onChange((cb, val) -> notifications.set(val))
        .attach(row);

    TextNode.create(0, 0)
        .text(Text.create("Enable notifications", info))
        .anchor(Align.START, Align.CENTER)
        .attach(row);
}).attach(parent);
```

## Voir aussi

- `ToggleNode` — toggle entre deux valeurs arbitraires, pas des booléens.
- `SwitchNode` — switch multi-états.