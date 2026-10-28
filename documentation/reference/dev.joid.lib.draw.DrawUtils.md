---
summary: Entry point of the immediate drawing API, with one helper each for shapes, text, images, 3D models and external rasters.
---

Nodes draw through these five helpers, and so does your code in the `draw` of a custom node or in a node layer:

```java fragment
DrawUtils.SHAPE.drawRoundedRect(x, y, 120D, 40D, Color.decode("#262B35"), 8F);
DrawUtils.TEXT.drawText(x + 12D, y + 12D, "Play", info, Align.START, Align.START);
```

Every call draws at once, in the coordinates of the current model-view matrix: design pixels inside a UI.
Call them on the render thread, during a frame; outside a frame the render bridge throws.

Learn: [Drawing shapes and text](/use/learn/going-further/drawing/).

## TEXT

Draws and measures text with a [[TextInfo]]: [[DrawText]].

## SHAPE

Draws rectangles, rounded rectangles, borders, circles, shadows, lines, curves and polygons: [[DrawShape]].

## MODEL

Draws a 3D model scaled into the UI: [[DrawModel]].

## RASTER

Draws what another renderer draws, through an offscreen framebuffer: [[DrawRaster]].

## RESOURCE

Draws resources and textures: [[DrawResource]].
