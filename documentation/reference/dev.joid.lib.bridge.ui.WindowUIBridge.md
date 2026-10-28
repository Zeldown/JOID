---
summary: The default UI bridge, which clears the window to a color every frame and draws text tooltips.
---

The backend registers one when no UI bridge was registered before it.
Get it from `BridgeHandler.UI.getBridge(WindowUIBridge.class)` to set its colors and its tooltip text, as [Style the window bridge](/use/learn/running/backends/#style-the-window-bridge) shows.

Learn: [Style the window bridge](/use/learn/running/backends/#style-the-window-bridge).

## WindowUIBridge()

Creates the bridge with its default colors, for a subclass.

## create()

Creates a window bridge with its default colors and no tooltip text style.

## background(Color)

Sets the color that clears the window before every frame, behind every UI.

## hoverColor(Color)

Sets the fill of the tooltip box.

## hoverInfo(TextInfo)

Sets the text style of the tooltips; without it, the bridge draws no tooltip and, with the dev jar, prints a warning once.

## hoverBorderColor(Color)

Sets the color of the border, one design pixel wide, of the tooltip box.

## drawHover(UI,Object,double,double)

Draws the lines of the tooltip in a rounded box near the pointer, moved to stay inside the window.

## drawBackground()

Clears the color of the window to `background`, then the depth and the stencil.

## getBackground()

Returns the color that clears the window.

## getHoverColor()

Returns the fill of the tooltip box.

## getHoverInfo()

Returns the text style of the tooltips, or `null` when none is set and no tooltip is drawn.

## getHoverBorderColor()

Returns the color of the border of the tooltip box.
