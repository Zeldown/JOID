# Tutorial 3: Interactivity and State

In this part the settings screen comes alive: the language list reacts to clicks, the placeholders become real switches and a slider, the screen follows its state through signals, and the values are saved to disk so they survive a restart. It applies [Input and Callbacks](../concepts/input.md), [Signals and Reactivity](../concepts/signals.md), [Input Controls](../essentials/controls.md) and [Saving State](../essentials/saving-state.md) to the screen of part 2.

You start from the code of [Tutorial 2](layout.md). By the end you add three classes (`ToggleSwitchNode`, `VolumeSliderNode`, `SettingsStore`) and one line to `Main`.

## Step 1: react to a click with onClick

Start with the language list: give each row an `onClick` callback, as in [Input and Callbacks](../concepts/input.md), that prints its language:

```java
RectNode
.create(0, 0, 720, 52)
.color(Color.WHITE)
.onClick((node, mouseX, mouseY, clickType) -> System.out.println("Selected " + language))
.body(item -> {
	TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
})
.attach(list);
```

Run the program and click the rows: the console prints the language you clicked. Each lambda captures the `language` of its loop turn, so every row knows its own language. `color(...)` stays before `onClick(...)` in the chain, following the chaining rule of [Nodes and the Node Tree](../concepts/nodes.md): the setters of `RectNode` first, then the setters that every node shares.

## Step 2: keep the selection in a signal

The selection is state, so it goes in a signal ([Signals and Reactivity](../concepts/signals.md)), a field of `SettingsUI`:

```java
private final StringSignal language = StringSignal.of("English");
```

`"English"` is the default value of the `StringSignal` (`dev.joid.lib.utils.signal.impl.primitive`). Write the click into the signal, and give each row a dot that shows while its language is the selected one:

```java
RectNode
.create(0, 0, 720, 52)
.color(Color.WHITE)
.onClick((node, mouseX, mouseY, clickType) -> this.language.set(language))
.body(item -> {
	TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
	CircleNode.create(680, 18, 16).color(Theme.INK).visible(this.language.map(selected -> selected.equals(language))).attach(item);
})
.attach(list);
```

- `CircleNode.create(x, y, diameter)` (`dev.joid.lib.ui.node.impl.design.shape`) draws a filled circle.
- `this.language.map(...)` derives a signal from `language`: `true` for the row of the selected language. `visible(...)` follows it, so the dot shows and hides when `language` changes.
- `map` is the right tool here because the loop variable `language` decides the result. When the value depends only on signals and fields, you write the plain expression instead, as the next step does.

Click "Deutsch": the click sets the signal, the dot of English hides and the dot of Deutsch shows. You never told a row to repaint, and no node is rebuilt: you changed the state, and the values that depend on it followed.

![Clicking the Deutsch row moves the dot from English to Deutsch](../images/tutorial-language-click.gif "The click sets the signal; the dots follow it")

![A click sets the language signal and the two dots follow it through map; a slider writes the volume signal and the value text follows it](../images/diagram-tutorial-signals.png "Signals carry the state; the nodes follow them")

## Step 3: input controls

The controls handle the input and the value, and you draw them, as [Input Controls](../essentials/controls.md) showed with a checkbox. The screen needs two of them: an on/off switch and a slider.

### An on/off switch with CheckboxNode

`CheckboxNode` (`dev.joid.lib.ui.node.impl.structure.checkbox`) flips between checked and unchecked on each click. Here it is drawn as a switch: a track and a knob that moves to the right when checked:

```java
package com.example.settings;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;

public class ToggleSwitchNode extends CheckboxNode {

	protected ToggleSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static ToggleSwitchNode create(final double x, final double y, final double width, final double height) {
		return new ToggleSwitchNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double size = super.getHeight() - 8D;
		final double knobX = super.isChecked() ? super.getWidth() - size - 4D : 4D;
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), super.isChecked() ? Theme.INK : Theme.CARD);
		DrawUtils.SHAPE.drawRect(super.getX() + knobX, super.getY() + 4D, size, size, Color.WHITE);
	}

}
```

