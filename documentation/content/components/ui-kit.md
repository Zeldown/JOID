# Building a UI Kit

JOID is design-neutral: its interactive components own the behavior (state, input, values, callbacks, signals) and draw nothing. You draw each of them once, in a small subclass, and these subclasses form your UI kit. Your screens only use the kit, so the same screen code takes a completely different look when you switch to another kit.

![The same settings panel drawn by a flat light kit and by a rounded dark kit](../images/uikit-side-by-side.png "The same screen code and the same signals: only the import line selects the kit")

## What a component gives you and what you draw

Every component follows the [custom node](../nodes/custom-nodes.md) contract: a `protected` constructor and a public static `create(...)` factory. The component class handles the input and keeps the state; your subclass reads that state and draws it.

| Component | It handles | You draw in |
| --- | --- | --- |
| [SliderNode](../nodes/input/slider.md) (`IntegerSliderNode`, `DoubleSliderNode`, `StringSliderNode`) | Values, dragging, the selected value, `signal`, `onChange` | `drawSlider(mouseX, mouseY)` for the track, and `drawCursor(mouseX, mouseY)` in a `SliderCursorNode` subclass installed with `cursor(...)` |
| [CheckboxNode](../nodes/input/checkbox.md) | The checked state, clicks, `signal`, `onChange` | `draw(mouseX, mouseY)`, reading `isChecked()` |
| [ToggleNode](../nodes/input/toggle.md) | The side, the value of each side, clicks, `signal`, `onChange` | `draw(mouseX, mouseY)`, reading `isToggle()` |
| [SwitchNode](../nodes/input/switch.md) | The list of states, the current one, `signal`, `onChange`, rebuilding when the states change | `init(UI)`: one child per state that follows `getState()` and calls `state(...)` on click |
| [SelectorNode](../nodes/input/selector.md) | The values, opening, closing, the layout of the options, the selection, `signal`, `onChange` | `option(value)`, which returns the node of one option, and `drawBackground(mouseX, mouseY)` |
| [ChartNode](../nodes/data/chart.md) | Labels, series, the scale (`getMin`, `getMax`), loading | `draw(mouseX, mouseY)` |
| [RadarChartNode](../nodes/data/radar-chart.md) | Axes, values, the scale, loading | `draw(mouseX, mouseY)` |

What every kit class uses while drawing:

- `super.getX()`, `super.getY()`, `super.getWidth()`, `super.getHeight()`, `super.dw(2)` (half the width) and `super.dh(2)`: draw relative to the node, never at fixed canvas positions.
- `super.hoverValue(1F)`: the hover progress of the node, from `0F` to `1F`, animated over `hoverDuration` (200 ms by default). Blend colors with it: `Theme.LINE.to(Theme.INK, super.hoverValue(1F))`.
- The state of the component: `isChecked()`, `getProgress()`, `isActive()`, `isSelected(node)`, `getState()`.
- [DrawUtils](../drawing/draw-utils.md): `DrawUtils.SHAPE` (rectangles, rounded rectangles, circles, lines, gradients through `Color.toGradient`) and `DrawUtils.TEXT`.

## Fonts and colors in a Theme

A kit keeps its colors and its text styles in one `Theme` class, used by every component. Colors are plain constants. Text styles need a loaded font, so the `Theme` loads it in a `load()` method that the application calls once, after JOID:

```java
package kit.flat;

import java.io.File;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final Color INK     = Color.decode("#999999");
	public static final Color LINE    = Color.decode("#DDDDDD");
	public static final Color HOVER   = Color.decode("#F4F4F4");
	public static final Color SURFACE = Color.decode("#FFFFFF");

	private static TextInfo text;
	private static TextInfo title;
	private static TextInfo inverse;

	private Theme() {}

	public static void load() {
		final MsdfFont font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();
		Theme.text = TextInfo.create(font, 18F, Theme.INK);
		Theme.title = TextInfo.create(font, FontWeight.BOLD, 26F, Theme.INK);
		Theme.inverse = TextInfo.create(font, 18F, Theme.SURFACE);
	}

	public static TextInfo getText() {
		return Theme.text;
	}

	public static TextInfo getTitle() {
		return Theme.title;
	}

	public static TextInfo getInverse() {
		return Theme.inverse;
	}

}
```

