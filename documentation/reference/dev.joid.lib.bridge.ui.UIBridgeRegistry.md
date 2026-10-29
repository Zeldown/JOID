---
summary: The registry of UI bridges, which runs the frame of each one and routes the input between them.
---

`BridgeHandler.UI` is the only instance.
[[Backend#frame]] calls `frame()`, and the input forwarders of the backends call the input methods; you call them yourself only when you forward the input of a host by hand.
Each input method returns `true` when JOID consumed the event, which should then not reach the rest of your application.

Learn: [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own), [Input forwarding](/build/input/).

## UIBridgeRegistry()

Creates an empty registry; JOID creates the one of `BridgeHandler.UI`.

## frame()

Runs one frame: when the window size changed and is not zero, it resizes the screen and lays every UI out again; then it runs `fireFrame()`, updates every bridge, begins the frame of the render bridge, lets the bridge of lowest priority draw the background, draws every bridge from the lowest priority to the highest, and ends the frame.

## onFrame(Runnable)

Adds a task that runs at the start of every frame, after the tasks of other threads and before the UIs update.

## fireFrame()

Runs the tasks queued by other threads on every [[FrameThreadBridge]], then the tasks given to `onFrame`; `frame()` calls it.

## mouseMoved()

Sends a mouse move to the bridges from the highest priority down, until one consumes it.

## mousePressed(MouseButton)

Sends a press to the bridges from the highest priority down, until one consumes it, and remembers the bridges that received it.

## mouseReleased(MouseButton)

Sends the release to every bridge that received the press of that button; returns `true` when one of them consumed the press.

## mouseScroll(double,double)

Sends a scroll, in wheel notches, to the bridges from the highest priority down, until one consumes it.

## charTyped(int)

Sends a typed Unicode code point to the bridges from the highest priority down, until one consumes it.

## keyPressed(Key,Key)

Records the press in the key state, with the physical key and the key of the layout, then sends it to the bridges from the highest priority down, until one consumes it.
Key repeats come from JOID itself: send only the first press.

## keyReleased(Key)

Records the release in the key state and sends it to every bridge; returns `true` when the press of that key was consumed, so that the host hides its release too.

## isKeyConsumed(Key)

If `true`, a bridge consumed the last press of that physical key.

## get(UI)

Returns the bridge that holds the UI when it is open, otherwise the bridge of highest priority that can handle it, or `null`.

## get(Class)

Returns the bridge of highest priority that can handle UIs of that class, or `null`.

## getKeyState()

Returns the state of the held keys, which [[Key#isDown]] reads for the keys that depend on the layout.