- Like the checkbox of Input Controls, the switch has a `protected` constructor and a static `create(...)` factory, like every node.
- `draw` runs every frame. It draws in the coordinate space of the parent, so the position of the node is `super.getX()`, `super.getY()`. `DrawUtils.SHAPE` (`dev.joid.lib.draw`) draws the shapes; see [Shapes](../drawing/shapes.md) for the others.

### A slider with IntegerSliderNode

A slider has two parts to draw. `IntegerSliderNode` (`dev.joid.lib.ui.node.impl.structure.slider.impl`) picks an integer by dragging a cursor along a track: you draw the track in `drawSlider`, and the cursor is a `SliderCursorNode` (`dev.joid.lib.ui.node.impl.structure.slider`) that draws itself in `drawCursor`, given to the slider with `cursor(...)`:

```java
package com.example.settings;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;

public class VolumeSliderNode extends IntegerSliderNode {

	protected VolumeSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Knob(height));
	}

	public static VolumeSliderNode create(final double x, final double y, final double width, final double height) {
		return new VolumeSliderNode(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY() + super.dh(2) - 3D, super.getWidth(), 6D, Theme.CARD);
	}

	private static final class Knob extends SliderCursorNode {

		private Knob(final double size) {
			super(size, size);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Theme.INK);
		}

	}

}
```

The cursor is a child of the slider: the slider centers it vertically and moves it along the track. Pressing anywhere on the track jumps the cursor under the mouse and starts dragging it; on release, the cursor snaps onto the position of the chosen value.

### Connecting the controls to signals

Add three more signals to `SettingsUI`, next to `language`:

```java
private final BooleanSignal music = BooleanSignal.of(true);
private final IntegerSignal volume = IntegerSignal.of(80);
private final BooleanSignal notifications = BooleanSignal.of(false);
```

Then replace the placeholder texts of the three rows with the controls:

```java
final RectNode music = this.row(flex, "Music", label);
ToggleSwitchNode.create(music.aw(-100), 18, 76, 36).signal(this.music).attach(music);
final RectNode volume = this.row(flex, "Volume", label).visible(this.music);
VolumeSliderNode.create(200, 24, 400, 24).values(0, 100, 80).signal(this.volume).attach(volume);
TextNode.create(0, 0, volume.aw(-24), volume.getHeight()).text(Text.create(this.volume.get() + " %", label, Align.END, Align.CENTER)).attach(volume);
```

```java
final RectNode notifications = this.row(flex, "Notifications", label);
ToggleSwitchNode.create(notifications.aw(-100), 18, 76, 36).signal(this.notifications).attach(notifications);
```

| Code | What it does |
| --- | --- |
| `signal(this.music)` | Binds the switch to the signal both ways, as in [Input Controls](../essentials/controls.md): the switch takes the value of the signal, each click writes the new state into it, and a value set elsewhere moves the switch. |
| `values(0, 100, 80)` | Gives the slider the integers from 0 to 100. The last number is the initial value; the binding then applies the value of the signal. |
| `signal(this.volume)` | Binds the slider the same way: each new value is written into the signal. |
| `visible(this.music)` | A boolean signal goes as it is to `visible(...)`: the Volume row shows only while the music is on. |
| `Text.create(this.volume.get() + " %", ...)` | A plain expression that reads a signal: JOID follows it and recomputes the text each time `volume` changes, so "80 %" follows the slider. |

Turn the music off: the Volume row disappears and the next sections move up, because a `FlexNode` gives no room to hidden children. Turn it on again and the row comes back with the slider where you left it.

![The Music switch is turned off and on, hiding then showing the Volume row, the slider is dragged from 80 % to 65 %, and the Notifications switch is turned on](../images/tutorial-controls.gif "Hidden rows give their room back; the value text follows the signal written by the slider")
## Step 4: persist the settings with a store

Close the window and start the program again: every value is back to its default. The signals live in the UI, and the UI starts from scratch on each launch. To keep them, move them into a `PERMANENT` store, the store context of [Saving State](../essentials/saving-state.md) that is saved to a file between runs. Create `SettingsStore`, with one signal per setting:

