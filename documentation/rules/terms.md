# Terms

The controlled list of terms of the JOID documentation, and the source of the glossary page (`npm run docgen` generates `use/reference/glossary`).
Each `##` is a term as the documentation writes it; the text under it is its definition (1 to 3 sentences, present tense).

`Avoid:` lists words that the `terms` check refuses in every page (prose only, code is ignored).
`See:` links the owner page of the notion (see `owners.md`).
A writer who needs a term that is not listed adds it here.

## UI
A class that extends [[UI]] and describes one interface: a screen, an overlay or a popup.
JOID draws every open UI on top of the others in the order of their `zindex`.
See: /use/learn/describing-the-ui/uis/

## screen
A UI that takes the whole window and receives the input first.
See: /use/learn/describing-the-ui/uis/

## overlay
A UI drawn over the screen, configured by [[UIDataOverlay]], that lets the input through or not.
Avoid: HUD
See: /use/learn/describing-the-ui/overlays-and-popups/

## popup
A UI opened above another one, configured by [[UIDataPopup]], with an opening and a closing transition.
See: /use/learn/describing-the-ui/overlays-and-popups/

## background
The documentation uses this word in three senses, always with its qualifier:

- **UI backdrop**: the veil that `@UIData(background = true)` draws behind a UI, with the color of `backgroundColor`; a modal popup uses it to dim what is under it.
- **Window background**: the color that [[WindowUIBridge#background(Color)]] clears the window with before every frame.
- **Host background**: what your own application draws around JOID when JOID runs inside it; JOID never draws it.
See: /use/learn/describing-the-ui/uis/

## node
An element of the node tree: a shape, a text, an image, a control or a container.
Every node extends [[Node]] and is created by a `create(...)` factory.
Avoid: widget
See: /use/learn/describing-the-ui/nodes/

## node tree
The nodes of a UI, attached to the UI or to each other; a child is drawn and positioned relative to its parent.
See: /use/learn/describing-the-ui/nodes/

## design canvas
The fixed area, in design pixels, on which you place the nodes of a UI; JOID scales it to the window.
Avoid: virtual canvas
See: /use/learn/describing-the-ui/design-canvas/

## design pixel
The unit of every position and size in JOID: one pixel of the design canvas, whatever the size of the window.
See: /use/learn/describing-the-ui/design-canvas/

## screen pixel
The unit of the window: one pixel of what the backend draws, in which the window bridge gives the size of the window and the position of the mouse.
On a HiDPI screen, the backends read the size of the framebuffer: a window that the system scales by 2 is twice as many screen pixels wide as the size the system gives it.
Avoid: window pixel, framebuffer pixel, device pixel
See: /use/learn/describing-the-ui/design-canvas/

## interface scale
A factor that the UI bridge applies on top of the canvas scaling, to make every UI larger or smaller.
See: /use/learn/describing-the-ui/design-canvas/

## signal
A value holder that the interface follows: a node property set from a signal shows its new value from the next frame after the signal changes.
See: /use/learn/interactivity/signals/

## reactive setter
The overload of a fluent setter that takes a `Supplier`, so that the property follows a value that changes.
To the question "When does JOID compute this value again?", it answers: on every frame for a lambda, when one of its signals changes for an expression that reads signals.
See: /use/learn/interactivity/signals/

## callback
A function that you give to an `onX(...)` method and that JOID calls when the event happens.
See: /use/learn/interactivity/events/

## phase
The moment of a callback relative to the node's own handling: `PRE` before it, `POST` after it.
See: /use/learn/interactivity/events/

## consume
To stop an input event: a consumed event reaches no other handler such as `onClick`, no UI below and not the host.
Listeners such as `onMousePressed` still hear it.
See: /use/learn/interactivity/events/

## cancel
To call `cancel()` on the [[DispatchContext]] that a callback receives: for an input event, it consumes the event; for an action such as `onChange`, a `PRE` callback that cancels stops the action and the `POST` phase.
Avoid: consume the context, consumes the context
See: /use/learn/interactivity/events/

## control
A node that holds a value the user changes: checkbox, toggle, switch, selector, slider, text field.
See: /use/learn/interactivity/controls/

## skin
A subclass of an abstract control that draws it; a set of skins forms a UI kit.
See: /use/learn/going-further/ui-kit/

## effect
A [[NodeEffect]] that changes how a node is drawn (rounded corners, border, shadow, blur, mask, transform), never its layout.
See: /use/learn/style-and-motion/effects/

## transition
The animation of a UI when it opens or closes.
See: /use/learn/style-and-motion/transitions/

## store
A shared value container that lives as long as its scope: a UI, the application or the disk.
See: /use/learn/state/stores-and-properties/

## backend
The module that runs JOID on a graphics and window library: LWJGL 2, LWJGL 3 or Vulkan.
See: /use/learn/running/backends/

## bridge
The interface through which the core of JOID talks to a backend (render, window, audio, thread, clock, UI).
See: /use/learn/running/embedding/

## UI bridge
The bridge that holds the open UIs, routes the input to them and draws them every frame; `WindowUIBridge` is the default one.
See: /use/learn/running/embedding/

## priority
The order of the bridges of one registry: the highest priority receives the input first and is drawn on top.
See: /use/learn/running/embedding/

## host
The application that runs JOID inside its own window and loop.
See: /use/learn/running/embedding/

## render thread
The thread that calls `frame()`: JOID draws there and calls the callbacks of your UIs there.
With JOID's backends, the end of a load (`onLoad`, a font future, `Signal.of(future)`) runs on the thread that finished loading instead.
See: /use/learn/running/threads/

## dev jar
The `-dev` jar of a JOID module: the prod content plus the inspector, the reload, the dev warnings and the snapshot backends.
See: /use/get-started/installation/

## prod jar
The `-prod` jar of a JOID module, the one an application ships.
See: /use/get-started/installation/

## data folder
The folder where JOID writes the data of the application: stores and remembered properties.
See: /use/learn/running/files/

## cache folder
The folder where JOID writes machine caches that it can rebuild, such as generated font atlases.
See: /use/learn/running/files/

## MSDF
Multi-channel signed distance field: the technique JOID uses to keep text sharp at every size.
See: /use/in-depth/msdf-text/

## anchor
The side or the corner of the window that a UI is pinned to, set by `anchorX` and `anchorY` of [[UIData]]: the canvas sits there when the window shows more than it, and zooms around it.
A node has anchors too: the point of its bounds that stays in place when its size changes ([Position and size](/use/learn/describing-the-ui/position-and-size/)).
See: /use/learn/describing-the-ui/design-canvas/

## body
The lambda that `body(...)` runs right away with a node, to create its children where the node is created.
See: /use/learn/describing-the-ui/nodes/

## derived value
A read-only signal computed from other signals, with `map` or [[Signal#from]].
JOID computes it again when it is read after one of those signals changed, or at once when it has subscribers.
See: /use/learn/state/derived-values/

## dev mode
The mode that the dev jar turns on: the inspector, hot reload, the dev shortcuts and the warnings meant for development.
[[JOIDEnv#isDevMode]] reads it.
See: /use/get-started/dev-tools/

## frame
One call of `frame()` by your loop: JOID handles the resize, updates every open UI, then draws them.
See: /use/in-depth/frame-lifecycle/
Avoid: tick, update tick

## layout node
A node that places its children itself on every frame, such as a [[FlexNode]] or a [[GridNode]]; any other node leaves its children where they are.
See: /use/learn/describing-the-ui/layout/

## resource
An image, an animated image, a vector image or a video, created by [[Resource#of]] from a file, a stream, a URL or an asset.
See: /use/learn/describing-the-ui/media/

## skeleton
The placeholder that a node draws in its place while one of its `wait` conditions fails.
See: /use/learn/state/rebuilding/