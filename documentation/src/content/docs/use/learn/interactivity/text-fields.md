---
title: "Text fields"
description: "Let the user type a text, a number or several lines, shape what they type, move the focus, and write a field for a value of your own."
genre: learn
sidebar:
  order: 7
---

A text field turns what the user types into a value: a name, a number, a color.
It is a [control](/use/learn/interactivity/controls/) with a text: everything that page says about signals, `beforeChange` and `onChange` holds here.
This page adds what only a field has: the focus, the cursor, and the rules on the text.

**You will learn**
- How to add a text field and read what the user types
- How to move the focus
- How to shape what the user can type
- How to ask for a number or for several lines
- How to write a field for a value of your own

## Add a text field

Unlike the other controls, a field draws its own text and cursor: it has a `create(...)` factory, and its `background(...)` is optional.
A field takes its font with `info(...)`, and binds its text to a [[StringSignal]]:

```java file="com/example/interactivity/fields/NameFieldUI.java" region="signal"
```

```java file="com/example/interactivity/fields/NameFieldUI.java" region="field"
```

<figure>

![A dark grey text field with the placeholder "Your name", and the texts "Hello, " and "Submits: 0"; the pointer clicks the field, the user types "Ada" and the greeting follows each key, then Enter makes "Submits: 1"](/captures/interactivity-field.webp)

<figcaption>
The signal follows each key, so "Hello, Ada" changes while the user types; Enter submits once.
</figcaption>
</figure>