```java
package com.example.settings;

import com.google.gson.JsonObject;

import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;

@UIStoreData(id = "settings", context = StoreContext.PERMANENT)
public class SettingsStore extends UIStore {

	private final BooleanSignal music = BooleanSignal.of(true);
	private final IntegerSignal volume = IntegerSignal.of(80);
	private final StringSignal language = StringSignal.of("English");
	private final BooleanSignal notifications = BooleanSignal.of(false);

	@Override
	public void load(final JsonObject json) {
		if (json.has("music")) {
			this.music.set(json.get("music").getAsBoolean());
		}
		if (json.has("volume")) {
			this.volume.set(json.get("volume").getAsInt());
		}
		if (json.has("language")) {
			this.language.set(json.get("language").getAsString());
		}
		if (json.has("notifications")) {
			this.notifications.set(json.get("notifications").getAsBoolean());
		}
	}

	@Override
	public void save(final JsonObject json) {
		json.addProperty("music", this.music.get());
		json.addProperty("volume", this.volume.get());
		json.addProperty("language", this.language.get());
		json.addProperty("notifications", this.notifications.get());
	}

	public BooleanSignal getMusic() {
		return this.music;
	}

	public IntegerSignal getVolume() {
		return this.volume;
	}

	public StringSignal getLanguage() {
		return this.language;
	}

	public BooleanSignal getNotifications() {
		return this.notifications;
	}

}
```

- `@UIStoreData` is required on every store. `id` names the file: `config/store/settings.store` in the working directory (the `joid.config` system property changes the folder).
- `load(JsonObject)` runs once, when the store is created and its file exists; `save(JsonObject)` fills the JSON before each write. A value missing from the file keeps its default.
- The store needs a public constructor that accepts the arguments of `useStore`, here none: the default constructor.

In `SettingsUI`, replace the four signal fields with the store:

```java
private final SettingsStore settings = super.useStore(SettingsStore.class);
```

then replace `this.music` with `this.settings.getMusic()`, `this.volume` with `this.settings.getVolume()`, and so on. `useStore` creates the store the first time, reading its file, and returns the same instance afterwards; the signals of the store are followed exactly like the fields were.

JOID writes the permanent stores when a UI closes, so pressing `Escape` saves them. Closing the window while the screen is open does not close the UI: save the stores yourself when the loop ends. In `Main`, add `UIStoreHook.saveAll()` (`dev.joid.lib.ui.core.hook.store`) after the loop:

```java
private void loop() {
	while (!GLFW.glfwWindowShouldClose(this.window)) {
		if (GLFW.glfwGetWindowAttrib(this.window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
			GLFW.glfwWaitEvents();
			continue;
		}

		GLFW.glfwPollEvents();
		this.flushPendingKey();

		this.bridge.update();
		BridgeHandler.RENDER.get().clear(0.1F, 0.1F, 0.1F, 1F);
		this.bridge.draw();
		GLFW.glfwSwapBuffers(this.window);
	}

	UIStoreHook.saveAll();
	GLFW.glfwDestroyWindow(this.window);
	GLFW.glfwTerminate();
}
```

The rest of `Main` is unchanged; add `import dev.joid.lib.ui.core.hook.store.UIStoreHook;`.

> TIP: A store fits these settings because they are signals that other screens of the application could share. For a few plain fields of one UI class, such as the selected tab, `@UIProperty` of [Saving State](../essentials/saving-state.md) is simpler.

## The complete code

`ToggleSwitchNode`, `VolumeSliderNode` and `SettingsStore` are complete above. `SettingsUI`:

