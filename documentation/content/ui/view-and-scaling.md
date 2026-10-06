# View and Scaling

Every UI is designed on a virtual canvas of 1920×1080 units, and its `UIView` (`dev.joid.lib.ui.core.view.UIView`) maps that canvas to the window: it fits the canvas without stretching, places it with the UI's anchors, and applies the interface scale of the bridge and the user's zoom. Read this page to convert between window and canvas coordinates and to build layouts that follow the window size.

## Window and canvas coordinates

| Space | Unit | Where you meet it |
| --- | --- | --- |
| Canvas (UI coordinates) | Canvas units of the 1920×1080 design | Node positions and sizes, mouse coordinates in node callbacks and UI hooks, `ui.getMouseX()`. |
| Window | What the window bridge reports: framebuffer pixels for the GLFW backends | `IWindowBridge.getWidth()`, `getMouseX()`, `UI.getWidth()`, `UI.draw(mouseX, mouseY)`. |

Convert with the view of a UI:

```java
final UIView view = this.getView();
final double canvasX = view.toUiX(windowX);
final double windowY = view.toScreenY(canvasY);
final double pixels = view.toScreenWidth(100D);
```

`toUiX`/`toUiY` and `toScreenX`/`toScreenY` are exact inverses, and `ui.getMouseX()` is `view.toUiX(windowMouseX)`.

## How the canvas fits the window

1. **Fit.** The canvas is scaled uniformly by `min(windowWidth / 1920, windowHeight / 1080)`. It is never stretched: one canvas unit has the same size horizontally and vertically.
2. **Extend.** When the window ratio is not 16:9, the visible area is larger than 1920×1080 in one direction: a 2560×1080 window shows 2560×1080 canvas units, a 1080×1080 window shows 1920×1920.
3. **Place.** The UI's anchors decide where the 1920×1080 design sits in that larger area.
4. **Scale.** The interface scale and the zoom scale the canvas around the anchor point.

### Anchors with anchorX and anchorY

`@UIData(anchorX = ..., anchorY = ...)` takes an `Align`: `START`, `CENTER` (default) or `END`. The anchor point is (0, 960 or 1920; 0, 540 or 1080) in canvas units.

| Window | Anchor | Result |
| --- | --- | --- |
| 2560×1080 | `anchorX = CENTER` | The design is centered: canvas x 0 is at window x 320, and the window shows x from -320 to 2240. |
| 2560×1080 | `anchorX = START` | The design is pinned to the left: canvas x 0 is at window x 0, the extra space is on the right. |
| 2560×1080 | `anchorX = END` | The design is pinned to the right. |
| 1920×1200 | `anchorY = CENTER` | 60 extra units above and below the design. |

```java
@UIData(anchorX = Align.END, anchorY = Align.START, background = false)
public final class MinimapUI extends UI {

    @Override
    public void init() {
        RectNode.create(1620, 20, 280, 280).color(Color.DARKGRAY).attach(this);
    }

}
```

![The window is resized to a wide and then a tall shape; the gray minimap stays in its top-right corner](../images/view-minimap.gif "Window sizes 1920×1080, 1920×760, 1300×1080: the design (dark area) is pinned to the top-right corner and the extra space (lighter) opens on the other sides (0.3× scale, black is outside the window).")

The minimap stays in the top-right corner of the window whatever its ratio, and the zoom shrinks it towards that corner.

## Interface scale

A bridge can scale a UI with `IUIBridge.getInterfaceScale(UI ui)` (default `1D`), for example to follow the GUI scale setting of its host. The UI reads it at every draw. The total scale is `interfaceScale × zoom`, applied around the anchor: with an interface scale of 0.5, the design takes half the size and the visible area is 3840×2160 canvas units. See [UI Bridge](../integration/ui-bridge.md).

## Zoom

| Way | Effect |
| --- | --- |
| `ui.zoom(double zoom)` | Sets the zoom. |
| `Ctrl` or `Alt` + `+` / `-` | Adds or removes 0.1, when the UI is `zoomable` (default). |
| Left `Alt` + wheel | Dev mode only; see [Developer Tools](../getting-started/dev-tools.md). |

