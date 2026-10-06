# Tutorial 2: Building the Layout

In this part you build the static layout of the settings screen: a card centered on the canvas, a header with an icon and a title, and a column of rows laid out by a `FlexNode`. Nothing reacts to the mouse yet; the values on the right of each row are placeholders that part 3 replaces with real controls.

You start from the `SettingsUI` of [Tutorial 1](setup.md). `Main` and `AppUIBridge` do not change in this part.

## Step 1: the card

Every visible element of a UI is a node. `RectNode` (`dev.joid.lib.ui.node.impl.design.shape`) draws a filled rectangle; it is the usual base for cards, rows and buttons. Replace the body of `init()`:

```java
@Override
public void init() {
    RectNode
    .create(560, 150, 800, 780)
    .color(SettingsUI.CARD)
    .attach(this);
}
```

with a color constant at the top of the class:

```java
private static final Color CARD = Color.decode("#111827");
```

- `create(x, y, width, height)` takes canvas units. The canvas is 1920×1080, so a card 800 wide at x = 560 is horizontally centered ((1920 - 800) / 2), and 780 high at y = 150 is vertically centered ((1080 - 780) / 2).
- `color(...)` sets the fill. `Color.decode` reads `#RRGGBB` and other formats; see [Colors and Gradients](../styling/colors.md).
- `attach(this)` adds the card at the top level of the UI.

Run the program: a dark gray card sits in the middle of the near-black background.

## Step 2: children with body

A node can hold children. `body(...)` runs a lambda right away with the node, so you build its children inline, and `attach(card)` adds each child to the card. Children are placed relative to their parent: (40, 40) inside the card is 40 units from its top-left corner, wherever the card is.

Add the header, an icon and a title:

```java
RectNode
.create(560, 150, 800, 780)
.color(SettingsUI.CARD)
.body(card -> {
    ResourceNode.create(40, 40, 48, 48).resource(Resource.of(new File("icons/settings.png"))).attach(card);
    TextNode.create(104, 64).text(Text.create("Settings", title)).anchorY(Align.CENTER).attach(card);
    TextNode.create(card.aw(-40), 64).text(Text.create("Saved automatically", hint)).anchor(Align.END, Align.CENTER).attach(card);
})
.attach(this);
```

- `ResourceNode` (`dev.joid.lib.ui.node.impl.design.resource`) displays an image. `Resource.of(new File(...))` (`dev.joid.lib.resource`) loads it in the background; until it is ready, the node draws a pulsing gray placeholder. A URL, a stream or a file of your jar work too; see [Resources](../resources/resources.md).
- `TextNode.create(x, y)` creates a text node without size: it takes the size of its text.
- **Anchors** decide which point of a node sits at its `x`/`y`. By default it is the top-left corner. `anchorY(Align.CENTER)` puts the vertical center of the title on y = 64, the center of the 48-unit icon (40 + 24), so both line up whatever the font size. `anchor(Align.END, Align.CENTER)` puts the right edge of the hint on its `x`.
- `card.aw(-40)` is the card's width plus -40: 760, 40 units from the right edge. The relative helpers (`aw`, `ah`, `dw`, `dh`, `mw`, `mh`) read the size of the node you call them on, here the parent, so you never hardcode it twice; see [Node Fundamentals](../nodes/node-fundamentals.md#relative-helpers-dw-dh-mw-mh-aw-ah-ax-ay).

`title` and `hint` are two `TextInfo` objects created at the start of `init()`:

```java
final TextInfo title = TextInfo.create(this.font, FontWeight.BOLD, 40, Color.WHITE);
final TextInfo hint = TextInfo.create(this.font, 18, SettingsUI.MUTED);
```

A `TextInfo` (`dev.joid.lib.font.dto`) is the style of a text: font, weight, size, color, spacing. Create each style once and reuse it; see [Text and TextInfo](../text/text-and-textinfo.md).

## Step 3: rows with FlexNode

Placing every row by hand would mean computing each `y` yourself. `FlexNode` (`dev.joid.lib.ui.node.impl.structure.flex`) does it: `FlexNode.vertical(x, y, width)` stacks its children from top to bottom with a fixed gap (`margin`), and grows to fit them. Each child is created at (0, 0); the flex moves it into its slot.

