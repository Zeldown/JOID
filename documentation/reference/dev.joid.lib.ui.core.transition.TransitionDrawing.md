---
summary: Function that draws a UI for a progress of its transition step, around the body that draws its nodes.
---
Give it to [[TransitionStep#draw]].
It runs on the render thread, at every frame of the step.

Learn: [Transitions](/use/learn/style-and-motion/transitions/#write-a-transition-of-your-own).

## draw(UI,float,Runnable)

Draws the UI at this progress, from `0` to `1`: change the matrix stack or the render state, then run the body once, which draws every node of the UI.
