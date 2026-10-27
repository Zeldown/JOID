---
title: "Keyboard and shortcuts"
description: "React to keys and typed characters, know who receives the keyboard, read a held key, and bind shortcuts."
genre: learn
sidebar:
  order: 5
---

The keyboard reaches a UI in two forms: the keys that go down and up, and the characters that they type.
This page shows who receives them, how to tell a letter from a place on the keyboard, and how to bind a shortcut.

**You will learn**
- How to react to a key and to a typed character
- Which nodes and UIs receive the keyboard
- How to tell the letter of a key from its place
- How to act while a key is held
- How to bind a shortcut to a UI or a node

## React to a key

The keyboard uses the `handle` events of [Events](/use/learn/interactivity/events/#hear-the-raw-input), on a node or, here, on the UI:

```java file="com/example/interactivity/keyboard/KeysUI.java" region="keys"
```

<figure>

![A grey card that reads "Key: none" and "Typed: "; the user types hello: "Key" names each key as it goes down, and each letter joins "Typed"](/captures/interactivity-keys.webp)

<figcaption>
The user types `hello`: each key goes down once, and each character arrives as a code point.
</figcaption>
</figure>

- [[UI#handleKeyPressed]] and [[Node#handleKeyPressed]] receive each key that goes down, as a [[Key]].
- [[UI#handleKeyReleased]] and [[Node#handleKeyReleased]] receive it when it goes up.
- [[UI#handleCharTyped]] and [[Node#handleCharTyped]] receive each typed character as a code point, with the layout and the Shift key applied; control characters, such as the one of Enter, never arrive there.

A key event is a [[NodeKeyEvent]] on a node and a [[UIKeyEvent]] on a UI; a character event is a [[NodeCharEvent]] or a [[UICharEvent]].
The release always reaches every node, even hidden or disabled ([Let the pointer through a decoration](/use/learn/interactivity/events/#let-the-pointer-through-a-decoration)).

## Know who receives the keyboard

The keyboard has no pointer: every visible node of the UI receives each key, wherever the pointer is.
For one key that goes down, the order is:

1. the shortcuts set with `overrideFocus(true)` ([Bind a shortcut](#bind-a-shortcut));
2. the nodes, from the front to the back, as for a press ([Follow an input through the tree](/use/learn/interactivity/events/#follow-an-input-through-the-tree)); a focused text field consumes every key;
3. the `handle` listeners of the UI;
4. the other shortcuts;
5. the keys of the UI itself, in [dev mode](/use/get-started/dev-tools/) only: the zoom (Ctrl or Alt with `+` and `-`, on a [zoomable](/use/learn/describing-the-ui/uis/) UI), the reload and the inspector.

Each step runs only when the key is not consumed yet.

Escape closes the top UI when it is closeable, is not an overlay, and no node or listener consumed the key; [[UI#beforeClose]] can still keep it open.
A focused text field consumes Escape to lose the focus, so the first Escape leaves the field and the second one closes the UI.
A key that the top screen does not consume goes on to the UI below it, except under a popup ([How UIs stack](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).

JOID has no focus of its own outside the text fields: a node that needs the keys tests its own state in its listener, and consumes what it handles.
[Text fields](/use/learn/interactivity/text-fields/#give-and-take-the-focus) explains the focus of a field.

## Tell a letter from its place

A key event holds two keys:

- [[NodeKeyEvent#getKey]] is the key that the layout of the user names: on a French AZERTY keyboard, the key at the place of `Q` is `Key.A`;
- [[NodeKeyEvent#getPhysicalKey]] is the place of the key on a US QWERTY keyboard, whatever the layout: that same key is `Key.Q`.

Use `getKey()` for a shortcut, so that Ctrl + Z is the key that reads `Z`.
Use `getPhysicalKey()` for a movement, so that W, A, S and D stay under the left hand on every layout.
Only the letters and the punctuation depend on the layout; the digits, the arrows, the modifiers and the function keys are the same in both.

## Act while a key is held

A held key sends one event when it goes down and one when it goes up, never a repeat.
To act on every frame while it is held, read its state in an update:

```java file="com/example/interactivity/keyboard/WalkUI.java" region="walk"
```

<figure>

![A grey square that slides to the right of a card while D is held, and stops when it is released](/captures/interactivity-walk.webp)

<figcaption>
The square moves 300 design pixels per second while D is held, at any frame rate.
</figcaption>
</figure>

- [[Key#isDown]] tests a key by its name in the layout, like `getKey()`.
- [[Key#isPhysicalDown]] tests a key by its place, like `getPhysicalKey()`.
- [[UI#isShiftKeyDown]], [[UI#isCtrlKeyDown]] and [[UI#isAltKeyDown]] test both keys of a modifier.

Text fields repeat a held key on their own, after 500 ms then every 100 ms.

## Bind a shortcut

A [[Shortcut]] runs a listener when its key goes down with its modifiers:

```java file="com/example/interactivity/keyboard/ShortcutUI.java" region="shortcut"
```

<figure>

![A card with a white "Notes" field and the texts "Ctrl + S: 0" and "F1: 0"; Ctrl + S and F1 count once each, then the pointer clicks the field, the user types, and of the same two shortcuts only F1 counts again](/captures/interactivity-shortcut.webp)

<figcaption>
Ctrl + S counts once before the field takes the focus; F1 counts twice, the second time while the user types in the field.
</figcaption>
</figure>

- [[Shortcut#ctrl]], [[Shortcut#alt]] and [[Shortcut#shift]] give the modifiers, and they must match exactly: `Shortcut.create(Key.S).ctrl(true)` does not run on Ctrl + Shift + S.
- A shortcut matches [[NodeKeyEvent#getKey]], the key that the layout names.
- [[Shortcut#onTrigger]] receives a [[ShortcutEvent]] and consumes the key: the nodes and the listeners after it never see it.
- [[Shortcut#overrideFocus]] `(true)` runs the shortcut before the nodes, so that it works while a text field takes the keys.

`attach(ui)` and `attach(node)` add the shortcut to a UI or a node, like [[UI#addShortcut]] and [[Node#addShortcut]]; [[Shortcut#detach]] removes it.
A shortcut of a node works only while the node is visible and enabled, and the shortcuts of the nodes in front run before the others.
A shortcut lives like a listener ([Open and close a UI](/use/learn/describing-the-ui/uis/#open-and-close-a-ui)).

### Use the key bindings of your application

`Shortcut.create(...)` also takes a binding of your own, such as the key that a player chose in the settings of your application.
An [[IKeyResolver]] tells JOID which [[Key]] such a binding means, and JOID asks it again at each key, so a new choice applies at once:

```java fragment
KeyResolver.register(new SettingsKeyResolver());
Shortcut.create(this.settings.getJumpBinding()).onTrigger(event -> this.jump()).attach(this);
```

The resolver implements `supports(binding)` and `resolve(binding)`; register it with [[KeyResolver#register]] before the first key.

## Recap

- **`handleKeyPressed`**, **`handleKeyReleased`** and **`handleCharTyped`** receive the keyboard on a node or a UI.
- Every **visible node** receives each key, wherever the pointer is; then the UI listeners, the shortcuts and the keys of the UI, until a step **consumes** it.
- **Escape** closes the top closeable UI when nothing consumed it; a focused field takes the first Escape.
- **`getKey()`** names the key in the layout of the user, for shortcuts; **`getPhysicalKey()`** gives its place, for movements.
- A held key sends no repeat: read **`isDown()`** or **`isPhysicalDown()`** in an update.
- A **`Shortcut`** runs on its key with its exact modifiers; **`overrideFocus(true)`** makes it work while a field has the focus.

## Try it

**Pause with Space.**
In `WalkUI`, stop the square while Space is held down.

<details>
<summary>Solution</summary>

Read the key at the start of the same update:

```java fragment
if (Key.SPACE.isDown()) {
	return;
}
```

</details>

**Save with Ctrl + Shift + S too.**
In `ShortcutUI`, make Ctrl + Shift + S count as a save as well.

<details>
<summary>Solution</summary>

The modifiers must match exactly, so add a second shortcut:

```java fragment
Shortcut.create(Key.S).ctrl(true).shift(true).onTrigger(event -> this.saves.increment()).attach(this);
```

</details>
