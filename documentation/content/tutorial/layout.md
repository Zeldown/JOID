# Tutorial 2: Building the Layout

In this part you build the static layout of the settings screen: a card centered on the canvas, a header with an icon and a title, and columns of rows laid out by `FlexNode`. Nothing reacts to the mouse yet; the values on the right of the rows are placeholders that part 3 replaces with real controls.

You start from the code of [Tutorial 1](setup.md). `Main` and `AppUIBridge` do not change in this part.

## Step 1: the card with RectNode

Every visible element of a UI is a node. `RectNode` (`dev.joid.lib.ui.node.impl.design.shape`) draws a filled rectangle; it is the usual base for cards, rows and buttons. First give `Theme` the two colors of the screen, next to the font:

```java
public static final Color INK  = Color.decode("#999999");
public static final Color CARD = Color.decode("#DDDDDD");
```

Then replace the body of `init()` in `SettingsUI`:

```java
@Override
public void init() {
	RectNode
	.create(560, 150, 800, 780)
	.color(Theme.CARD)
	.attach(this);
}
```

- `create(x, y, width, height)` takes canvas units. A card 800 wide at x = 560 is horizontally centered ((1920 - 800) / 2), and 780 high at y = 150 is vertically centered ((1080 - 780) / 2).
- `color(...)` sets the fill. `Color.decode` reads `#RRGGBB` and other formats; see [Colors and Gradients](../styling/colors.md).
- `attach(this)` adds the card at the top level of the UI.

![A light gray card centered on a near-black background](../images/tutorial-layout-card.png "Step 1: one RectNode, centered on the canvas")

## Step 2: children with body

A node holds children. `body(...)` runs a lambda right away with the node, so you build its children inline, and `attach(card)` adds each child to the card. Children are placed relative to their parent: (40, 40) inside the card is 40 units from its top-left corner, wherever the card is.

Create the text styles at the start of `init()`:

```java
final TextInfo title = TextInfo.create(Theme.getFont(), FontWeight.BOLD, 40F, Theme.INK);
final TextInfo hint = TextInfo.create(Theme.getFont(), 18F, Theme.INK);
```

Then add the header to the card:

```java
RectNode
.create(560, 150, 800, 780)
.color(Theme.CARD)
.body(card -> {
	ResourceNode.create(40, 40, 48, 48).resource(Resource.of(new File("icons/settings.png"))).attach(card);
	TextNode.create(104, 64).text(Text.create("Settings", title)).anchorY(Align.CENTER).attach(card);
	TextNode.create(0, 40, card.aw(-40), 48).text(Text.create("Saved automatically", hint, Align.END, Align.CENTER)).attach(card);
})
.attach(this);
```

- A `TextInfo` (`dev.joid.lib.font.dto`) is the style of a text: font, weight, size, color, spacing. Create each style once and reuse it; see [Text and TextInfo](../text/text-and-textinfo.md).
- `ResourceNode` (`dev.joid.lib.ui.node.impl.design.resource`) displays an image. `Resource.of(new File(...))` (`dev.joid.lib.resource`) loads it in the background; until it is ready, the node draws a pulsing placeholder. A URL, a stream or a file of your jar work too; see [Resources](../resources/resources.md).
- `TextNode.create(x, y)` creates a text node without a size: it takes the size of its text.
- **Anchors** decide which point of a node sits at its `x` and `y`; by default it is the top-left corner. `anchorY(Align.CENTER)` puts the vertical center of the title on y = 64, the center of the 48-unit icon (40 + 24), so both line up whatever the font size.
- `TextNode.create(x, y, width, height)` gives the text a box, and `Text.create(text, info, horizontalAlign, verticalAlign)` aligns the text inside it: the hint ends at the right edge of a box 40 units narrower than the card.
- `card.aw(-40)` is the width of the card plus -40, that is 760. The relative helpers (`aw`, `ah`, `dw`, `dh`, `mw`, `mh`) read the size of the node you call them on, so you never write a size twice; see [Node Fundamentals](../nodes/node-fundamentals.md).

