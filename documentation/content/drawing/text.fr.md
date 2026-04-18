# Text

`DrawText` est la façade pour rendre du texte hors de l'arbre de nœuds. Elle accepte soit un `String` brut + `TextInfo`, soit un objet `Text` pré-construit pouvant porter plusieurs runs, alignement, règles d'overflow et modifiers. Accessible via `DrawUtils.TEXT` ou directement via `DrawText.getInstance()` — les deux retournent le même singleton. Chaque méthode de dessin retourne un `FontBounds` décrivant le rectangle pixel occupé par le texte.

## Dessiner des chaînes simples

### `drawText` (simple, aligné)

```java
FontBounds drawText(double x, double y,
                    String text, TextInfo info,
                    Align horizontalAlign, Align verticalAlign)
```

Forme la plus courte. Enveloppe la chaîne en interne dans un `Text` à un élément avant de dessiner.

```java
DrawUtils.TEXT.drawText(100, 100, "Hello", info, Align.START, Align.START);
```

### `drawText` (simple, dans une box)

```java
FontBounds drawText(double x, double y, double width, double height,
                    String text, TextInfo info,
                    Align horizontalAlign, Align verticalAlign,
                    TextOverflow overflow, TextMode mode)
```

Dessine à l'intérieur du rectangle `(x, y, width, height)` selon le `TextMode` et `TextOverflow` choisis. Voir les tableaux [modes](#modes-textmode) et [overflow](#overflow-textoverflow) plus bas.

```java
DrawUtils.TEXT.drawText(40, 40, 200, 60, "A very long subtitle", info,
    Align.CENTER, Align.CENTER, TextOverflow.ELLIPSIS, TextMode.OVERFLOW);
```

## Dessiner un `Text` pré-construit

### `drawText` (builder)

```java
FontBounds drawText(double x, double y, Text text)
FontBounds drawText(double x, double y, double width, double height, Text text, TextMode mode)
```

À utiliser quand vous avez déjà construit un `Text` avec plusieurs éléments, alignement ou modifiers.

```java
Text line = Text.create()
    .add(TextElement.create("HP: ", info))
    .add(TextElement.create(() -> String.valueOf(hp.get()), infoBold))
    .align(Align.CENTER, Align.CENTER);

DrawUtils.TEXT.drawText(cx, cy, line);
```

## Découpage de texte

### `getLines`

```java
List<String> getLines(double width, String text, TextInfo info)
List<Text>   getLines(double width, Text text)
```

Découpe un texte en lignes qui tiennent dans la largeur en pixels donnée. Respecte `\n`, `\r` et les marqueurs `<br>`. La première variante retourne des `String` ; la seconde préserve la structure element/info pour le texte multi-style.

```java
double y = 40;
for (final String line : DrawUtils.TEXT.getLines(300, longText, info)) {
    DrawUtils.TEXT.drawText(20, y, line, info, Align.START, Align.START);
    y += info.getHeight();
}
```

## Modes (`TextMode`)

| Mode | Comportement |
|---|---|
| `NORMAL` | Dessine le texte tel quel, aligné dans la box. Pas de wrap, pas d'overflow. |
| `OVERFLOW` | Si le texte dépasse `width`, tronque et ajoute le suffixe `TextOverflow` courant. |
| `SPLIT` | Wrap en plusieurs lignes pour tenir dans `width`. Pas de clipping vertical — le texte peut dépasser `height`. |
| `BOX` | Wrap comme `SPLIT`, mais saute toute ligne qui sortirait de `(y, y+height)`. |

## Overflow (`TextOverflow`)

| Valeur | Suffixe |
|---|---|
| `NONE` | *chaîne vide* |
| `ELLIPSIS` | `...` |
| `DOT` | `.` |
| `HYPHEN` | `-` |

Passez-le à l'appel de dessin pour le mode `OVERFLOW`, ou intégrez-le au `Text`.

```java
Text t = Text.create("A very long subtitle", info).overflow(TextOverflow.ELLIPSIS);
DrawUtils.TEXT.drawText(x, y, 200, 40, t, TextMode.OVERFLOW);
```

## Le builder `Text`

### `Text.create`

