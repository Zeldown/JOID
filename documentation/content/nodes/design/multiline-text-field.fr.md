# MultilineTextFieldNode

Champ de saisie multi-ligne. Wrap le texte long à la largeur du nœud, supporte le scroll via molette, et propose le presse-papier, la sélection et la navigation au complet comme `TextFieldNode`.

## Créer

```java
MultilineTextFieldNode.create(x, y, width, height)
```

Une seule factory — `height` est toujours explicite car le wrap dépend de lui.

## API

```java
T text(String text)
T placeholder(String placeholder)
T info(TextInfo textInfo)

T focused(boolean focused)
T filter(BiFunction<String, String, String> filter)
T maxTextLength(int maxTextLength)                         // -1 = illimité

T margin(double margin)
T margin(double margin, double cursorMargin)
T marginTop(double margin)
T marginBottom(double margin)
T marginLeft(double margin)
T marginRight(double margin)
T cursorMargin(double cursorMargin)
T cursorPosition(int cursorPos)
```

Défauts : `margin = 2` sur tous les côtés, `cursorMargin = -1` (interprété comme `lineHeight * 2` au premier draw).

Il n'y a **pas** de setter `lineHeight`, `maxLines`, alignment, `cursorColor` ni `selectionColor`. La hauteur de ligne vaut `info.getHeight()`, le texte est toujours aligné à gauche, et le curseur comme la sélection utilisent des couleurs fixes.

## Callbacks

```java
T onChange(NodeTextFieldChangeCallback<T> callback)       // (node, oldText, newText)
T onFocus(NodeTextFieldFocusCallback<T> callback)         // (node)
```

Il n'y a **pas** de `onEnter` sur `MultilineTextFieldNode` — presser Enter insère un retour à la ligne (`Enter` comme `Numpad Enter`).

## Clavier & souris

- `←` / `→` — déplace le curseur sur la ligne courante.
- `↑` / `↓` — déplace le curseur à la ligne visuelle précédente / suivante, en gardant l'offset horizontal.
- `Home` / `End` — curseur à 0 / fin du texte.
- `Backspace` / `Delete` — supprime caractère ou sélection.
- `Shift + ← / → / ↑ / ↓` — étend la sélection.
- `Ctrl + A` — tout sélectionner.
- `Ctrl + C` / `Ctrl + V` / `Ctrl + X` — presse-papier.
- `Enter` / `Numpad Enter` — insère un retour à la ligne.
- `Esc` — unfocus.
- Molette — scroll vertical d'une ligne par tick.

## Exemple — éditeur de notes

```java
final StringSignal note = new StringSignal("");

RectNode.create(40, 40, 600, 400)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(8F))
    .body(wrapper -> {
        MultilineTextFieldNode.create(12, 12, 576, 376)
            .info(TextInfo.create(myFont, 14, Color.WHITE))
            .placeholder("Start typing…")
            .maxTextLength(10_000)
            .onChange((field, oldText, newText) -> note.set(newText))
            .attach(wrapper);
    })
    .attach(parent);
```

## Voir aussi

- [TextFieldNode](text-field.md) — variante mono-ligne.