![The card with a gear icon, the bold title Settings and the hint Saved automatically on the right](../images/tutorial-layout-header.png "Step 2: three children placed relative to the card")

## Step 3: rows with FlexNode

Placing every row by hand means computing each `y` yourself. `FlexNode` (`dev.joid.lib.ui.node.impl.structure.flex`) does it: `FlexNode.vertical(x, y, width)` stacks its children from top to bottom with a gap (`margin`) and grows to fit them. Each child is created at (0, 0); the flex moves it into its slot.

All the rows look the same, so a small method builds one and returns it, ready to receive the control on its right:

```java
private RectNode row(final Node parent, final String name, final TextInfo info) {
	return RectNode
	.create(0, 0, 720, 72)
	.color(Color.WHITE)
	.body(row -> {
		TextNode.create(24, row.dh(2)).text(Text.create(name, info)).anchorY(Align.CENTER).attach(row);
	})
	.attach(parent);
}
```

`row.dh(2)` is half the height of the row: with `anchorY(Align.CENTER)`, the label is vertically centered. `attach(...)` returns the node typed as what you assign it to, here a `RectNode`.

Two more styles go next to `title` and `hint`, for the section titles and the labels:

```java
final TextInfo section = TextInfo.create(Theme.getFont(), FontWeight.BOLD, 16F, Theme.INK).letterSpacing(0.08F);
final TextInfo label = TextInfo.create(Theme.getFont(), 22F, Theme.INK);
```

Then the column, inside the `body` of the card, after the header:

```java
FlexNode
.vertical(40, 112, 720)
.margin(12)
.body(flex -> {
	TextNode.create(0, 0, 0, 36).text(Text.create("AUDIO", section, Align.START, Align.END)).attach(flex);
	final RectNode music = this.row(flex, "Music", label);
	TextNode.create(0, 0, music.aw(-24), music.getHeight()).text(Text.create("On", label, Align.END, Align.CENTER)).attach(music);
	final RectNode volume = this.row(flex, "Volume", label);
	TextNode.create(0, 0, volume.aw(-24), volume.getHeight()).text(Text.create("80 %", label, Align.END, Align.CENTER)).attach(volume);
	TextNode.create(0, 0, 0, 36).text(Text.create("GENERAL", section, Align.START, Align.END)).attach(flex);
	final RectNode notifications = this.row(flex, "Notifications", label);
	TextNode.create(0, 0, notifications.aw(-24), notifications.getHeight()).text(Text.create("Off", label, Align.END, Align.CENTER)).attach(notifications);
})
.attach(card);
```

- A section title is a text node 36 units high whose text sits at the bottom of its box, which leaves some space above each section. A width of `0` follows the text.
- The value of each row is a text node as large as the row minus 24 units, with its text aligned to the end.
- The children are laid out in the order you attach them. A hidden child takes no room: the next ones move up to fill its place, which part 3 uses.

![The card with its header, the AUDIO section with the Music and Volume rows and the GENERAL section with the Notifications row](../images/tutorial-layout-rows.png "Step 3: a FlexNode stacks the section titles and the rows")

## Step 4: a nested list

The language list is a second `FlexNode` inside the first one. Add a constant to `SettingsUI`:

```java
private static final List<String> LANGUAGES = Arrays.asList("English", "Français", "Deutsch", "Español");
```

then build one row per language, after the Notifications row:

```java
TextNode.create(0, 0, 0, 36).text(Text.create("LANGUAGE", section, Align.START, Align.END)).attach(flex);
FlexNode
.vertical(0, 0, 720)
.margin(8)
.body(list -> {
	for (final String language : SettingsUI.LANGUAGES) {
		RectNode
		.create(0, 0, 720, 52)
		.color(Color.WHITE)
		.body(item -> {
			TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
		})
		.attach(list);
	}
})
.attach(flex);
```