```java
Text create()
Text create(Object text, TextInfo info)
Text create(Supplier<Object> text, TextInfo info)
Text create(Object text, TextInfo info, Align horizontalAlign)
Text create(Object text, TextInfo info, Align horizontalAlign, Align verticalAlign)
Text create(Object text, TextInfo info, TextOverflow overflow)
Text create(Object text, TextInfo info, Align align, TextOverflow overflow)
Text create(Object text, TextInfo info, Align horizontal, Align vertical, TextOverflow overflow)
Text create(List<TextElement> elements)
Text create(TextElement... elements)
```

Les variantes `Supplier<Object>` lient une source dynamique — le texte ré-évalue à chaque draw sans reconstruire le `Text`.

### `text` / `info` (mutation)

```java
Text text(String text)
Text text(Supplier<String> text)
Text text(int index, String text)
Text text(int index, Supplier<String> text)
Text info(TextInfo info)
Text info(int index, TextInfo info)
```

Remplace le texte ou `TextInfo` de l'élément `0` par défaut, ou de l'élément indexé. Invalide le cache width/height.

### `add` / `addAll` / `remove` / `clear`

```java
Text add(TextElement element)
Text add(Text otherText)
Text addAll(List<TextElement> elements)
Text remove(TextElement element)
Text clear()
```

Construction incrémentale de la liste d'éléments. Chaque opération invalide le cache width/height.

### Configuration

```java
Text align(Align horizontal, Align vertical)
Text horizontalAlign(Align align)
Text verticalAlign(Align align)
Text overflow(TextOverflow overflow)
Text modifier(ITextModifier modifier)
```

### Copies

```java
Text copy()
Text copyProperties()
Text copyWithOverflow(TextOverflow overflow)
Text copyWithHorizontalAlign(Align align)
Text copyWithVerticalAlign(Align align)
Text copyWithModifier(ITextModifier modifier)
```

### Lecture

```java
String       getRawText()
String       getText()
String       getText(TextElement element)
double       getWidth()
double       getHeight()
double       dw(double value)       // width / value
double       dh(double value)       // height / value
double       aw(double value)       // width + value
double       ah(double value)       // height + value
FontBounds   getBounds()
boolean      isEmpty()
```

`getRawText` retourne la concaténation non modifiée ; `getText` applique le modifier attaché (s'il y en a un).

## `TextElement`

### `TextElement.create`

```java
TextElement create(Object text, TextInfo info)
TextElement create(Supplier<Object> text, TextInfo info)
TextElement create(int text, TextInfo info)
TextElement create(double text, TextInfo info)
TextElement create(float text, TextInfo info)
TextElement create(long text, TextInfo info)
TextElement create(char text, TextInfo info)
TextElement create(boolean text, TextInfo info)
```

Factories pour un run unique. Les primitifs sont convertis via `String.valueOf`.

### Setters fluides

```java
TextElement text(Object text)
TextElement text(Supplier<Object> text)
TextElement text(int text)
TextElement text(double text)
TextElement text(float text)
TextElement text(long text)
TextElement text(char text)
TextElement text(boolean text)
TextElement info(TextInfo info)
TextElement modifier(ITextModifier modifier)
```

### Copies

```java
TextElement copy()
TextElement copyWithText(Object text)
TextElement copyWithText(Supplier<Object> text)
TextElement copyWithInfo(TextInfo info)
TextElement copyWithModifier(ITextModifier modifier)
```

## Modifiers (`ITextModifier`)

Un modifier transforme la chaîne finale au moment du draw. Attachez-le avec `text.modifier(...)` ou `element.modifier(...)`. Intégrés :

| Modifier | Exemple |
|---|---|
| `TextUpperCaseModifier` | `HELLO` |
| `TextLowerCaseModifier` | `hello` |
| `TextCapitalizeModifier` | `Hello` |
| `TextWordCapitalizeModifier` | `Hello World` |
| `TextCamelCaseModifier` | `helloWorld` |
| `TextUpperCamelCaseModifier` | `HelloWorld` |
| `TextSnakeCaseModifier` | `hello_world` |

Les modifiers custom implémentent `ITextModifier.modify(String)` et retournent la chaîne transformée.

## Voir aussi

- `DrawUtils` — point d'entrée pour les quatre façades.
- `Custom Fonts` — comment produire `TextInfo` à partir d'un font provider.
- `TextNode` — wrapper nœud du même pipeline de texte.