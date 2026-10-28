---
summary: The contract of a UI bridge, the place where JOID opens, holds and closes the UIs of one surface.
---

[[JOID#open]] asks [[UIBridgeRegistry#get]] for a bridge and opens the UI there; the UI stays in that bridge until it closes.
A bridge registered in `BridgeHandler.UI` extends [[UIBridge]], which implements this interface.
[Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own) writes one that takes the panels of the application only.

Learn: [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own), [UI bridge](/build/bridges/ui/).

## open(UI)

JOID calls it on the render thread from `JOID.open(ui)`, on the bridge chosen for the UI.
It decides what the UI replaces, then adds it.

## close(UI)

JOID calls it on the render thread from `JOID.close(ui)` once the close listeners accepted, from `JOID.forceClose(ui)`, and when Escape closes a UI.
It removes the UI from the bridge.

## add(UI)

Adds the UI to the bridge without closing anything.

## remove(UI)

Removes the UI from the bridge, without any close listener.

## isOpen(UI)

If `true`, the bridge holds the UI.

## isOnTop(UI)

If `true`, the UI is the first of its kind to receive the input: screens and popups on one side, overlays on the other.

## canHandle(UI)

If `true`, the bridge accepts to open the UI; it asks `canHandle(Class)` with the class of the UI by default.

## canHandle(Class)

If `true`, the bridge accepts the UIs of that class; it answers from the class alone, so that lookups without an instance give the same answer.

## getUiList()

Returns the UIs that the bridge holds, sorted by `zindex`, then by opening order.

## drawHover(UI,Object,double,double)

JOID calls it while the pointer rests on a node that has a text tooltip, with the content and the pointer position in design pixels of the UI.
It draws the tooltip.

## closeAll()

Disposes and removes every UI of the bridge: their `onClose` listeners (never `beforeClose`) and the `onDetach` listeners of their nodes run, and their stores and remembered fields are saved.
Call it after your loop: [Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends).

## forceOpen(UI)

Closes every UI of the bridge with `closeAll()`, then opens the UI.

## replace(UI,UI)

Disposes the first UI (its `onClose` listeners and the `onDetach` listeners of its nodes run, its stores and remembered fields are saved), removes it and adds the second, without transition and without `beforeClose`.

## getInterfaceScale(UI)

Returns a factor that JOID applies on top of the canvas scaling of the UI, read every frame; `1D` by default.
See [interface scale](/use/learn/describing-the-ui/design-canvas/).