A `FlexNode` is a node like any other: the outer column sees the inner list as one child, 232 units high (four rows of 52 and three gaps of 8). Part 3 marks the selected language.

## The complete code

`Theme`:

```java
package com.example.settings;

import java.io.File;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;

public final class Theme {

	public static final Color INK  = Color.decode("#999999");
	public static final Color CARD = Color.decode("#DDDDDD");

	private static MsdfFont font;

	private Theme() {}

	public static void load() {
		Theme.font = MsdfFontLoader.load(new File("fonts/Montserrat-Regular.ttf"), new File("fonts/Montserrat-Bold.ttf")).join();
	}

	public static MsdfFont getFont() {
		return Theme.font;
	}

}
```

`SettingsUI`:

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

@UIData(backgroundColor = "#18181B")
public final class SettingsUI extends UI {

	private static final List<String> LANGUAGES = Arrays.asList("English", "Français", "Deutsch", "Español");

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
				TextNode.create(0, 0, music.aw(-24), music.getHeight()).text(Text.create("On", label, Align.END, Align.CENTER)).attach(music);
				final RectNode volume = this.row(flex, "Volume", label);
				TextNode.create(0, 0, volume.aw(-24), volume.getHeight()).text(Text.create("80 %", label, Align.END, Align.CENTER)).attach(volume);
				TextNode.create(0, 0, 0, 36).text(Text.create("GENERAL", section, Align.START, Align.END)).attach(flex);
				final RectNode notifications = this.row(flex, "Notifications", label);
				TextNode.create(0, 0, notifications.aw(-24), notifications.getHeight()).text(Text.create("Off", label, Align.END, Align.CENTER)).attach(notifications);
				TextNode.create(0, 0, 0, 36).text(Text.create("LANGUAGE", section, Align.START, Align.END)).attach(flex);
				FlexNode
				.vertical(0, 0, 720)
				.margin(8)
				.body(list -> {
					for (final String language : SettingsUI.LANGUAGES) {
						RectNode
						.create(0, 0, 720, 52)
						.color(Color.WHITE)
						.body(item -> {
							TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
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

The card has its header, then three sections: **AUDIO** with the Music and Volume rows, **GENERAL** with the Notifications row, **LANGUAGE** with four rows. Each row shows its label on the left and a placeholder value on the right.

![A light gray card with a gear icon and the title Settings, AUDIO, GENERAL and LANGUAGE sections, white rows Music On, Volume 80 % and Notifications Off, and four white language rows](../images/tutorial-layout.png "The static layout: a card, a header and two nested FlexNode columns")

> TIP: In dev mode, press `F3`, click Inspect and hover the rows: the inspector shows the class, the bounds and the place in the tree of each node. See [Developer Tools](../getting-started/dev-tools.md).

## Recap

- Nodes are created with `create(...)` factories, configured with chained setters and added with `attach(...)`.
- `body(...)` builds children inline; the position of a child is relative to its parent.
- Anchors (`anchor`, `anchorX`, `anchorY`) choose the point of a node that sits at its `x` and `y`; a text box with an alignment places a text inside a fixed area; `dw`, `aw` and their siblings compute positions from the size of a node.
- `FlexNode` stacks its children with a gap and grows to fit them; flex nodes nest.
- `TextNode` shows a `Text` styled by a `TextInfo`; `ResourceNode` shows an image loaded with `Resource.of(...)`.

Next, [Tutorial 3: Interactivity and State](interactivity.md) turns the placeholders into switches, a slider and a clickable list, and saves the values.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md)
- [FlexNode](../nodes/layout/flex.md)
- [TextNode](../nodes/visual/text.md)
- [ResourceNode](../nodes/visual/resource.md)
- [Essentials: Layout](../essentials/layout.md)