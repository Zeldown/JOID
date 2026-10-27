---
title: "Controls"
description: "Bind a checkbox, a toggle, a switch, a selector or a slider to a signal, react to its changes, refuse one, and show a progress."
genre: learn
sidebar:
  order: 6
---

A control holds a value that the user changes: a checkbox, a toggle, a switch, a selector, a slider.
Every control of JOID follows the same pattern: options, a value, a signal bound both ways, and the events `beforeChange` and `onChange`.
Learn the pattern once on this page, then the options of each control.

**You will learn**
- How to bind a control to a signal
- How to react to a change, and refuse one
- Why a control keeps a value that its bounds refuse
- In which order a change runs its events
- How to use the options of each control, and show a progress

## Bind a control to a signal

A control is abstract: its look comes from a skin, a small subclass that gives it its parts.
The controls of this page use the plain skins of the examples (`PlainCheckboxNode`, `PlainIntegerSliderNode`…); [UI kit](/use/learn/going-further/ui-kit/) shows how to write your own.

```java file="com/example/interactivity/controls/SettingsUI.java" region="signals"
```

```java file="com/example/interactivity/controls/SettingsUI.java" region="bind"
```

<figure>

![A card with a checked "Subtitles" box, a "Volume" slider and a number field at 9, and the texts "Subtitles: true" and "Volume: 9"](/captures/interactivity-settings.webp)

<figcaption>
The slider and the field share `volume`: the user dragged the slider from 7 to 9, and the field and the text followed.
</figcaption>
</figure>

The value of a control has two setters, and the argument decides which one runs:

- a plain value, such as `checked(true)` or `value(7)`, gives the control its start value;
- a [signal](/use/learn/interactivity/signals/), such as `checked(this.subtitles)`, binds the control both ways: the control shows the value of the signal, and writes into it each value that the user picks.