```java
JOID.inst().load();
Theme.load();
```

- `load()` runs from your `Main`, after `JOID.inst().load()`: the bridges and the settings of JOID (dev mode, configuration folder) are in place, and you control when the font loads. `join()` waits for the font; the first load of a font file generates its atlas, later loads read a cache.
- The components read the styles with `Theme.getText()`, `Theme.getTitle()` and `Theme.getInverse()`, when they build their nodes or draw.
- Do not load the font in a `static final` field: the font would load whenever the class is first touched, possibly before JOID, and a missing file would fail the class initialization instead of a call you control.

See [Adding Your Own Fonts](../fonts/adding-fonts.md) for the other ways to load a font.

## A complete kit: kit.flat

The flat kit is minimal and rectangular: a white surface, gray ink, hairline borders. It has one class per component, plus a `Panel` and a `Label` so that a whole screen comes from the kit. All the classes are in the package `kit.flat`.

### Panel and Label

The card that holds a screen, with its title, and the text of a row:

```java
package kit.flat;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.ui.node.Node;

public class Panel extends Node {

	private final Text title;

	protected Panel(final double x, final double y, final double width, final double height, final String title) {
		super(x, y, width, height);
		this.title = Text.create(title, Theme.getTitle());
	}

	public static Panel create(final double x, final double y, final double width, final double height, final String title) {
		return new Panel(x, y, width, height, title);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Theme.SURFACE);
		DrawUtils.TEXT.drawText(super.getX() + 40D, super.getY() + 32D, this.title);
		DrawUtils.SHAPE.drawRect(super.getX() + 40D, super.getY() + 84D, super.getWidth() - 80D, 1D, Theme.LINE);
	}

}
```

```java
package kit.flat;

import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class Label extends TextNode {

	protected Label(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static Label create(final double x, final double y, final double width, final double height, final String text) {
		return new Label(x, y, width, height).text(Text.create(text, Theme.getText(), Align.START, Align.CENTER));
	}

}
```

`Label.create` passes its text to `Text.create`, which follows the signals that the text reads. A method that only passes its parameter on keeps the expression of its caller, so `Label.create(500, 110, 60, 44, this.volume.get() + " %")` shows the new volume each time the signal changes.

### Slider

`drawSlider` draws the track and fills it up to `getProgress()`, the position of the cursor on its travel from `0F` to `1F`. The cursor is a `SliderCursorNode`: the slider centers it vertically, moves it along the track and snaps it onto the chosen value on release. The cursor counts as hovered during the whole drag, so its halo stays even when the pointer leaves it.

```java
package kit.flat;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;

public class Slider extends IntegerSliderNode {

	protected Slider(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Knob(16D));
	}

	public static Slider create(final double x, final double y, final double width, final double height) {
		return new Slider(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		final double centerY = super.getY() + super.dh(2);
		DrawUtils.SHAPE.drawRect(super.getX(), centerY - 1D, super.getWidth(), 2D, Theme.LINE);
		DrawUtils.SHAPE.drawRect(super.getX(), centerY - 1D, super.getWidth() * super.getProgress(), 2D, Theme.INK);
	}

	private static final class Knob extends SliderCursorNode {

		private Knob(final double size) {
			super(size, size);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			final double halo = super.hoverValue(6F);
			DrawUtils.SHAPE.drawRect(super.getX() - halo, super.getY() - halo, super.getWidth() + halo * 2D, super.getHeight() + halo * 2D, Theme.LINE);
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Theme.INK);
		}

	}

}
```

### Checkbox

The size is a parameter of `create`: each kit decides the width it needs for that height.

