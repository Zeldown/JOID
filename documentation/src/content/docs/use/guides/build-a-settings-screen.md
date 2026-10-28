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

![A Sound label with a checked box on its right, its mark blue, and a Volume 80 label with a slider and its blue thumb on its right; the pointer drags the slider and the label follows, unchecks Sound, which disables the slider, checks it again and drags the slider back to 80](/captures/guides-settings-screen.webp)

<figcaption>
Each control reads and writes a signal of the store.
</figcaption>
</figure>

## Before you start

- Stores and their scopes: [Stores and remembered properties](/use/learn/state/stores-and-properties/).
- Controls and their signal binding: [Controls](/use/learn/interactivity/controls/).
- Skins for the controls: this guide uses the plain checkbox and slider that [Build a UI kit](/use/guides/build-a-ui-kit/) writes in its first step.

## 1. Write the store

Write a [[UIStore]] of scope [[UIStoreScope#PERMANENT]], with one signal per setting, a getter for each, and the `toJson`/`fromJson` that keep them on disk, as [Write a store](/use/learn/state/stores-and-properties/#write-a-store) and [Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk) show.
This guide names its file `audio` in [[UIStoreData]] and keeps two signals:

```java file="com/example/guides/SettingsGuideStore.java" region="fields"
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

Another UI that asks for the store gets the same signals, so it follows this screen ([Use a store from several UIs](/use/learn/state/stores-and-properties/#use-a-store-from-several-uis)).

**Check:** move the volume to 30, then uncheck the sound: the label reads `Volume 30`, and the slider greys out.
Close the application and start it again: the screen shows the same values, read from `store/audio.store` in the [data folder](/use/learn/running/files/).

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