All the rows look the same, so a small method builds one and returns it, ready to receive the control on its right:

```java
private RectNode row(final Node parent, final String name, final TextInfo info) {
    return RectNode
    .create(0, 0, 720, 72)
    .color(SettingsUI.ROW)
    .body(row -> {
        TextNode.create(24, row.dh(2)).text(Text.create(name, info)).anchorY(Align.CENTER).attach(row);
    })
    .attach(parent);
}
```

`row.dh(2)` is half the row's height: with `anchorY(Align.CENTER)`, the label is vertically centered. `attach(...)` returns the node typed as what you assign it to, here a `RectNode`.

Section titles are text nodes with a fixed height of 36 and their text aligned to the bottom of that box, which leaves some space above each section:

```java
TextNode.create(0, 0, 0, 36).text(Text.create("AUDIO", section, Align.START, Align.END)).attach(flex);
```

A width of `0` still follows the text; the height of 36 is kept. `Text.create(text, info, horizontalAlign, verticalAlign)` places the text inside the node's box.

Now the column, inside the card's `body`:

```java
FlexNode
.vertical(40, 112, 720)
.margin(12)
.body(flex -> {
    TextNode.create(0, 0, 0, 36).text(Text.create("AUDIO", section, Align.START, Align.END)).attach(flex);
    final RectNode music = this.row(flex, "Music", label);
    TextNode.create(music.aw(-24), music.dh(2)).text(Text.create("On", label)).anchor(Align.END, Align.CENTER).attach(music);
    final RectNode volume = this.row(flex, "Volume", label);
    TextNode.create(volume.aw(-24), volume.dh(2)).text(Text.create("80 %", label)).anchor(Align.END, Align.CENTER).attach(volume);

    TextNode.create(0, 0, 0, 36).text(Text.create("GENERAL", section, Align.START, Align.END)).attach(flex);
    final RectNode notifications = this.row(flex, "Notifications", label);
    TextNode.create(notifications.aw(-24), notifications.dh(2)).text(Text.create("Off", label)).anchor(Align.END, Align.CENTER).attach(notifications);
})
.attach(card);
```

