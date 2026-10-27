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
- How to give and take the focus
- How to shape what the user can type
- How to ask for a number or for several lines
- How to write a field for a value of your own

## Add a text field

A field takes its font with `info(...)`, and binds its text to a [[StringSignal]]:

```java file="com/example/interactivity/fields/NameFieldUI.java" region="signal"
```

```java file="com/example/interactivity/fields/NameFieldUI.java" region="field"
```

<figure>

![A white text field with the placeholder "Your name", and the texts "Hello, " and "Submits: 0"; the pointer clicks the field, the user types "Ada" and the greeting follows each key, then Enter makes "Submits: 1"](/captures/interactivity-field.webp)

<figcaption>
The signal follows each key, so "Hello, Ada" changes while the user types; Enter submits once.
</figcaption>
</figure>

[[TextFieldNode]] needs a [[TextInfo]] before it is attached; without one, the attach throws an `IllegalStateException`.
With the factory without a height, the field takes the height of its font plus its margins, set by [[FieldNode#margin]], or one side at a time with `marginTop(...)`, `marginBottom(...)`, `marginLeft(...)` and `marginRight(...)`.
`KitTextFieldNode` is a [[TextFieldNode]] with a white `background(...)` and a border while it has the focus; any node can be the background.

- [[FieldNode#onChange]] runs on each edit with a [[NodeFieldChangeEvent.Post]]: `getText()` is the text, `getValue()` the value, `isValid()` whether the text is a valid value.
- [[LineFieldNode#onSubmit]] runs when the user presses Enter, after the field lost the focus.
- `placeholder(...)` shows a text while the field is empty and has no focus, in the color of [[FieldNode#placeholderColor]].
- [[FieldNode#cursorColor]] and [[FieldNode#selectionColor]] color the cursor and the selection; the cursor blinks.
- [[LineFieldNode#alignX]] places the text at the start, the center or the end of the field.

## Give and take the focus

A field takes the keyboard only while it has the focus: it then consumes every key and every character ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).

- A press on the field gives it the focus and puts the cursor under the pointer.
- A press anywhere else takes the focus, and so do Enter in a one-line field, Escape, and the detach of the field.
- Escape keeps the typed text ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard) gives what a second Escape does).
- One field has the focus at a time, in all the UIs on the screen: a press on a field first takes the focus from the field that had it, which runs its `onBlur`, then gives it to the new one.

[[FieldNode#focus]] and [[FieldNode#blur]] act from your code, and `focused(...)` binds the focus to a signal:

```java file="com/example/interactivity/fields/FocusFieldUI.java" region="focus"
```

<figure>

![A white "Search" field, a white "Search" button and the text "Not focused"; the pointer clicks the button, the field gets its focus border and the cursor, and the text reads "Focused"; a press elsewhere on the card leaves the focus in the empty field](/captures/interactivity-focus.webp)

<figcaption>
The button gives the focus to the field, and the signal follows it.
</figcaption>
</figure>

`beforeFocus` and `beforeBlur` can refuse the focus or keep it: here, the field keeps the focus while it is empty.
`onFocus` and `onBlur` run after the change.

The user edits the text with the keys of every text editor:

| Keys | Action |
|---|---|
| Left, Right | Move the cursor; with Ctrl (Alt on macOS), by word |
| Home, End | Go to the start or the end of the line |
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

![A white field with the placeholder "Code" and the text "Signal: "; the user types a space, a, b and 1: the digit never appears, and Enter turns the text into "AB" in the field and in the signal](/captures/interactivity-field-rules.webp)

<figcaption>
The user types ` ab1` then Enter: the digit is refused, and the field rewrites ` ab` as `AB` when it loses the focus.
</figcaption>
</figure>

- [[FieldNode#accept]] refuses an edit whose text fails the test: the digit never appears.
- [[FieldNode#maxTextLength]] cuts the text to a number of characters, also for a paste.
- [[FieldNode#normalize]] turns the text into the value: the field shows the normalized text when it loses the focus, and at once for a text given by a setter or a signal.
- [[FieldNode#allowEmpty]] `(true)` lets the field be empty, with the value `null`; otherwise, a field that the user empties takes back its last value when it loses the focus.
- [[FieldNode#markup]] `(true)` shows the [markup](/use/learn/describing-the-ui/text-markup-and-effects/) of the text instead of its tags.

The signal follows the text as the user types it, and receives the normalized text with the rewrite.
A rule that changes keeps the text asked for, as a control keeps its value ([Keep a value that the bounds refuse](/use/learn/interactivity/controls/#keep-a-value-that-the-bounds-refuse)): `maxTextLength` that grows again shows the whole text.

## Ask for a number

```java file="com/example/interactivity/fields/NumberFieldUI.java" region="number"
```

<figure>

![A white number field that reads 4, and the text "Players: 4"; the pointer clicks the field, two presses of the Up arrow bring both to 6, and two presses of the Down arrow bring them back to 4](/captures/interactivity-field-number.webp)

<figcaption>
Two presses of the Up arrow, then two of the Down arrow: the value goes from 4 to 6 and back, and the signal follows.
</figcaption>
</figure>

[[IntegerFieldNode]] holds an `Integer` between [[IntegerFieldNode#min]] and [[IntegerFieldNode#max]]:

- the user types digits, and a minus sign when `min` is below `0`;
- Up and Down, or the wheel over the focused field, add or remove [[IntegerFieldNode#step]];
- a typed value out of the range shows as its bound when the field loses the focus;
- `value(...)` gives the value or binds it to a signal, and `change(value)` acts like the user.

An empty field that the user leaves takes back its last value, `0` before any; with `allowEmpty(true)`, it holds `null`, and a bound signal receives `null`.

## Let the user type several lines

```java file="com/example/interactivity/fields/NotesFieldUI.java" region="notes"
```

[[MultilineTextFieldNode]] wraps its text in its width.
Enter starts a new line, Up and Down move the cursor between lines, and the wheel scrolls a text taller than the field.

## Write a field for your own value

A field for another type extends [[LineFieldNode]] and says how a text becomes a value:

```java file="com/example/interactivity/fields/HexColorFieldNode.java" region="field"
```

| Method | What it does |
|---|---|
| `parse(text)` | Turns a text into a value, or returns `null` when the text is not one |
| `format(value)` | Turns a value into the text that the field shows |
| `accepts(text)` | Refuses an edit, like `accept(...)`, for every field of the class |
| `correct(value)` | Brings a value back into its bounds, as `min` and `max` do |
| `increment(value, count)` | Gives the value of `count` steps of Up and Down, or `null` for none |

`startValue(...)` and `bindValue(...)` are the two forms of a value setter: give them a public name, as `value(...)` above.
`write(value, true)` acts like the user, for a `change(...)` of your own.

```java file="com/example/interactivity/fields/ColorFieldUI.java" region="use"
```

<figure>

![A white field that reads "#555555" next to a dark grey rectangle; the user selects the text and types "#7C5CFF", and the rectangle turns violet once the color is complete, then the user types "#555555" back](/captures/interactivity-field-color.webp)

<figcaption>
The rectangle reads the same signal as the field: a valid color shows at once.
</figcaption>
</figure>

## Recap

- A **text field** needs `info(...)`, binds its text to a signal, and runs `onChange` on each edit and **`onSubmit`** on Enter.
- A press gives the **focus**, a press elsewhere, Enter, Escape or a detach takes it; **one field** has the focus at a time on the screen.
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