A bound signal must be writable: a derived value, which cannot be written, throws an `IllegalArgumentException`.
Two controls bound to the same signal show the same value, as the slider and the field above.
`change(value)` sets the value as if the user picked it, with its `before` event ([Refuse a change](#refuse-a-change)).

## React to a change

`onChange` runs after each real change of the value, whatever its source: a gesture, a setter, `change(...)` or the bound signal.
It never runs for a value equal to the current one, and not for the start value.
Its event holds the old value (`getPrevious()`) and the new one (`getValue()`):

```java fragment
PlainCheckboxNode.create(240, 40, 50, 50).onChange(event -> this.save(event.getValue())).attach(card);
```

Most of the time, a signal makes `onChange` useless: the nodes that show the value read the signal.
Keep `onChange` for an action, such as saving the settings or playing a sound.

## Refuse a change

`beforeChange` runs before a change that the user or `change(...)` asks for, and `cancel()` refuses it:

```java file="com/example/interactivity/controls/RefuseUI.java" region="refuse"
```

<figure>

![A slider whose thumb sits at 8 of 10 after a drag to the end of the track, and the text "Volume: 8"](/captures/interactivity-refuse.webp)

<figcaption>
The user dragged the thumb to the end of the track: at the release, the value is 8, the last one that `beforeChange` let through.
</figcaption>
</figure>

The setters and the bound signal are never refused: "Set to 10" writes `10` into `volume`, and the slider shows `10`.
A refusal belongs to the user, the value of your code always applies.
A refused value does not stop the drag of a slider: the thumb follows the pointer, and the value keeps the last step that passed.

## Keep a value that the bounds refuse

A control remembers the last value that it was asked for, by a setter, its signal or the user, and shows it within its bounds.
When the bounds change, it asks for that value again:

```java file="com/example/interactivity/controls/PendingUI.java" region="pending"
```

<figure>

![A card with a checked "Limit to 10" box, a number field that shows 10, and the text "Tickets: 10"](/captures/interactivity-pending.webp)

<figcaption>
With the limit, the field shows `10` and `tickets` holds `10`; once the box is unchecked, the field and the signal are back to `50`.
</figcaption>
</figure>

- The value shown is the value asked for, kept within the bounds: `max(...)` and `min(...)` of a number field, the options of a switch, a selector or a slider.
- The bound signal holds the value shown, so every node that reads it agrees with the control.
- The value asked for waits until the bounds allow it: the next value that the user, a setter or the signal gives replaces it.

While the value asked for is not one of its options, a choice (switch, selector, slider) keeps its current value, or shows its first option when the current one is gone too.
The order of the setters does not matter either: `value(7)` before `values(...)` gives the same control as after it.

## Know the order of a change

One change of a control runs its steps in this order:

1. `beforeChange`, for a gesture or a call to `change(...)`; a cancel stops everything;
2. the value applies, and the control writes it into its bound signal;
3. the listeners of that signal run: every other node bound to it shows the new value;
4. the `onChange` listeners of the controls, in the order of the changes.

In an `onChange` listener, every node bound to the same signal already shows the new value.
When a listener corrects the value, such as `if (event.getValue() > 50) slider.change(50)`, the listeners receive `0 > 80`, then `80 > 50`: the last `onChange` always gives the final value.
Inside your own [[Signal#batch]], `onChange` runs at once, and the nodes bound to the signal follow when the batch ends ([Derived values](/use/learn/state/derived-values/)).

Two controls bound to one signal with bounds that never agree, such as a slider from `0` to `100` and a field limited to `95`, stop correcting each other after a few writes: the signal keeps the value of the gesture, each control shows its own bound, and the dev mode prints a warning.

## Turn an option on or off

[[BaseCheckboxNode]] holds a `boolean`: `checked(...)` gives it, `isChecked()` reads it, and a press on the box toggles it.
A [[CheckboxNode]] draws a `box` and a `mark`, which shows while the box is checked.

## Choose between two values

```java file="com/example/interactivity/controls/ChoicesUI.java" region="toggle"
```

[[BaseToggleNode]] holds a `boolean` with `toggle(...)`, and a press switches it.
`state(onValue, offValue)` gives the two values that `getValue()` returns, here `"Night"` and `"Day"`.
A [[ToggleNode]] slides its `knob` along its `track`: [[ToggleNode#speed]] is the part of the way done in 1/60 s, and `1F` moves it at once.

## Choose one of a few options

```java file="com/example/interactivity/controls/ChoicesUI.java" region="switch"
```

<figure>

![A row of three options, "Story", "Normal" and "Hard", with "Hard" selected in grey, and the text "Difficulty: Hard"](/captures/interactivity-switch.webp)

<figcaption>
A switch shows every option side by side; a press on one selects it.
</figcaption>
</figure>

The choice controls share the options of [[BaseChoiceNode]]:

- `values(...)` gives the options, at least one; a control attached without options throws an `IllegalStateException`;
- `value(...)` selects an option, and throws an `IllegalArgumentException` for a value that is not one;
- `valueIndex(...)` selects an option by its index, a plain one or an [[IntegerSignal]] bound both ways;
- `getValue()`, `getValueIndex()` and `getValueList()` read them.

New options keep the value when it is still one of them; otherwise the control selects the first one.
A [[SwitchNode]] builds one node per option with its `option((value, index) -> node)` and lays them out side by side.

## Choose from a drop-down list

```java file="com/example/interactivity/controls/ChoicesUI.java" region="selector"
```

<figure>

![An open selector that lists "English", "French", "German" and "Japanese" under its field, with "French" under the pointer](/captures/interactivity-selector.webp)

<figcaption>
A press opens the list; a press on an option selects it and closes the list.
</figcaption>
</figure>

A [[SelectorNode]] shows its selected option and opens a list of the others.
[[SelectorNode#direction]] opens it `DOWN`, the default, or `UP` near the bottom of the screen.
[[BaseSelectorNode#open]] and [[BaseSelectorNode#close]] act from your code; `beforeOpen`, `onOpen`, `beforeClose` and `onClose` follow it, and `active(...)` binds the open state to a signal.
The list closes when the selector is detached.

## Pick a value on a track

```java file="com/example/interactivity/controls/ChoicesUI.java" region="slider"
```

```java file="com/example/interactivity/controls/ChoicesUI.java" region="quality"
```

<figure>

![A slider whose thumb sits at the third of four steps, and the text "Quality: HIGH"](/captures/interactivity-slider.webp)

<figcaption>
A slider picks among ordered values; the thumb jumps to the step under a press.
</figcaption>
</figure>

A slider is a choice whose options sit along a track: a press on the track jumps to the nearest step, and a drag follows the pointer.

| Slider | Options |
|---|---|
| [[IntegerSliderNode]] | `range(min, max)` or `range(min, max, step)` |
| [[DoubleSliderNode]] | `range(min, max, step)` |
| [[StringSliderNode]] | `values(...)` of texts or of the constants of an enum, and `value(constant)` |

A [[SliderNode]] places a `thumb` on a `track`.
[[BaseSliderNode#inset]] keeps the first and the last step away from the ends, by half the thumb by default.
[[SliderNode#speed]] eases the thumb to a new value, and `1F`, the default, moves it at once; [[BaseSliderNode#isSliding]] is `true` during a drag.

## Show a progress

A [[ProgressNode]] shows a value and takes no input:

```java file="com/example/interactivity/controls/ProgressUI.java" region="progress"
```

<figure>

![Two bars under a slider set at 6 of 10: the first is filled from the left on 60 % of its width, the second from the right](/captures/interactivity-progress.webp)

<figcaption>
The bars follow the slider: `0.6F` fills 60 % of the track, from the side of the direction.
</figcaption>
</figure>

The progress goes from `0F` to `1F`, and a value out of it shows an empty or a full bar.
[[ProgressNode#direction]] fills from the left (the default), the right, the top or the bottom.
The `fill` part is required, the `track` part is optional.

## Recap

- A **control** is abstract: a skin gives it its parts.
- A plain value gives the **start value**; a **signal** binds the value both ways; `change(...)` acts like the user.
- **`onChange`** runs on every real change, whatever its source, never for an equal value nor for the start value.
- **`beforeChange`** refuses what the user or `change(...)` asks for; setters and signals are never refused.
- A control **keeps the value asked for** and shows it within its bounds, and the signal holds the value shown.
- A change runs `beforeChange`, then applies, then updates the **bound nodes**, then runs `onChange`.
- **Choices** take their options with `values(...)`; new options keep the value when they still hold it.
- **`ProgressNode`** shows a value from `0F` to `1F` and takes no input.

## Try it

**Mute at zero.**
In `SettingsUI`, uncheck "Subtitles" when the volume reaches `0`.

<details>
<summary>Solution</summary>

An action on a change goes in `onChange`:

```java fragment
PlainIntegerSliderNode
.create(240, 140, 300, 40)
.range(0, 10)
.value(this.volume)
.onChange(event -> this.subtitles.set(event.getValue() > 0 && this.subtitles.peek()));
```

</details>

**A switch on an index.**
In `ChoicesUI`, keep the difficulty as an `IntegerSignal` from `0` to `2` instead of a text.

<details>
<summary>Solution</summary>

`valueIndex(...)` binds the index both ways:

```java fragment
PlainSwitchNode.<String>create(60, 60, 600, 60).values("Story", "Normal", "Hard").valueIndex(this.level).attach(card);
```

</details>
