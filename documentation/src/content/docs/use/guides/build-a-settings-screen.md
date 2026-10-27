---
title: "Build a settings screen"
description: "Bind the controls of a settings screen to a store that JOID keeps on disk."
genre: guide
sidebar:
  order: 3
---

Settings outlive the screen that edits them: the user expects to find them again after a restart.
This guide keeps two settings, a sound switch and a volume, in a store saved on disk, and binds the controls of a screen to it.

## What you will build

<figure>

![A Sound label with a checked box on its right, and a Volume 80 label with a slider on its right; the pointer drags the slider and the label follows, unchecks Sound, which disables the slider, checks it again and drags the slider back to 80](/captures/guides-settings-screen.webp)

<figcaption>
Each control reads and writes a signal of the store.
</figcaption>
</figure>

## Before you start

- Stores and their scopes: [Stores and properties](/use/learn/state/stores-and-properties/).
- Controls and their signal binding: [Controls](/use/learn/interactivity/controls/).
- Skins for the controls: this guide uses the plain checkbox and slider that [Build a UI kit](/use/guides/build-a-ui-kit/) writes in its first step.

## 1. Write the store

A [[UIStore]] annotated with [[UIStoreData]] and the scope [[UIStoreScope#PERMANENT]] lives as long as the application and is written to disk.
Give it one signal per setting, and a getter for each:

```java file="com/example/guides/SettingsGuideStore.java" region="fields"
```

```java file="com/example/guides/SettingsGuideStore.java" region="getters"
```

**Check:** the class compiles.

## 2. Bind the controls to the store

`useStore` returns the store, and creates it the first time with the factory you give it:

```java file="com/example/guides/SettingsGuideUI.java" region="use"
```

Bind each control to its signal: `checked(...)` for the checkbox, `value(...)` for the slider.
The control then shows the signal and writes the value the user picks into it:

```java file="com/example/guides/SettingsGuideUI.java" region="sound"
```

```java file="com/example/guides/SettingsGuideUI.java" region="volume"
```

- `save()` in `onChange` writes the file as soon as a value changes, so a crash loses nothing.
- `enabled(store.getSound())` greys the slider out while the sound is off.
- The label derives its text from the volume with `map(...)` ([Derived values](/use/learn/state/derived-values/)).

**Check:** move the volume to 30, then uncheck the sound: the label reads `Volume 30`, and the slider greys out.

## 3. Read and write the file

JOID writes what `toJson()` returns, and gives the content of the file to `fromJson(...)` when the store is created again.
Read each key only when the file has it, and each number through `Number`, since the file gives it back as a `Long` or a `Double`:

```java file="com/example/guides/SettingsGuideStore.java" region="json"
```

A file written before you added a setting keeps working: the setting it lacks keeps its default value.

**Check:** set the volume to 30, close the application and start it again: the screen shows the volume at 30.
The file is `store/audio.store` in the [data folder](/use/learn/running/files/).

## 4. Read the settings elsewhere

Every UI that calls `useStore(SettingsGuideStore.class, ...)` gets the same instance, with the same signals.
A text of another UI that reads `getVolume()` follows the slider of this screen, and the factory runs only once.

**Check:** open an overlay ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/)) that shows the volume with the same `map(...)`, then move the slider: the text of the overlay follows it.

## If it does not work

1. `useStore` throws `IllegalStateException: The store … has no @UIStoreData annotation`: add the annotation to the store class.
2. The settings come back to their defaults after a restart: the scope is not `PERMANENT`, or `fromJson` reads other keys than `toJson` writes.
3. The error output prints `[JOID] The store … cannot be restored from …`: `fromJson` threw on the file, such as a `ClassCastException` on a number cast to `Integer`; JOID keeps the file aside and starts the store from its defaults.
4. The settings are lost when the application starts from another folder: the data folder is relative to the working directory by default; move it with `dataDir(...)` ([Where JOID stores files](/use/learn/running/files/)).
5. A change of a control reaches neither the store nor the other UIs: the control received a value, such as `value(80)`, instead of the signal.

## Reference

- [[UIStore]]
- [[UIStoreData]]
- [[UIStoreScope]]
- [[CheckboxNode]]
- [[IntegerSliderNode]]
