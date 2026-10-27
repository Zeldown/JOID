---
summary: Base class of a screen, an overlay or a popup that builds its node tree in init() and draws it on the design canvas.
---
A `UI` is one interface: a screen, an overlay or a popup.
Write one class per interface, build its nodes in `init()`, configure it with annotations on the class ([[UIData]], [[UIDataPopup]], [[UIDataOverlay]], [[UIDataScale]], [[UIDataDebug]]), then open it with [[JOID#open]]:

```java file="com/example/reference/ProfileUI.java" region="ui"
```

Every position that a `UI` method takes or returns is in design pixels on the 1920 × 1080 canvas, unless the entry says screen pixels.
The input reaches the nodes first, from the front-most, then the `handleX` listeners of the UI.
JOID calls the hooks and the listeners of a UI on the render thread.

Learn: [UIs](/use/learn/describing-the-ui/uis/), [The design canvas](/use/learn/describing-the-ui/design-canvas/), [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## UI()

Reads the annotations of the class and of its annotated superclasses, and prepares the UI without building any node: `init()` runs when the UI opens.
A listener, a task or a shortcut added in the constructor lasts for the whole life of the instance.
Throws an `IllegalStateException` when the class is both a popup and an overlay.

## init()

Builds the node tree of the UI.
JOID calls it at every opening and at every `reload()`, after it has removed the nodes of the previous opening; a listener, a task or a shortcut added here lasts until the UI closes.

## update()

Runs your own code on every frame, after the nodes of the UI have been updated and before they are drawn.

## load(double,double)

Prepares the UI for a window of this size, in screen pixels: it applies the interface scale and, on the first call after an opening or a `reload()`, loads the remembered properties, runs `init()` and starts the opening transition.
The UI bridge calls it when it opens the UI and when the window resizes; you never call it.

## dispose()

Closes the UI at once, without asking the `beforeClose` listeners and without a transition: it detaches every node, task and shortcut, saves the stores and the remembered properties, then fires `onClose`.
The UI bridge calls it; to close a UI, call [[JOID#close]] or [[JOID#forceClose]].

## reload()

Builds the UI again on the same instance: it saves the remembered properties, reads the annotations of the class again, then runs `init()` again, so the fields keep their values.
Throws an `IllegalStateException` when the annotations make the UI both a popup and an overlay.

## renew()

Replaces this open UI by a fresh instance of its class, built by its constructor without argument, at the same place among the open UIs, and returns that instance.
Throws an `IllegalStateException` when the UI is not open, when the class has no constructor without argument, or when that constructor fails.

## getNodeList()

The nodes attached to the UI itself, in drawing order: the back-most first.
The list is a read-only view that follows the UI.

## getNodeListAt(double,double)

Every visible node under this point of the design canvas, children included, from the front-most to the back-most.

## getNodeAt(double,double)

The front-most visible, interactive node under this point of the design canvas, or `null` when there is none.

## getHoveredNode()

The front-most interactive node under the mouse, or `null` when there is none or when another UI is on top of this one.

## getHoveredPath()

The hovered node followed by each of its parents up to the root of the tree; empty when nothing is hovered.

## drawHover(Object,double,double)

Draws a tooltip at this point: an `Iterable` gives one line per element, any other value one line.
The text tooltips of the nodes go through it; by default it asks the UI bridge, so override it to draw the tooltips of this UI yourself.

## handleMousePressed(EventListener)

Adds a listener that hears a mouse press after the nodes of the UI, unless a node consumed it; a `MONITOR` listener always hears it.
A listener that consumes the event stops the UIs below and the host.

## handleMouseReleased(EventListener)

Adds a listener that hears a mouse release after the nodes of the UI, unless a node consumed it.

## handleMouseDragged(EventListener)

Adds a listener that hears a mouse move while a button is held, after the nodes of the UI, unless a node consumed it.

## handleMouseScroll(EventListener)

Adds a listener that hears a mouse wheel turn after the nodes of the UI, unless a node consumed it.

## handleCharTyped(EventListener)

Adds a listener that hears a typed character after the nodes of the UI, unless a node consumed it.

## handleKeyPressed(EventListener)

Adds a listener that hears a key press after the shortcuts and the nodes of the UI, unless one of them consumed it, and before the zoom and dev-mode keys.

## handleKeyReleased(EventListener)

Adds a listener that hears a key release after the nodes of the UI, unless a node consumed it.

## fireMousePressed(MouseButton)

Sends a mouse press at the current mouse position to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.
The UI bridge calls it; call it yourself only to simulate an input.

## fireMouseReleased(MouseButton)

Sends a mouse release to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.

## fireMouseDragged(MouseButton,long)

Sends a mouse move with this button held for `deltaTime` ms to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.

## fireMouseScroll(double,double)

Sends a wheel turn, in notches, to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.
In dev mode, Alt with the wheel zooms a zoomable UI when nothing consumed the turn.

## fireCharTyped(int)

Sends a typed character, as a Unicode code point, to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.

## fireKeyPressed(Key,Key)

Sends a key press to the shortcuts, the nodes and the listeners of the UI, then handles the zoom keys and the dev-mode keys, and returns `true` when something consumed it.
`physicalKey` is the position of the key on the keyboard, `key` the key that the layout gives; an unopened UI ignores it and returns `false`.

## fireKeyReleased(Key,Key)

Sends a key release to the nodes, then to the listeners of the UI, and returns `true` when something consumed it.

## fireUpdate()

Updates every node of the UI, then runs `update()`.
The UI bridge calls it once per frame.

## fireClose()

Asks the UI to close and returns `true` when it closed at once.
It returns `false` when a `beforeClose` listener cancelled, or when the closing transition starts (the UI closes when it ends); an already closed UI returns `true`.
The UI bridge calls it; to close a UI, call [[JOID#close]].

## beforeClose(EventListener)

Adds a listener that runs before the UI closes through [[JOID#close]] or Escape; cancelling the event keeps the UI open.
[[JOID#forceClose]] does not call it.

## onClose(EventListener)

Adds a listener that runs once the UI has closed, after its nodes are detached and its stores and properties are saved.
It may open another UI.

## getMouseX()

Horizontal position of the mouse on the design canvas.

## getMouseY()

Vertical position of the mouse on the design canvas.

## isAltKeyDown()

If `true`, the left or the right Alt key is down.

## isCtrlKeyDown()

If `true`, the left or the right Control key is down.

## isShiftKeyDown()

If `true`, the left or the right Shift key is down.

## mask(double,double,double,double,Runnable)

Runs the drawing clipped to this rectangle, in the coordinates of the current drawing.
Masks nest: inside another mask, the drawing is clipped to both, and the mask always ends, even when the drawing throws.

## mask(Resource,double,double,double,double,Runnable)

Runs the drawing clipped to the pixels of the resource, drawn in this rectangle, whose alpha is at least 0.5.
A resource that has not loaded yet hides the whole drawing.

## draw(double,double)

Draws one frame of the UI with the mouse at this point, in screen pixels: the UI backdrop, then the nodes inside the transition, then the drag copies and the tooltip of the hovered node.
The UI bridge calls it once per frame.

## drawGhost(Node)

Adds a drag copy to draw above every node of the UI during the current frame.
A dragged node calls it for its `DraggableType.COPY` copy.

## getTaskList()

The tasks attached to the UI itself, without those of its nodes; a read-only view.

## getShortcutList()

The shortcuts attached to the UI itself, without those of its nodes; a read-only view.

## getIndex()

The `zindex` of the UI, which orders it among the open UIs of its bridge.

## getZoom()

The zoom of the UI, `1` when it is not zoomed.

## getWindowWidth()

Width of the window, in screen pixels.

## getWindowHeight()

Height of the window, in screen pixels.

## getViewX()

Position of the left edge of the window on the design canvas.
It is `0` when the window shows exactly the canvas, and negative when the window shows more than the canvas on the left.

## getViewY()

Position of the top edge of the window on the design canvas, negative when the window shows more than the canvas above it.

## getViewWidth()

Width of the window measured in design pixels, after the interface scale and the zoom.

## getViewHeight()

Height of the window measured in design pixels, after the interface scale and the zoom.

## getBridge()

The UI bridge that holds this UI, or that would open it; `null` when no registered bridge accepts it.

## getCurrent()

The UI whose code is running on the render thread: the one that runs `init()`, a listener, a task or an input; `null` outside of them.
A node created without an explicit UI reads it.

## getView()

The view that converts between screen pixels and design pixels for this UI.

## getData()

The [[UIDataObject]] read from `@UIData`; its setters change the options of the running UI from the next frame.

## getDebug()

The [[UIDataDebugObject]] read from `@UIDataDebug`.

## getPopup()

The [[UIDataPopupObject]] read from `@UIDataPopup`.

## getScale()

The [[UIDataScaleObject]] read from `@UIDataScale`.

## getOverlay()

The [[UIDataOverlayObject]] read from `@UIDataOverlay`.

## transition

The animation played when the UI opens and closes.
A popup gets a [[PopTransition]] from `@UIDataPopup`; a transition set here replaces it, and `null` removes it.

## depthLevel

Depth, along the Z axis, that the root nodes drawn after this point and the next UI start from.
JOID resets it to `0` at every frame; a node that draws in depth, such as a 3D model, raises it so that what comes next is drawn in front.

## isClosed()

If `true`, the UI has closed and has not been opened again.

## isInitialized()

If `true`, `init()` has run since the UI opened or since the last `reload()`.

## getFps()

Frames per second of the UI, measured over the last second.

## getRenderTime()

Time that the last frame of the UI took to draw, in nanoseconds.

## getFrameTime()

Time between the last two frames of the UI, in ms; `1000 / 60` before the first frame.

## isOnTop()

If `true`, the UI is the first to receive the input among the screens and popups of its bridge, or among its overlays for an overlay.

## getDevNode()

The dev panel of the UI, created at its first load in dev mode; `null` outside dev mode.
F3 shows or hides it.

## zoom(double)

Sets the zoom of the UI: `2D` shows the canvas twice as large around its anchor.
The value is kept between `0.1` and the largest zoom that the interface scale allows; the requested value applies again when the scale changes.

## zoom(Signal)

Binds the zoom to a signal: the UI follows the signal, and a zoom by the user or by `zoomTo` writes back to it.

## zoomTo(double)

Zooms to this value as a change: the `beforeZoom` listeners can refuse it and `onZoom` fires.

## zoomBy(double)

Zooms by this amount from the current zoom, as a change, like `zoomTo`.

## beforeZoom(EventListener)

Adds a listener that runs before the zoom changes; cancelling the event refuses the change.

## onZoom(EventListener)

Adds a listener that runs after the zoom has changed.

## add(Node[])

Attaches nodes at the root of the UI, in front of the other roots of the same `zindex`.
A node that has a parent or that belongs to another UI leaves it first; a node already at the root of this UI moves to the front without being loaded again.

## remove(Node[])

Detaches these root nodes from the UI; a node that is not a root of this UI is ignored.

## addTask(Task)

Attaches a task to the UI: it runs while the UI is open, and stops when the UI closes.
A task attached elsewhere moves here.

## removeTask(Task)

Detaches a task from the UI.

## addShortcut(Shortcut)

Attaches a shortcut to the UI: it fires while the UI is open, after the shortcuts of its visible and enabled nodes.
A shortcut attached elsewhere moves here.

## removeShortcut(Shortcut)

Detaches a shortcut from the UI.

## useStore(Class,Supplier)

Returns the store of this class, created by the factory the first time.
A `LOCAL` store lives as long as the UI is open; a `GLOBAL` or `PERMANENT` store is shared by every UI.

## lerpByFramerate(double,double,double,double)

Moves `value` toward `target` and returns the result, at the same pace whatever the frame rate.
`speed` is the share of the remaining distance covered in 1/60 s (`1` reaches the target at once), and the result snaps to the target once it is within `snapDiff`.
