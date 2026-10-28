---
summary: The base class of every UI bridge, which dispatches the input to its UIs, updates them and draws them by layer.
---

A subclass decides what opening and closing a UI mean; [[StackUIBridge]] already does it with a screen stack, and is the usual base.
[[UIBridgeRegistry]] calls the input methods and the steps of the frame; each input method returns `true` when the bridge consumed the event.
[Draw JOID over your own scene](/use/learn/running/embedding/#draw-joid-over-your-own-scene) overrides `drawBackground()` to keep the scene of the host under the UIs.

Learn: [UI bridge](/build/bridges/ui/), [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own).

## UIBridge()

Creates a bridge with no UI.

## load()

Lays every UI of the bridge out at the current size of the window; the registry calls it when the size changes.

## mouseMoved()

Sends a drag to the UIs from the top down while a button is held, and returns `true` when a UI consumed it; without a held button it does nothing, since the hover is read from the pointer position each frame.

## mousePressed(MouseButton)

Sends the press to the UIs from the top down, until one consumes it or a popup stops it, and remembers the UIs that received it for the release.

## mouseReleased(MouseButton)

Sends the release to the UIs from the top down and to every UI that received the press; returns `true` when the press or the release was consumed.

## mouseScroll(double,double)

Sends a scroll of that many notches to the UIs from the top down; a scroll of zero notches does nothing.

## charTyped(int)

Sends a typed character, as a Unicode code point, to the UIs from the top down; control characters are ignored.

## keyPressed(Key,Key)

Sends a key press to the UIs from the top down.
Escape closes the top closeable UI that is not an overlay when no node consumed it ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).

## keyReleased(Key,Key)

Sends the release to the UIs from the top down and to every UI that received the press; returns `true` when the press was consumed.

## isKeyConsumed(Key)

If `true`, the last press of that key was consumed by this bridge.

## update()

Updates every UI of the bridge; the registry calls it once per frame, before drawing.

## draw()

Draws every UI of the bridge; the same as `draw(filter)` with a filter that accepts every UI.

## draw(Predicate)

Draws the shown UIs that the filter accepts: the overlays below the screens, then the screens and popups, then the overlays above the screens, each group in `zindex` order, then sets the cursor.
It first releases the GPU resources that nothing uses any more.
Throws an `IllegalStateException` outside a frame of the render bridge; a draw that throws skips the rest of the frame of the bridge, puts the matrix and state stacks back and prints one warning.

## add(UI)

Adds the UI to the list of the bridge.

## remove(UI)

Removes the UI from the list of the bridge.

## isOpen(UI)

If `true`, the bridge holds the UI.

## isOnTop(UI)

If `true`, the UI is the first of its kind to receive the input.

## getUiList()

Returns the UIs of the bridge sorted by `zindex`, then by opening order.

## drawHover(UI,Object,double,double)

Draws nothing; with the dev jar, prints a warning once per bridge.
Override it to draw tooltips, or extend [[WindowUIBridge]].

## isScreenOpen()

If `true`, the bridge holds a UI that is not an overlay.
JOID never calls it: a host overrides it to answer for its own screens too.

## isOverlayHidden()

If `true`, the overlays that do not ask to stay drawn are neither drawn nor given the input; `false` by default.
Override it to follow a host that hides its own interface.

## drawBackground()

JOID calls it once per frame on the bridge of the lowest priority only, before any UI is drawn; it does nothing by default.
Override it to clear or paint the window.

## hasScreen()

If `true`, the bridge holds a UI that is not an overlay.