```java
package kit.flat;

import javax.vecmath.Vector2d;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;

public class Checkbox extends CheckboxNode {

	protected Checkbox(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static Checkbox create(final double x, final double y, final double size) {
		return new Checkbox(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double x = super.getX();
		final double y = super.getY();
		final double size = super.getWidth();
		if (!super.isChecked()) {
			DrawUtils.SHAPE.drawRect(x, y, size, size, Theme.LINE.to(Theme.INK, super.hoverValue(1F)));
			DrawUtils.SHAPE.drawRect(x + 2D, y + 2D, size - 4D, size - 4D, Theme.SURFACE);
			return;
		}

		DrawUtils.SHAPE.drawRect(x, y, size, size, Theme.INK);
		DrawUtils.SHAPE.drawLine(Theme.SURFACE, 2.5F, new Vector2d(x + size * 0.25D, y + size * 0.52D), new Vector2d(x + size * 0.43D, y + size * 0.7D), new Vector2d(x + size * 0.76D, y + size * 0.32D));
	}

}
```

### Switch

`SwitchNode` builds its children in `init(UI)`, and again each time its list of states changes. Each segment is a `RectNode` whose color and text follow `getState()` through `Signal.from(() -> ...)`, so a segment changes without being rebuilt, and its click calls `state(...)`.

```java
package kit.flat;

import java.util.List;

import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.Signal;

public class Switch extends SwitchNode {

	protected Switch(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static Switch create(final double x, final double y, final double width, final double height) {
		return new Switch(x, y, width, height);
	}

	@Override
	public void init(final UI ui) {
		final List<String> states = super.getStateList().get();
		final double width = super.getWidth() / states.size();
		for (int i = 0; i < states.size(); i++) {
			final String state = states.get(i);
			RectNode
			.create(i * width, 0, width, super.getHeight())
			.color(Signal.from(() -> super.getState().equals(state) ? Theme.INK : Theme.SURFACE))
			.borderColor(Theme.LINE)
			.borderStroke(1D)
			.onClick((node, mouseX, mouseY, clickType) -> super.state(state))
			.body(segment -> {
				TextNode
				.create(0, 0, width, super.getHeight())
				.text(Signal.from(() -> Text.create(state, super.getState().equals(state) ? Theme.getInverse() : Theme.getText(), Align.CENTER, Align.CENTER)))
				.attach(segment);
			})
			.attach(this);
		}
	}

}
```

`Signal.from(() -> ...)` follows the signals its lambda reads (`getState()` reads the states and the index of the switch). The loop variable `state` decides the result, which a plain expression cannot follow: the lambda form is the one to use here.

### Selector

`Selector` extends `SelectorNode<String>`: its values are strings. `values(...)` calls `option(value)` once per value, and the selector sizes and places the nodes that `option` returns. Each option is a `RectNode` with a hover color, its text, and a layer that draws the arrow of the selected option. `drawBackground` outlines the selector, darker while the list is open.

```java
package kit.flat;

import javax.vecmath.Vector2d;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.utils.align.Align;

public class Selector extends SelectorNode<String> {

	protected Selector(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static Selector create(final double x, final double y, final double width, final double height) {
		return new Selector(x, y, width, height);
	}

	@Override
	protected Node option(final String value) {
		return RectNode
		.create(0, 0, super.getDefaultWidth(), super.getDefaultHeight())
		.color(Theme.SURFACE)
		.hoveredColor(Theme.HOVER)
		.body(option -> {
			TextNode.create(16, 0, option.aw(-16), option.getHeight()).text(Text.create(value, Theme.getText(), Align.START, Align.CENTER)).attach(option);
			option.layer((mouseX, mouseY) -> this.drawArrow(option));
		});
	}

	@Override
	public void drawBackground(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), super.isActive() ? Theme.INK : Theme.LINE);
	}

	private void drawArrow(final Node option) {
		if (!super.isSelected(option)) {
			return;
		}

		final double x = option.getX() + option.aw(-28);
		final double y = option.getY() + option.dh(2);
		final double tip = super.isActive() ? -4D : 4D;
		DrawUtils.SHAPE.drawLine(Theme.INK, 2F, new Vector2d(x - 6D, y - tip), new Vector2d(x, y + tip), new Vector2d(x + 6D, y - tip));
	}

}
```

