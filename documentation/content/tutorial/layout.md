# Tutorial 2: Building the Layout

In this part you build the static layout of the settings screen with what [Nodes and the Node Tree](../concepts/nodes.md), [Layout](../essentials/layout.md) and [Text](../essentials/text.md) taught: a card centered on the canvas, a header with an icon and a title, and columns of rows laid out by `FlexNode`. Nothing reacts to the mouse yet; the values on the right of the rows are placeholders that part 3 replaces with real controls.

You start from the code of [Tutorial 1](setup.md). `Main` and `AppUIBridge` do not change in this part.

## Step 1: the card with RectNode

The card is a `RectNode` (`dev.joid.lib.ui.node.impl.design.shape`), the filled rectangle of [Nodes and the Node Tree](../concepts/nodes.md). First give `Theme` the two colors of the screen, next to the font, so that every class of the application shares them:

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

Positions are units of the 1920×1080 virtual canvas, fitted to the window without stretching; see [The Virtual Canvas](../concepts/canvas.md). A card 800 wide at x = 560 is horizontally centered ((1920 - 800) / 2), and 780 high at y = 150 is vertically centered ((1080 - 780) / 2), whatever the window. `Color.decode` reads the `#RRGGBB` colors of your design, as in [Styling and Effects](../concepts/styling.md).

![A light gray card centered on a near-black background](../images/tutorial-layout-card.png "Step 1: one RectNode, centered on the canvas")

## Step 2: children with body

The header is three children of the card, built in its `body(...)` and placed relative to it: (40, 40) inside the card is 40 units from its top-left corner, wherever the card is. Create the text styles once, at the start of `init()`, and reuse them:

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

- The icon is a `ResourceNode` (`dev.joid.lib.ui.node.impl.design.resource`) showing `Resource.of(new File(...))`, loaded in the background as in [Images and Media](../essentials/media.md).
- The title is a `TextNode` without a size, which takes the size of its text. `anchorY(Align.CENTER)` puts its vertical center on y = 64, the center of the 48-unit icon (40 + 24), so both line up whatever the font size.
- The hint has a box: `TextNode.create(x, y, width, height)` with `Text.create(text, info, horizontalAlign, verticalAlign)` aligns the text inside it, so the hint ends at the right edge of a box 40 units narrower than the card. `card.aw(-40)` is the width of the card minus 40, that is 760: the size helpers of [Layout](../essentials/layout.md) read the size of the node, so you never write a size twice.

![The card with a gear icon, the bold title Settings and the hint Saved automatically on the right](../images/tutorial-layout-header.png "Step 2: three children placed relative to the card")

## Step 3: rows with FlexNode

The rows go in a `FlexNode.vertical(x, y, width)` column (`dev.joid.lib.ui.node.impl.structure.flex`), which stacks its children with a gap (`margin`) and grows to fit them: each child is created at (0, 0) and the flex moves it into its slot, as in [Layout](../essentials/layout.md).

All the rows look the same, so a small method of `SettingsUI` builds one and returns it, ready to receive the control on its right:

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

`row.dh(2)` is half the height of the row: with `anchorY(Align.CENTER)`, the label is vertically centered. The method attaches the row to the column it receives and returns it: `attach(...)` returns the node typed as what you assign it to, here a `RectNode`, so the caller can add the value on the right of the row.

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
- The children are laid out in the order you attach them. A hidden child takes no room: the next ones move up to fill its place, which part 3 uses to hide the Volume row.

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
import dev.joid.lib.font.TextInfo;
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

> TIP: In dev mode, press `F3`, click Inspect and hover the rows: the inspector shows the class, the bounds and the place in the tree of each node. See [Developer Tools](../concepts/dev-tools.md).

## Recap

- Nodes are created with `create(...)` factories, configured with chained setters and added with `attach(...)`.
- `body(...)` builds children inline; the position of a child is relative to its parent.
- Anchors (`anchor`, `anchorX`, `anchorY`) choose the point of a node that sits at its `x` and `y`; a text box with an alignment places a text inside a fixed area; `dw`, `aw` and their siblings compute positions from the size of a node.
- `FlexNode` stacks its children with a gap and grows to fit them; flex nodes nest.
- `TextNode` shows a `Text` styled by a `TextInfo`; `ResourceNode` shows an image loaded with `Resource.of(...)`.
- A method that builds a node, attaches it and returns it removes the repetition of identical rows.

## See also

- Next: [Tutorial 3: Interactivity and State](interactivity.md) turns the placeholders into switches, a slider and a clickable list, and saves the values.
- [Layout](../essentials/layout.md)
- [Nodes and the Node Tree](../concepts/nodes.md)
- [FlexNode](../nodes/layout/flex.md)
- [TextNode](../nodes/visual/text.md)
- [ResourceNode](../nodes/visual/resource.md)