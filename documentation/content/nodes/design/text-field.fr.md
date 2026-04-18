# TextFieldNode

Champ de saisie mono-ligne. Le nœud possède son propre texte, curseur et état de sélection, et gère la navigation clavier, le presse-papier et les clics souris. Le fond et la bordure sont dessinés par le container, pas par le champ lui-même.

## Créer

```java
TextFieldNode.create(x, y, width)                 // height auto-calculé depuis info
TextFieldNode.create(x, y, width, height)         // height explicite
```

Les deux factories retournent un `TextFieldNode`. Quand `height` vaut `0`, le nœud se redimensionne à `info.getHeight() + marginVertical * 2` au premier draw.

## API

```java
T text(String text)
T placeholder(String placeholder)
T info(TextInfo textInfo)

T align(Align horizontal, Align vertical)
T horizontalAlign(Align align)
T verticalAlign(Align align)

T focused(boolean focused)
T filter(BiFunction<String, String, String> filter)      // (oldText, newText) -> newText
T maxTextLength(int maxTextLength)                         // -1 = illimité

T margin(double margin)
T margin(double margin, double cursorMargin)
T marginHorizontal(double margin)
T marginVertical(double margin)
T marginLeft(double margin)
T marginRight(double margin)
T marginTop(double margin)
T marginBottom(double margin)
T cursorMargin(double cursorMargin)
T cursorPosition(int cursorPos)
```

Défauts : `marginHorizontal = 2`, `marginVertical = 10`, `cursorMargin = 15`, `horizontalAlignment = START`, `verticalAlignment = CENTER`.

Il n'y a **pas** de setter `cursorColor`, `cursorWidth`, `selectionColor` ou `password` — le curseur est dessiné avec la couleur courante de `info` et une alpha pulsée en sinus, la couleur de sélection est codée en dur à `(50, 152, 253, 100)`.

## Callbacks

```java
T onChange(NodeTextFieldChangeCallback<T> callback)       // (node, oldText, newText)
T onFocus(NodeTextFieldFocusCallback<T> callback)         // (node)
T onEnter(NodeTextFieldEnterCallback<T> callback)         // (node, text)
```

`onEnter` se déclenche sur `ENTER`, `NUMPAD_ENTER` et `ESC` — chacune de ces touches unfocus aussi le champ.

## Raccourcis clavier

Gérés dans `keyPressed` :

- `←` / `→` — déplacer le curseur (maintien → auto-repeat).
- `Home` / `End` — début / fin de ligne.
- `Backspace` / `Delete` — supprimer caractère ou sélection.
- `Shift + ←/→` — étendre la sélection.
- `Ctrl + A` — tout sélectionner.
- `Ctrl + C` / `Ctrl + V` / `Ctrl + X` — presse-papier.
- `Enter` / `Numpad Enter` / `Esc` — unfocus et déclenchent `onEnter`.

## Exemple — champ email

```java
final StringSignal email = new StringSignal("");

TextFieldNode.create(40, 40, 400, 40)
    .info(TextInfo.create(myFont, 16, Color.WHITE))
    .placeholder("email@example.com")
    .onChange((field, oldText, newText) -> email.set(newText))
    .attach(parent);
```

Enveloppez dans un `RectNode` pour fond et bordure :

```java
RectNode.create(40, 40, 400, 40)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F))
    .body(wrapper -> {
        TextFieldNode.create(8, 0, 384)
            .info(TextInfo.create(myFont, 16, Color.WHITE))
            .placeholder("email@example.com")
            .attach(wrapper);
    })
    .attach(parent);
```

## `IntegerFieldNode`

Sous-classe de `TextFieldNode` qui garde son contenu comme entier dans une range `[min, max]` :

```java
IntegerFieldNode.create(x, y, width)
IntegerFieldNode.create(x, y, width, height)

T min(int minValue)
T max(int maxValue)
T range(int minValue, int maxValue)
T value(int value)
int getValue()
```

La sous-classe installe un `filter` qui strip les caractères non-numériques et clamp la valeur parsée à `[min, max]`. `getValue()` retourne le parse entier du texte courant.

## Voir aussi

- [MultilineTextFieldNode](multiline-text-field.md) — variante multi-ligne.
- [Signals](../../state/signals.md) — lier la valeur du champ à un state.