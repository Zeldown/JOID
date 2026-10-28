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

A control is abstract: its look comes from its parts, the nodes that draw its box, its track or its thumb.
The controls of this page get their parts where they are created, in an empty subclass of the control, such as a [[CheckboxNode]] created with `new CheckboxNode(...) {}`, followed by the setters of the parts; [Give your controls a look](/use/learn/going-further/ui-kit/) gathers them in reusable skins, and [Know the parts of each control](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control) lists the parts of each control.
A part created with a size of `0` takes the size that the control gives it ([Skin a control with parts](/use/learn/going-further/ui-kit/#skin-a-control-with-parts)).

```java file="com/example/interactivity/controls/SettingsUI.java" region="signals"
```

```java file="com/example/interactivity/controls/SettingsUI.java" region="bind"
```

<figure>

![A dark card with a checked "Subtitles" box, its mark blue, a "Volume" slider and a number field at 7, and the texts "Subtitles: true" and "Volume: 7"; the pointer drags the slider to 9 and back to 7, and the field and the text follow](/captures/interactivity-settings.webp)

<figcaption>
The slider and the field share `volume`: while the user drags the slider, the field and the text follow.
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
checkbox.onChange(event -> this.save(event.getValue()));
```

Most of the time, a signal makes `onChange` useless: the nodes that show the value read the signal.
Keep `onChange` for an action, such as saving the settings or playing a sound.

## Refuse a change

`beforeChange` runs before a change that the user or `change(...)` asks for, and `cancel()` refuses it:

```java file="com/example/interactivity/controls/RefuseUI.java" region="refuse"
```

<figure>

![A slider at 5 of 10 with a blue thumb, a dark grey "Set to 10" button and the text "Volume: 5"; the pointer drags the thumb to the end of the track: the text stays at 8 and, at the release, the thumb goes back to 8; then it clicks "Set to 10", and the slider and the text show 10](/captures/interactivity-refuse.webp)

<figcaption>
`beforeChange` refuses the values above 8 that the user asks for: the thumb goes back to 8 at the release.
"Set to 10" writes the signal, which is never refused.
</figcaption>
</figure>

The setters and the bound signal are never refused: a refusal belongs to the user, the value of your code always applies.
A refused value does not stop the drag of a slider: the thumb follows the pointer, and the value keeps the last step that passed.

## Keep a value that the bounds refuse

A control remembers the last value that it was asked for, by a setter, its signal or the user, and shows it within its bounds.
A value that the bounds refuse is the **pending value**: the control asks for it again each time the bounds change:

```java file="com/example/interactivity/controls/PendingUI.java" region="pending"
```

<figure>

![A card with a "Limit to 10" box, a number field that shows 50 and the text "Tickets: 50"; the pointer checks the box, and the field and the text show 10, then unchecks it, and both are back to 50](/captures/interactivity-pending.webp)

<figcaption>
With the limit, the field shows `10` and `tickets` holds `10`; once the box is unchecked, the field and the signal are back to `50`.
</figcaption>
</figure>

- The value shown is the last value asked for, kept within the bounds: `max(...)` and `min(...)` of a number field, the options of a switch, a selector or a slider.
- The bound signal holds the value shown, so every node that reads it agrees with the control.
- The pending value waits until the bounds allow it: the next value that the user, a setter or the signal gives replaces it.
- Writing to the signal the value that it already holds changes nothing, so the pending value stays.

While the pending value is not one of its options, a choice (switch, selector, slider) shows a fallback.
An [[IntegerSliderNode]] or a [[DoubleSliderNode]] shows the option nearest to the pending value, the smaller one on a tie: `50` on `range(0, 10)` shows `10`.
Any other choice keeps its current value, or shows its first option when the current one is gone too.
The [[BaseChoiceNode#fallback]] hook picks that value, for a choice of your own.

The order of the setters does not matter either: `value(7)` before `values(...)` gives the same control as after it.

## Know the order of a change

One change of a control runs its steps in this order:

1. `beforeChange`, for a gesture or a call to `change(...)`; a cancel stops everything;
2. the value applies, and the control writes it into its bound signal;
3. the listeners of that signal run: every other node bound to it shows the new value;
4. the `onChange` listeners of the controls, in the order of the changes.

In an `onChange` listener, every node bound to the same signal already shows the new value.
When a listener corrects the value, such as `if (event.getValue() > 50) slider.change(50)`, the listeners receive the change from `0` to `80`, then from `80` to `50`: the last `onChange` always gives the final value.
Inside your own [[Signal#batch]], `onChange` runs at once, and the nodes bound to the signal follow when the batch ends ([Derived values](/use/learn/state/derived-values/)).

## Turn an option on or off

```java file="com/example/interactivity/controls/SettingsUI.java" region="checkbox"
```

<figure>

![On the settings card, the pointer clicks the box next to Subtitles twice: its blue mark disappears and the line below goes from Subtitles: true to Subtitles: false, then both come back](/captures/interactivity-checkbox.webp)

<figcaption>
Each press toggles the box, and the bound signal `subtitles` follows it.
</figcaption>
</figure>

[[BaseCheckboxNode]] holds a `boolean`: `checked(...)` gives it, `isChecked()` reads it, and a press toggles it.

## Choose between two values

```java file="com/example/interactivity/controls/ChoicesUI.java" region="toggle"
```

<figure>

![On a dark card, the pointer clicks a dark grey track with a blue knob twice: the knob moves to the other half of the track and the line below goes from Night: false to Night: true, then both come back](/captures/interactivity-toggle.webp)

<figcaption>
Each press moves the knob to the other half, and the bound signal `night` holds the new state.
</figcaption>
</figure>

[[BaseToggleNode]] holds a `boolean` with `toggle(...)`, and a press switches it.
`state(onValue, offValue)` gives the two values that `getValue()` returns, here `"Night"` and `"Day"`.
Its two type parameters are the types of the on value and of the off value.
[[ToggleNode#speed]] sets how fast a [[ToggleNode]] slides to its new state: the part of the way done in 1/60 s, and `1F` moves at once.

## Choose one of a few options

```java file="com/example/interactivity/controls/ChoicesUI.java" region="switch"
```

<figure>

![A row of three options, "Story", "Normal" and "Hard", with "Normal" selected in blue; the pointer selects "Hard", "Story", then "Normal", and the text "Difficulty" follows](/captures/interactivity-switch.webp)

<figcaption>
A switch shows every option side by side; a press on one selects it.
</figcaption>
</figure>

The choice controls share the options of [[BaseChoiceNode]]:

- `values(...)` gives the options, at least one; a control attached without options throws an `IllegalStateException`;
- `value(...)` selects an option, and throws an `IllegalArgumentException` for a value that is not one;
- `valueIndex(...)` selects an option by its index, a plain one or an [[IntegerSignal]] bound both ways;
- `getValue()`, `getValueIndex()` and `getValueList()` read them.

New options keep the value when it is still one of them; otherwise the control shows its [fallback](#keep-a-value-that-the-bounds-refuse).
`getValueList()` is a read-only view that follows the options set later.
A [[SwitchNode]] builds one node per option with its `option((value, index) -> node)` and lays them out side by side.
The option function runs once per value, so `value` acts like the variable of a loop: the color and the label read `difficulty` in a lambda ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).

## Choose from a drop-down list

```java file="com/example/interactivity/controls/ChoicesUI.java" region="selector"
```

<figure>

![A dark grey selector that reads "English"; the pointer opens it, and a list of "English", "French", "German" and "Japanese" covers it; the pointer picks "French", opens it again and picks "English", and the text "Language" follows](/captures/interactivity-selector.webp)

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

![A slider whose blue thumb sits at the third of four steps, and the text "Quality: HIGH"; the pointer presses the second step, then the last one, then drags the thumb to the first step and back to the third, and the text follows](/captures/interactivity-slider.webp)

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

[[BaseSliderNode#inset]] keeps the first and the last step away from the ends, by half the thumb by default.
[[SliderNode#speed]] eases the thumb to a new value, and `1F`, the default, moves it at once; [[BaseSliderNode#isSliding]] is `true` during a drag.

## Show a progress

A [[ProgressNode]] shows a value and takes no input:

```java file="com/example/interactivity/controls/ProgressUI.java" region="progress"
```

<figure>

![Two dark grey bars under a slider set at 6 of 10: the first is filled in blue from the left on 60 % of its width, the second from the right; the pointer drags the slider to 10, to 0, then back to 6, and both bars follow](/captures/interactivity-progress.webp)

<figcaption>
The bars follow the slider: at 6, `0.6F` fills 60 % of the track, from the side of the direction.
</figcaption>
</figure>

The progress goes from `0F` to `1F`, and a value out of it shows an empty or a full bar.
[[ProgressNode#direction]] fills from the left (the default), the right, the top or the bottom.

## Recap

- A **control** is abstract: a skin gives it its parts.
- A plain value gives the **start value**; a **signal** binds the value both ways; `change(...)` acts like the user.
- **`onChange`** runs on every real change, whatever its source, never for an equal value nor for the start value.
- **`beforeChange`** refuses what the user or `change(...)` asks for; setters and signals are never refused.
- A control keeps the **pending value** that its bounds refuse and asks for it again when they change; the signal holds the value shown.
- A change runs `beforeChange`, then applies, then updates the **bound nodes**, then runs `onChange`.
- **Choices** take their options with `values(...)`; new options keep the value when they still hold it, else the choice shows its **fallback**.
- **`ProgressNode`** shows a value from `0F` to `1F` and takes no input.

## Try it

**Hide subtitles at zero.**
In `SettingsUI`, uncheck "Subtitles" when the volume reaches `0`.

<details>
<summary>Solution</summary>

An action on a change goes in `onChange`:

```java file="com/example/interactivity/controls/MuteUI.java" region="mute" ins={6}
```

</details>

**A switch on an index.**
In `ChoicesUI`, keep the difficulty as an `IntegerSignal` from `0` to `2` instead of a text.

<details>
<summary>Solution</summary>

`valueIndex(...)` binds the index both ways:

```java fragment
new SwitchNode<String>(60, 60, 600, 60) {}
.option(...)
.values("Story", "Normal", "Hard")
.valueIndex(this.level)
.attach(card);
```

</details>