```java
package com.example.settings;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

@UIData(backgroundColor = "#18181B")
public final class SettingsUI extends UI {

	private static final List<String> LANGUAGES = Arrays.asList("English", "Français", "Deutsch", "Español");

	private final SettingsStore settings = super.useStore(SettingsStore.class);

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.getFont(), FontWeight.BOLD, 40F, Theme.INK);
		final TextInfo hint = TextInfo.create(Theme.getFont(), 18F, Theme.INK);
		final TextInfo section = TextInfo.create(Theme.getFont(), FontWeight.BOLD, 16F, Theme.INK).letterSpacing(0.08F);
		final TextInfo label = TextInfo.create(Theme.getFont(), 22F, Theme.INK);

		RectNode
		.create(560, 150, 800, 780)
		.color(Theme.CARD)
		.body(card -> {
			ResourceNode.create(40, 40, 48, 48).resource(Resource.of(new File("icons/settings.png"))).attach(card);
			TextNode.create(104, 64).text(Text.create("Settings", title)).anchorY(Align.CENTER).attach(card);
			TextNode.create(0, 40, card.aw(-40), 48).text(Text.create("Saved automatically", hint, Align.END, Align.CENTER)).attach(card);
			FlexNode
			.vertical(40, 112, 720)
			.margin(12)
			.body(flex -> {
				TextNode.create(0, 0, 0, 36).text(Text.create("AUDIO", section, Align.START, Align.END)).attach(flex);
				final RectNode music = this.row(flex, "Music", label);
				ToggleSwitchNode.create(music.aw(-100), 18, 76, 36).signal(this.settings.getMusic()).attach(music);
				final RectNode volume = this.row(flex, "Volume", label).visible(this.settings.getMusic());
				VolumeSliderNode.create(200, 24, 400, 24).values(0, 100, 80).signal(this.settings.getVolume()).attach(volume);
				TextNode.create(0, 0, volume.aw(-24), volume.getHeight()).text(Text.create(this.settings.getVolume().get() + " %", label, Align.END, Align.CENTER)).attach(volume);
				TextNode.create(0, 0, 0, 36).text(Text.create("GENERAL", section, Align.START, Align.END)).attach(flex);
				final RectNode notifications = this.row(flex, "Notifications", label);
				ToggleSwitchNode.create(notifications.aw(-100), 18, 76, 36).signal(this.settings.getNotifications()).attach(notifications);
				TextNode.create(0, 0, 0, 36).text(Text.create("LANGUAGE", section, Align.START, Align.END)).attach(flex);
				FlexNode
				.vertical(0, 0, 720)
				.margin(8)
				.body(list -> {
					for (final String language : SettingsUI.LANGUAGES) {
						RectNode
						.create(0, 0, 720, 52)
						.color(Color.WHITE)
						.onClick((node, mouseX, mouseY, clickType) -> this.settings.getLanguage().set(language))
						.body(item -> {
							TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
							CircleNode.create(680, 18, 16).color(Theme.INK).visible(this.settings.getLanguage().map(selected -> selected.equals(language))).attach(item);
						})
						.attach(list);
					}
				})
				.attach(flex);
			})
			.attach(card);
		})
		.attach(this);
	}

	private RectNode row(final Node parent, final String name, final TextInfo info) {
		return RectNode
		.create(0, 0, 720, 72)
		.color(Color.WHITE)
		.body(row -> {
			TextNode.create(24, row.dh(2)).text(Text.create(name, info)).anchorY(Align.CENTER).attach(row);
		})
		.attach(parent);
	}

}
```

## What you should see

The rows hold working controls: two switches, a slider with its value on the right, and a clickable language list. Change a few values, close the window and start the program again: the screen opens with your values. The file `config/store/settings.store` holds them as JSON, for example `{"music":true,"volume":65,"language":"Deutsch","notifications":true}`.

![The settings card with the Music switch on, the Volume slider at 65 %, the Notifications switch on and the dot on Deutsch](../images/tutorial-interactivity.png "These values come back from config/store/settings.store on the next launch")

## Recap

- Callbacks such as `onClick` are lambdas registered on a node; an input callback consumes the event it handles.
- A `Signal<T>` holds state. A setter that receives a signal, a `map(...)` of a signal or an expression that reads signals follows them; a boolean signal goes as it is to `visible(...)`.
- Input controls are abstract: you subclass `CheckboxNode`, `IntegerSliderNode` and the others, and only draw them. `signal(...)` binds a control to a signal both ways.
- A `PERMANENT` store keeps signals between runs; `UIStoreHook.saveAll()` saves it when the application exits.

## See also

- Next: [Tutorial 4: Polish](polish.md) gives the screen its final look.
- [Signals and Reactivity](../concepts/signals.md)
- [Input Controls](../essentials/controls.md)
- [Saving State](../essentials/saving-state.md)
- [SliderNode](../nodes/input/slider.md)
- [Stores](../state/stores.md)