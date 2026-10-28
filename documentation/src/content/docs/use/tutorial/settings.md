---
title: "Save the settings"
description: "Keep the music and volume settings in a permanent store, shared by two UIs and saved across sessions."
genre: tutorial
sidebar:
  order: 6
---

In this lesson the settings popup works: the player turns the music on and off and sets the volume, the menu shows the result, and both survive a restart.

<figure>

![The settings popup over the darkened menu: Music with an On button, Volume with minus and plus buttons around 80 %, and a Close button; the pointer turns the music off, raises the volume by one step, lowers it back and turns the music on, and the line of the menu behind the popup follows each click](/captures/tutorial-settings.webp)

<figcaption>
The result of this lesson: the popup and the menu read the same store.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/state/) fills the panel from a signal.
The settings popup still shows fixed texts, and nothing remains when the program stops.

## 1. Write a permanent store

A store holds values that outlive a UI.
Its scope decides how long: `PERMANENT` keeps it for the whole run, shares it between every UI that asks for it, and saves it to a file.
Create `SettingsStore.java`:

```java file="com/example/tutorial/settings/SettingsStore.java" title="SettingsStore.java"
```

- The annotation [[UIStoreData]] names the file of the store, and its scope, a [[UIStoreScope]], sets how long the store lives: `@UIStoreData(id = "settings", scope = UIStoreScope.PERMANENT)`.
- `toJson()` writes the values when JOID saves the store; `fromJson(...)` reads them back when a file exists, and without one the store keeps the defaults of its fields.
- The values are signals, so every node built from them follows them.

The values travel in a `Map`, and a number comes back from the file as a `Long` or a `Double`: `((Number) json.get("volume")).intValue()` reads it as an `int` ([Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk)).
Nothing uses the store yet, so the menu looks the same.

## 2. Change the settings in the popup

`SettingsUI` gets the store with `useStore(...)`, and replaces the two fixed lines with buttons:

```java file="com/example/tutorial/settings/SettingsUI.java" title="SettingsUI.java" ins={7-8,14,25-27,34-61}
```

- `useStore(SettingsStore.class, SettingsStore::new)` returns the store, and creates it with the factory the first time.
- `music` and `volume` hold the two signals of the store: a setter follows a signal read from a local variable that holds it, in any expression ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).
- The On/Off button toggles `music`, and its label is built from it.
- The minus and plus buttons change `volume` by one step, between 0 and 10; the text shows it as a percentage.

**Run it.**
Open the settings: the music button switches between On and Off, and the volume moves by 10 % between 0 % and 100 %.
Close the popup and open it again: it shows the values you left.

## 3. Show the settings in the menu

`MenuUI` asks for the same store; a `PERMANENT` store exists once, so both UIs share the same signals:

```java file="com/example/tutorial/settings/MenuUI.java" region="store" title="MenuUI.java"
```

Add a line under the buttons, built from both settings:

```java file="com/example/tutorial/settings/MenuUI.java" region="footer"
```

As in `SettingsUI`, read both signals through local variables ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).

**Run it.**
Open the settings and change the music and the volume: the line under the buttons changes at once, behind the popup.

## 4. Close the UIs when the program stops

JOID saves the store when a UI that uses it closes, and again when the program exits normally.
When the player closes the window, the menu is still open when the loop ends.
Close every open UI after the loop, before the window is destroyed: their close listeners run and the store reaches the disk at that moment, while a killed process saves nothing ([Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends)).

```java file="com/example/tutorial/settings/Main.java" title="Main.java" ins={43}
```

**Run it.**
Change the settings, close the window, and run the program again: the menu shows your settings.
They live in `joid/store/settings.store`, under the working directory: [Where JOID stores files](/use/learn/running/files/#find-the-folders) explains the folder.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/settings/MenuUI.java"
```

</details>

## Challenge

Count the launches of the game, and show "Launch 3" under the settings line on the third one.
Use a field annotated with [[UIProperty]], which JOID saves when the UI closes and restores before `init()`.

<details>
<summary>Solution</summary>

Add the field to `MenuUI`:

```java file="com/example/tutorial/settings/ChallengeUI.java" region="field"
```

Then count and show it in `init()`:

```java file="com/example/tutorial/settings/ChallengeUI.java" region="solution"
```

A property suits a value that belongs to one UI; a store suits values that several UIs share.

</details>

## What you learned

- A [store](/use/learn/state/stores-and-properties/) holds values that outlive a UI; its scope decides how long it lives and whether it is saved.
- Two UIs that ask for the same `PERMANENT` store share its signals.
- JOID saves stores and properties when a UI closes; [close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends) so that they close properly.
- A field marked `@UIProperty` keeps its value from one run to the next ([Stores and remembered properties](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui)).