The children are laid out in the order you attach them. Hide one later (see [Node Fundamentals](../nodes/node-fundamentals.md#visibility-and-enabled-state)) and the next ones move up to fill its place.

## Step 4: a nested list

The language list is a second `FlexNode`, inside the first one. It builds one row per language from a list, and colors the selected one with the accent color:

```java
TextNode.create(0, 0, 0, 36).text(Text.create("LANGUAGE", section, Align.START, Align.END)).attach(flex);
FlexNode
.vertical(0, 0, 720)
.margin(8)
.body(list -> {
    for (final String language : SettingsUI.LANGUAGES) {
        RectNode
        .create(0, 0, 720, 52)
        .color("English".equals(language) ? SettingsUI.ACCENT : SettingsUI.ROW)
        .body(item -> {
            TextNode.create(24, item.dh(2)).text(Text.create(language, label)).anchorY(Align.CENTER).attach(item);
        })
        .attach(list);
    }
})
.attach(flex);
```

A `FlexNode` is a node like any other: the outer column sees the inner list as one child, 232 units high (four rows of 52 and three gaps of 8). "English" is hardcoded as the selected language for now; part 3 stores the real choice.

## The complete code

```java
package com.example.settings;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
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

@UIData(backgroundColor = "#030712")
public final class SettingsUI extends UI {

    private static final Color CARD = Color.decode("#111827");
    private static final Color ROW = Color.decode("#1F2937");
    private static final Color ACCENT = Color.decode("#6366F1");
    private static final Color MUTED = Color.decode("#9CA3AF");

    private static final List<String> LANGUAGES = Arrays.asList("English", "Français", "Deutsch", "Español");

    private final IFont font;

    public SettingsUI(final IFont font) {
        this.font = font;
    }

    @Override
    public void init() {
        final TextInfo title = TextInfo.create(this.font, FontWeight.BOLD, 40, Color.WHITE);
        final TextInfo hint = TextInfo.create(this.font, 18, SettingsUI.MUTED);
        final TextInfo section = TextInfo.create(this.font, FontWeight.BOLD, 16, SettingsUI.MUTED).letterSpacing(0.08F);
        final TextInfo label = TextInfo.create(this.font, 22, Color.WHITE);

        RectNode
        .create(560, 150, 800, 780)
        .color(SettingsUI.CARD)
        .body(card -> {
            ResourceNode.create(40, 40, 48, 48).resource(Resource.of(new File("icons/settings.png"))).attach(card);
            TextNode.create(104, 64).text(Text.create("Settings", title)).anchorY(Align.CENTER).attach(card);
            TextNode.create(card.aw(-40), 64).text(Text.create("Saved automatically", hint)).anchor(Align.END, Align.CENTER).attach(card);

            FlexNode
            .vertical(40, 112, 720)
            .margin(12)
            .body(flex -> {
                TextNode.create(0, 0, 0, 36).text(Text.create("AUDIO", section, Align.START, Align.END)).attach(flex);
                final RectNode music = this.row(flex, "Music", label);
                TextNode.create(music.aw(-24), music.dh(2)).text(Text.create("On", label)).anchor(Align.END, Align.CENTER).attach(music);
                final RectNode volume = this.row(flex, "Volume", label);
                TextNode.create(volume.aw(-24), volume.dh(2)).text(Text.create("80 %", label)).anchor(Align.END, Align.CENTER).attach(volume);

                TextNode.create(0, 0, 0, 36).text(Text.create("GENERAL", section, Align.START, Align.END)).attach(flex);
                final RectNode notifications = this.row(flex, "Notifications", label);
                TextNode.create(notifications.aw(-24), notifications.dh(2)).text(Text.create("Off", label)).anchor(Align.END, Align.CENTER).attach(notifications);

                TextNode.create(0, 0, 0, 36).text(Text.create("LANGUAGE", section, Align.START, Align.END)).attach(flex);
                FlexNode
                .vertical(0, 0, 720)
                .margin(8)
                .body(list -> {
                    for (final String language : SettingsUI.LANGUAGES) {
                        RectNode
                        .create(0, 0, 720, 52)
                        .color("English".equals(language) ? SettingsUI.ACCENT : SettingsUI.ROW)
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
        .color(SettingsUI.ROW)
        .body(row -> {
            TextNode.create(24, row.dh(2)).text(Text.create(name, info)).anchorY(Align.CENTER).attach(row);
        })
        .attach(parent);
    }

}
```

## What you should see

The card now has its header (the icon, "Settings" in bold, "Saved automatically" in gray on the right), then three sections: **AUDIO** with the Music and Volume rows, **GENERAL** with the Notifications row, **LANGUAGE** with four rows, "English" highlighted in indigo. Each row shows its label on the left and a placeholder value on the right.

![A flat dark gray card with a gear icon and the title Settings, AUDIO, GENERAL and LANGUAGE sections, rows Music On, Volume 80 % and Notifications Off, and four language rows with English filled in indigo](../images/tutorial-layout.png "The static layout: a card, a header and two nested FlexNode columns")

> TIP: In dev mode, press `F3` and hover the rows: the inspector shows each node's class, bounds and position in the tree. See [Developer Tools](../getting-started/dev-tools.md).

## Recap

- Nodes are created with `create(...)` factories, configured with chained setters and added with `attach(...)`.
- `body(...)` builds children inline; a child's position is relative to its parent.
- Anchors (`anchor`, `anchorX`, `anchorY`) choose the point of a node that sits at its `x`/`y`; the helpers `dw`, `aw` and their siblings compute positions from the parent's size.
- `FlexNode` stacks its children with a gap and grows to fit them; flex nodes nest.
- `TextNode` shows a `Text` styled by a `TextInfo`; `ResourceNode` shows an image loaded with `Resource.of(...)`.

Next, [Tutorial 3: Interactivity and State](interactivity.md) turns the placeholders into switches, a slider and a clickable list, and saves the values.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md)
- [FlexNode](../nodes/layout/flex.md)
- [TextNode](../nodes/visual/text.md)
- [ResourceNode](../nodes/visual/resource.md)
- [Essentials: Layout](../essentials/layout.md)