The zoom is clamped between 0.1 and `max(1, 1 / interfaceScale)`: at an interface scale of 1 you can zoom out but not beyond the fitted size, and a UI shrunk by its interface scale can be zoomed back to full size. A zoom below 1 shows more canvas: at 0.5, the visible area of a 16:9 window is 3840×2160 units.

The zoom survives `reload()`. A new `load(width, height)` from the bridge, as on a window resize with `UIBridge.load()`, sets it back to 1.

## Reacting to the visible area

Three `DoubleSignal`s of the UI follow the view:

| Getter | Value |
| --- | --- |
| `getZoomLevel()` | The zoom. |
| `getScaledWidth()` | The visible width in canvas units: `viewportWidth / (interfaceScale × zoom)`. |
| `getScaledHeight()` | The visible height in canvas units. |

They change on load, resize, zoom and interface scale changes. Watch them to keep a node aligned with the window edges. This bar spans the whole visible width at the top of the window:

```java
@Override
public void init() {
    final UIView view = this.getView();

    RectNode
    .create(view.toUiX(0D), view.toUiY(0D), this.getScaledWidth().getOrDefault(), 60D)
    .color(Color.BLACK)
    .watch(this.getScaledWidth(), WatchProperty.NONE)
.watch(this.getScaledHeight(), WatchProperty.NONE)
    .onWatch((node, signal, properties) -> node.bounds(view.toUiX(0D), view.toUiY(0D), this.getScaledWidth().getOrDefault(), 60D))
    .attach(this);
}
```

![The window is resized and the black bar keeps spanning its whole top edge](../images/view-bar.gif "The bar follows the visible area at every window size (0.3× scale, black below or beside the window is outside it).")

`UI.getWidth()` and `UI.getHeight()` return the window size in pixels, not the canvas size.

## Projection

With `@UIData(projection = true)` (default), the UI draws with its own orthographic projection that spans the visible area. With `projection = false`, it keeps the projection set by the host, for hosts that already set up a 2D projection for their screens. The [Quick Start](../getting-started/quick-start.md) sets a window-pixel projection before drawing, which the UI background and `drawBackground` use.

## UIView reference

| Member | Description |
| --- | --- |
| `static UIView create(double anchorX, double anchorY)` | Creates a view with an anchor point in canvas units. Each UI creates its own; you rarely need another. |
| `UIView resize(double width, double height)` | Sets the window size and recomputes the fit. |
| `UIView zoom(double zoom)` | Sets the zoom, clamped to `[0.1, getMaxZoom()]`. Use `ui.zoom(...)` on a UI so its signals follow. |
| `UIView interfaceScale(double interfaceScale)` | Sets the interface scale and clamps the zoom again. |
| `double toUiX(double screenX)`, `double toUiY(double screenY)` | Window to canvas. |
| `double toScreenX(double uiX)`, `double toScreenY(double uiY)` | Canvas to window. |
| `double toScreenWidth(double uiWidth)`, `double toScreenHeight(double uiHeight)` | Canvas length to window pixels. |
| `double getScale()` | `interfaceScale × zoom`. |
| `double getMaxZoom()` | `max(1, 1 / interfaceScale)`. |
| `double getVisibleWidth()`, `double getVisibleHeight()` | The visible area in canvas units. |
| `double getOffsetX()`, `double getOffsetY()` | Offset of the design inside the visible area, from the anchor. |
| `double getViewportWidth()`, `double getViewportHeight()` | The visible area at scale 1 (at least 1920×1080). |
| `double getWidth()`, `double getHeight()` | The window size. |
| `double getAnchorX()`, `double getAnchorY()` | The anchor point. |
| `double getZoom()`, `double getInterfaceScale()` | The current zoom and interface scale. |
| `void render(IRenderBridge render, boolean projection, Runnable draw)` | Runs `draw` with the view's projection (if `projection`) and transform, and restores them afterwards, even when `draw` throws. |

## See also

- [The UI Class](ui-class.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [Watching Signals](../state/watch.md)
- [UI Bridge](../integration/ui-bridge.md)