[[TextFieldNode]] needs a [[TextInfo]] before it is attached; without one, the attach throws an `IllegalStateException`.
With the factory without a height, the field takes the height of its font plus its margins, set by [[FieldNode#margin]], or one side at a time with `marginTop(...)`, `marginBottom(...)`, `marginLeft(...)` and `marginRight(...)`.
The fields of this page take a dark grey `background(...)`, and any node can be the background; their `placeholderColor(...)` is a grey that reads better on it than the default, the text color at half its opacity.

- [[FieldNode#onChange]] runs on each edit with a [[NodeFieldChangeEvent.Post]]: `getText()` is the text, `getValue()` the value, `isValid()` whether the text is a valid value.
- [[LineFieldNode#onSubmit]] runs when the user presses Enter: the field blurs first, and the listener runs even when a `beforeBlur` listener keeps the focus.
- `placeholder(...)` shows a text while the field is empty and has no focus, in the color of [[FieldNode#placeholderColor]].
- [[FieldNode#cursorColor]] and [[FieldNode#selectionColor]] color the cursor and the selection; the cursor blinks.
- [[LineFieldNode#alignX]] places the text at the start, the center or the end of the field.

## Move the focus

A field takes the keyboard only while it has the focus: it then consumes every key and every character ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).

- A press on the field focuses it and puts the cursor under the pointer.
- A press anywhere else blurs the field, and so do Enter in a one-line field, Escape, and the detach of the field.
- Escape keeps the typed text ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard) gives what a second Escape does).
- A press on a field blurs the field that had the focus among the UIs of its [UI bridge](/use/learn/running/embedding/) (the UIs of one window), which runs its `onBlur`, then focuses the new one: the presses of the user keep one focused field at a time.

[[FieldNode#focus]] and [[FieldNode#blur]] act from your code, and `focused(...)` binds the focus to a signal:

```java file="com/example/interactivity/fields/FocusFieldUI.java" region="focus"
```

<figure>

![A dark grey "Search" field, a dark grey "Search" button and the text "Not focused"; the pointer clicks the button, the field gets its blue focus border and the cursor, and the text reads "Focused"; a press elsewhere on the card leaves the focus in the empty field; once an "a" is typed, Escape, shown in a badge, blurs the field and the text reads "Not focused" again](/captures/interactivity-focus.webp)

<figcaption>
The button focuses the field, and the signal follows the focus.
</figcaption>
</figure>

`beforeFocus` and `beforeBlur` can refuse the focus or keep it: here, the field keeps the focus while it is empty.
`onFocus` and `onBlur` run after the change.
Unlike a press, `focus()` and `focused(...)` blur no other field: call `blur()` on the field that had the focus first.

The user edits the text with the keys of every text editor:

| Keys | Action |
|---|---|
| Left, Right | Move the cursor; with Ctrl (Alt on macOS), by word |
| Home, End | Go to the start or the end of the text, in a field of several lines too |
| Shift with a move, or a drag of the pointer | Select |
| A double press, a triple press | Select a word, a line |
| Backspace, Delete | Erase a character; with Ctrl (Alt on macOS), a word |
| Ctrl + A, C, X, V (Cmd on macOS) | Select all, copy, cut, paste |

From your code, [[FieldNode#cursorPosition]] and [[FieldNode#selection]] give the cursor and the selection, a plain value or a signal bound both ways; [[FieldNode#moveCursor]] and [[FieldNode#select]] act like the user, with `beforeCursorMove`, `onCursorMove`, `beforeSelect` and `onSelect`.
Losing the focus clears the selection.

## Shape what the user types

```java file="com/example/interactivity/fields/RulesFieldUI.java" region="rules"
```

<figure>

![A dark grey field with the placeholder "Code" and the text "Signal: "; the user types a space, a, b and 1, each key showing in a badge at the bottom right: the digit never appears, and Enter turns the text into "AB" in the field and in the signal; a click in the field, Backspace twice and Enter empty both again](/captures/interactivity-field-rules.webp)

<figcaption>
The user types ` ab1` then Enter: the digit is refused, and the field rewrites ` ab` as `AB` when it loses the focus.
</figcaption>
</figure>

- [[FieldNode#accept]] refuses an edit whose text fails the test: the digit never appears.
- [[FieldNode#maxTextLength]] cuts the text to a number of characters, also for a paste.
- [[FieldNode#normalize]] turns the text into the value: the field shows the normalized text when it loses the focus, and at once for a text given by a setter or a signal.
- [[FieldNode#allowEmpty]] `(true)` lets the field be empty, with the value `null`; otherwise an empty text field holds `""`, and a field that reads no value from the empty text, such as a number field, takes back its last value when it loses the focus.
- [[FieldNode#markup]] `(true)` shows the [markup](/use/learn/describing-the-ui/text-markup-and-effects/) of the text instead of its tags.

The signal follows the text as the user types it, and receives the normalized text with the rewrite.
A rule that changes keeps the text asked for, as a control keeps its pending value ([Keep a value that the bounds refuse](/use/learn/interactivity/controls/#keep-a-value-that-the-bounds-refuse)).

## Ask for a number

```java file="com/example/interactivity/fields/NumberFieldUI.java" region="number"
```

<figure>

![A dark grey number field that reads 4, and the text "Players: 4"; the pointer clicks the field, two presses of the Up arrow bring both to 6, and two presses of the Down arrow bring them back to 4](/captures/interactivity-field-number.webp)

<figcaption>
Two presses of the Up arrow, then two of the Down arrow: the value goes from 4 to 6 and back, and the signal follows.
</figcaption>
</figure>

[[IntegerFieldNode]] holds an `Integer` between [[IntegerFieldNode#min]] and [[IntegerFieldNode#max]]:

- the user types digits, and a minus sign when `min` is below `0`;
- Up and Down, or the wheel over the focused field, add or remove [[IntegerFieldNode#step]];
- a typed value out of the range shows as its bound when the field loses the focus;
- `value(...)` gives the value or binds it to a signal, and `change(value)` acts like the user.

An empty number field takes back `0` before any value, as `allowEmpty` above says; with `allowEmpty(true)`, a bound signal receives `null`.

## Let the user type several lines

```java file="com/example/interactivity/fields/NotesFieldUI.java" region="notes"
```

[[MultilineTextFieldNode]] wraps its text in its width.
Enter starts a new line, Up and Down move the cursor between lines, and the wheel scrolls a text taller than the field.

<figure>

![A dark grey field of several lines with the placeholder "Notes"; the pointer clicks it and the user types a sentence, each key showing in a badge at the bottom right, that wraps onto a second line; Enter starts a third line, "Second line", Up moves the cursor to the line above, then Ctrl + A and Backspace empty the field and Escape leaves it](/captures/interactivity-notes.webp)

<figcaption>
The text wraps in the width of the field by itself; only Enter starts a line of its own.
</figcaption>
</figure>

## Write a field for your own value

A field for another type extends [[LineFieldNode]] and says how a text becomes a value:

```java file="com/example/interactivity/fields/HexColorFieldNode.java" region="field"
```

The constructor, the factory and the generic setters follow the pattern of every node of your own ([Custom nodes](/use/learn/going-further/custom-nodes/#give-the-node-a-property)).

| Method | What it does |
|---|---|
| `parse(text)` | Turns a text into a value, or returns `null` when the text is not one |
| `format(value)` | Turns a value into the text that the field shows |
| `accepts(text)` | Refuses an edit, like `accept(...)`, for every field of the class |
| `correct(value)` | Brings a value back into its bounds, as `min` and `max` do |
| `increment(value, count)` | Gives the value of `count` steps of Up and Down, or `null` for none |

`startValue(...)` and `bindValue(...)` are `protected`: expose them under the name of your property, as `value(...)` does above.
`write(value, true)` acts like the user, for a `change(...)` of your own.

```java file="com/example/interactivity/fields/ColorFieldUI.java" region="use"
```

<figure>

![A dark grey field that reads "#5B8CFF" next to a blue rectangle; the user selects the text and types "#F2A541", and the rectangle turns orange once the color is complete; Escape leaves the field, the user clicks it again and types "#5B8CFF" back, and the rectangle stays orange until the last F turns it blue](/captures/interactivity-field-color.webp)

<figcaption>
The rectangle reads the same signal as the field: a valid color shows at once.
</figcaption>
</figure>

## Recap

- A **text field** needs `info(...)`, binds its text to a signal, and runs `onChange` on each edit and **`onSubmit`** on Enter.
- A field gains the **focus** on a press, and loses it on a press elsewhere, Enter, Escape or its detach; a press keeps **one field** focused at a time.
- A focused field **consumes every key**; shortcuts set with `overrideFocus(true)` still run.
- **`accept`**, **`maxTextLength`**, **`normalize`** and **`allowEmpty`** shape the text; the field rewrites it when it loses the focus.
- **`IntegerFieldNode`** holds a number between `min` and `max`, stepped by the arrows and the wheel.
- **`MultilineTextFieldNode`** wraps its text; Enter starts a new line.
- A field of your own extends **`LineFieldNode`** and implements `parse` and `format`.

## Try it

**Submit only valid names.**
In `NameFieldUI`, count a submit only when the name has at least two letters.

<details>
<summary>Solution</summary>

The listener reads the field:

```java fragment
.onSubmit(event -> {
	if (event.getNode().getText().trim().length() >= 2) {
		this.submits.increment();
	}
})
```

</details>

**Step the color.**
Make Up and Down in `HexColorFieldNode` brighten and darken the color.

<details>
<summary>Solution</summary>

`increment` gives the value of a number of steps:

```java fragment
@Override
protected Color increment(final Color value, final int count) {
	return count > 0 ? value.brighter() : value.darker();
}
```

</details>