## Using the kit

A settings panel built only from the kit. The signals hold the values; the screen code says what to show, never how it looks:

```java
package app.flat;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;
import kit.flat.*;

public final class SettingsUI extends UI {

	private final IntegerSignal volume = IntegerSignal.of(65);
	private final StringSignal quality = StringSignal.of("High");
	private final StringSignal language = StringSignal.of("English");
	private final BooleanSignal subtitles = BooleanSignal.of(true);

	@Override
	public void init() {
		Panel
		.create(660, 270, 600, 400, "Settings")
		.body(panel -> {
			Label.create(40, 110, 160, 44, "Volume").attach(panel);
			Slider.create(200, 110, 280, 44).values(0, 100, 65).signal(this.volume).attach(panel);
			Label.create(500, 110, 60, 44, this.volume.get() + " %").attach(panel);
			Label.create(40, 180, 160, 44, "Subtitles").attach(panel);
			Checkbox.create(200, 188, 28).signal(this.subtitles).attach(panel);
			Label.create(40, 250, 160, 44, "Quality").attach(panel);
			Switch.create(200, 250, 360, 44).states("Low", "Medium", "High").signal(this.quality).attach(panel);
			Label.create(40, 320, 160, 44, "Language").attach(panel);
			Selector.create(200, 320, 360, 44).values("English", "English", "Français", "Deutsch", "Español").signal(this.language).attach(panel);
		})
		.attach(this);
	}

}
```

`signal(...)` binds each control to its signal both ways: the control takes the value of the signal, writes each change of the user into it, and follows the values that other code sets. The first argument of `values(...)` is the initial value, which the binding replaces with the value of the signal. The volume label follows `this.volume.get()`, so it changes only when the slider writes a new value.

![The cursor drags the volume slider, unchecks Subtitles, picks Medium and selects Deutsch in both kits at once](../images/uikit-use.gif "The same clicks on both kits: the components behave the same, only the drawing differs")

## A second kit: kit.round

The round kit has the same class names and the same factories, in the package `kit.round`: a dark surface, rounded pills, a white accent and soft glows. Its `Theme` adds a `glow` helper, drawn as stacked translucent rounded rectangles:

```java
package kit.round;

import java.io.File;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final Color INK     = Color.decode("#FFFFFF");
	public static final Color TRACK   = Color.decode("#52525B");
	public static final Color MUTED   = Color.decode("#A1A1AA");
	public static final Color SURFACE = Color.decode("#3F3F46");

	private static TextInfo text;
	private static TextInfo title;
	private static TextInfo inverse;

	private Theme() {}

	public static void load() {
		final MsdfFont font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();
		Theme.text = TextInfo.create(font, 18F, Theme.MUTED);
		Theme.title = TextInfo.create(font, FontWeight.BOLD, 28F, Theme.INK);
		Theme.inverse = TextInfo.create(font, FontWeight.BOLD, 18F, Theme.SURFACE);
	}

	public static void glow(final double x, final double y, final double width, final double height, final float radius, final double spread) {
		for (int i = 8; i >= 1; i--) {
			final double grow = spread * i / 8D;
			DrawUtils.SHAPE.drawRoundedRect(x - grow, y - grow, width + grow * 2D, height + grow * 2D, Theme.INK.copyAlpha(0.03F), radius + (float) grow);
		}
	}

	public static TextInfo getText() {
		return Theme.text;
	}

	public static TextInfo getTitle() {
		return Theme.title;
	}

	public static TextInfo getInverse() {
		return Theme.inverse;
	}

}
```

Its slider draws a rounded track filled with the accent and a round cursor whose glow grows on hover:

```java
package kit.round;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;

public class Slider extends IntegerSliderNode {

	protected Slider(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Knob(24D));
	}

	public static Slider create(final double x, final double y, final double width, final double height) {
		return new Slider(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		final double trackY = super.getY() + super.dh(2) - 4D;
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), trackY, super.getWidth(), 8D, Theme.TRACK, 4F);
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), trackY, super.getWidth() * super.getProgress(), 8D, Theme.INK, 4F);
	}

	private static final class Knob extends SliderCursorNode {

		private Knob(final double size) {
			super(size, size);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			final float focus = super.hoverValue(1F);
			final double centerX = super.getX() + super.dw(2);
			final double centerY = super.getY() + super.dh(2);
			DrawUtils.SHAPE.drawCircle(centerX, centerY, Theme.INK.copyAlpha(0.12F + 0.12F * focus), 16D + 4D * focus);
			DrawUtils.SHAPE.drawCircle(centerX, centerY, Theme.INK, 11D);
		}

	}

}
```

Its checkbox is a pill switch: the same `CheckboxNode` state, drawn as a track and a knob. `create` makes it 1.8 times as wide as it is high:

```java
package kit.round;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;

public class Checkbox extends CheckboxNode {

	protected Checkbox(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static Checkbox create(final double x, final double y, final double size) {
		return new Checkbox(x, y, size * 1.8D, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double x = super.getX();
		final double y = super.getY();
		final float radius = (float) super.dh(2);
		final double knobY = y + super.dh(2);
		if (!super.isChecked()) {
			DrawUtils.SHAPE.drawRoundedRect(x, y, super.getWidth(), super.getHeight(), Theme.TRACK.to(Theme.MUTED, super.hoverValue(0.3F)), radius);
			DrawUtils.SHAPE.drawCircle(x + radius, knobY, Theme.MUTED, radius - 4D);
			return;
		}

		Theme.glow(x, y, super.getWidth(), super.getHeight(), radius, 10D);
		DrawUtils.SHAPE.drawRoundedRect(x, y, super.getWidth(), super.getHeight(), Theme.INK, radius);
		DrawUtils.SHAPE.drawCircle(x + super.getWidth() - radius, knobY, Theme.SURFACE, radius - 4D);
	}

}
```

`Panel`, `Label`, `Switch` and `Selector` keep the structure and the API of the flat classes and change only what they draw: a rounded glowing panel, pill segments with `RoundedNodeEffect`, rounded options with a glow around the open list.

## Switching kits

### By import

When both kits have the same class names and the same factories, the import line is the only difference:

```java
import kit.flat.*;
```

```java
import kit.round.*;
```

The rest of `SettingsUI` does not change. This is the simplest way when a project uses one kit at a time.

### With a factory

To choose the kit at runtime (a theme setting, a light and a dark mode), put the factories behind an interface. The methods return the base types of JOID, so screens keep the whole API of each component:

```java
package kit;

import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;

public interface Kit {

	public CheckboxNode checkbox(final double x, final double y, final double size);
	public IntegerSliderNode slider(final double x, final double y, final double width, final double height);
	public SwitchNode switcher(final double x, final double y, final double width, final double height);
	public SelectorNode<String> selector(final double x, final double y, final double width, final double height);

}
```

Each kit implements it with its own classes:

```java
package kit.flat;

import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import kit.Kit;

public final class FlatKit implements Kit {

	@Override
	public CheckboxNode checkbox(final double x, final double y, final double size) {
		return Checkbox.create(x, y, size);
	}

	@Override
	public IntegerSliderNode slider(final double x, final double y, final double width, final double height) {
		return Slider.create(x, y, width, height);
	}

	@Override
	public SwitchNode switcher(final double x, final double y, final double width, final double height) {
		return Switch.create(x, y, width, height);
	}

	@Override
	public SelectorNode<String> selector(final double x, final double y, final double width, final double height) {
		return Selector.create(x, y, width, height);
	}

}
```

`kit.round.RoundKit` is the same class with the `kit.round` classes. A screen then receives a `Kit`:

```java
package app.factory;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;
import kit.Kit;

public final class SettingsUI extends UI {

	private final Kit kit;

	private final IntegerSignal volume = IntegerSignal.of(65);
	private final StringSignal quality = StringSignal.of("High");
	private final StringSignal language = StringSignal.of("English");
	private final BooleanSignal subtitles = BooleanSignal.of(true);

	public SettingsUI(final Kit kit) {
		this.kit = kit;
	}

	@Override
	public void init() {
		this.kit.slider(860, 380, 280, 44).values(0, 100, 65).signal(this.volume).attach(this);
		this.kit.checkbox(860, 458, 28).signal(this.subtitles).attach(this);
		this.kit.switcher(860, 520, 360, 44).states("Low", "Medium", "High").signal(this.quality).attach(this);
		this.kit.selector(860, 590, 360, 44).values("English", "English", "Français", "Deutsch", "Español").signal(this.language).attach(this);
	}

}
```

Open it with `JOID.open(new SettingsUI(new FlatKit()))` or `JOID.open(new SettingsUI(new RoundKit()))`. A method that a kit class adds on top of JOID is not reachable through the base type: keep the kit classes to the API of JOID, or add the method to the interface.

## Reference

What a kit class reads and overrides, per component:

| Component | Override | Read while drawing |
| --- | --- | --- |
| `SliderNode<O>` | `drawSlider(double, double)`; `SliderCursorNode.drawCursor(double, double)` | `getProgress()` (`0F` to `1F`), `getValue()`, `getCursor()`, `hoverValue(float)` of the cursor |
| `CheckboxNode` | `draw(double, double)` | `isChecked()`, `hoverValue(float)` |
| `ToggleNode<F, S>` | `draw(double, double)` | `isToggle()`, `getValue()` |
| `SwitchNode` | `init(UI)` | `getStateList()`, `getState()`; `state(String)` or `index(int)` on click |
| `SelectorNode<V>` | `option(V)`, `drawBackground(double, double)` | `isActive()`, `isSelected(Node)`, `getValue()`, `getDefaultWidth()`, `getDefaultHeight()` |
| `ChartNode`, `RadarChartNode` | `draw(double, double)` | the data, the labels and the scale of the chart |

Tips for a kit:

| Tip | Why |
| --- | --- |
| One `Theme` class | The colors, gradients and text styles in one place: a new palette is one file. |
| Draw from the size of the node | `getWidth()`, `getHeight()`, `dw(2)` and `dh(2)` make every size work. |
| Sizes as parameters | Take the sizes in `create(...)`; derive the other dimension when a kit needs a shape, as the round `Checkbox` does with `size * 1.8D`. |
| Hover with `hoverValue` | `hoverValue(1F)` animates from `0F` to `1F` when the pointer enters the node and back when it leaves; set the speed with `hoverDuration(...)` and the curve with `hoverEquation(...)`. |
| Keep the behavior in JOID | Override the drawing hooks and leave the input methods alone: dragging, clicking, selecting and the callbacks work the same in every kit. |
| Same names in every kit | Same class names and same `create(...)` signatures keep switching kits a one-line change. |

## Pitfalls

- A font loaded in a `static final` field of the `Theme` loads whenever the class is first touched: load it in a `load()` method called after `JOID.inst().load()`.
- A setter of `Node` in the middle of a chain returns a `Node`: call the setters of the component first (`values`, `states`, `signal`), then `attach`.
- `signal(...)` needs a writable signal: a `ComputedSignal` (a `map(...)` or a `Signal.from(...)`) throws an `IllegalArgumentException`. Pass a computed signal to a setter such as `value(...)` instead, which follows it one way.
- Draw relative to `super.getX()` and `super.getY()`: a kit class drawn at fixed canvas positions breaks as soon as it moves.

## See also

- [Component Catalog](overview.md)
- [Custom Nodes](../nodes/custom-nodes.md)
- [SliderNode](../nodes/input/slider.md)
- [SwitchNode](../nodes/input/switch.md)
- [SelectorNode](../nodes/input/selector.md)
- [Adding Your Own Fonts](../fonts/adding-fonts.md)