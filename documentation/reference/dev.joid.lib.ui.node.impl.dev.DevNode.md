---
summary: The dev panel of a UI in dev mode, with the inspector, the reload button, the grid and the frame rate.
---
JOID creates one for each UI at its first load in dev mode, at the bottom-right corner, and F3 shows or hides it.
The panel can be dragged on the screen; I turns the inspector on and off, Enter inspects the parent of the inspected node, G shows the grid and R reloads the UI.

Learn: [Dev tools](/use/get-started/dev-tools/).

## create(double,double)

Creates a dev panel whose bottom-right corner sits at `x`, `y`.

## getReloadAnimator()

The animator that flashes the reload button when the UI reloads.
