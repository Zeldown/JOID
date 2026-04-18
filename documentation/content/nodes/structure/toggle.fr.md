# ToggleNode

Toggle à deux valeurs backed par une paire `ToggleState<F, S>`. `ToggleNode` est **abstract** — vous la sous-classez pour dessiner les deux états visuels, pendant que la classe de base gère le click-to-flip et expose la valeur actuellement sélectionnée.

Contrairement à `CheckboxNode` (booléen seulement), `ToggleNode` permet aux deux côtés de porter des valeurs arbitraires. Usage typique : `ToggleState<String, String>` pour une paire de textes, `ToggleState<Enum, Enum>` pour deux modes.

## Utilisation

```java
public class MyToggle extends ToggleNode<String, String> {

    public MyToggle(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            isToggle() ? Color.decode("#3b82f6") : Color.decode("#374151"), 6F);
    }
}
```

Puis utilisez-la :

```java
new MyToggle(0, 0, 80, 32)
    .state("ON", "OFF")
    .toggle(false)
    .onChange((node, next) -> System.out.println("flipped, now toggle=" + next))
    .attach(parent);
```

## API

```java
T state(F toggle, S back)          // les deux valeurs portées par le toggle
T toggle(boolean value)             // true → la valeur est le côté "toggle" (F)

<V> V getValue()                   // retourne F si toggle, sinon S
boolean isToggle()
ToggleState<F, S> getState()

T onChange(NodeToggleChangeCallback<T, F, S> callback)
```

Les paramètres génériques `F` et `S` sont les types des deux côtés. Le callback de changement reçoit le nœud et la **nouvelle** valeur booléenne du toggle.

## Comportement

`mousePressed` est override : quand le curseur est sur le nœud, le toggle flippe et le callback se déclenche avec le nouveau booléen. La valeur associée se lit avec `getValue()` dans le callback.

## Exemple — draft / preview

```java
public class DraftPreviewToggle extends ToggleNode<String, String> { ... }

new DraftPreviewToggle(0, 0, 160, 36)
    .state("Draft", "Preview")
    .toggle(true)
    .onChange((node, next) -> {
        String current = node.getValue();
        System.out.println("switched to " + current);
    })
    .attach(parent);
```

## Voir aussi

- `CheckboxNode` — booléen seulement.
- `SwitchNode` — cycle à travers N états string.