---
summary: Saves the OpenGL state that JOID changes during a frame and gives it back to the host when the frame ends.
---

Entries nest: only the outermost `enter()` starts the journal and resets the pipeline for JOID, and only the matching `exit()` restores the state of the host.

Learn: [The state journal](/build/embedding-in-a-host/#the-state-journal), [Check the guard](/build/embedding-in-a-host/#check-the-guard).

## GlStateGuard(IGlBinding,GlCapabilities)

Creates the guard and its journaled binding over the binding of the backend.

## enter()

Enters the guard; the outermost entry starts the journal and resets what a host may leave on: scissor, smooth lines, logic op, polygon offset, sRGB, primitive restart, depth clamp, the fixed pipeline, the clear values, the faces, the line width and the pixel storage.

## exit()

Leaves the guard; the outermost exit writes back every state that JOID changed since `enter()`.

## run(Runnable)

Runs the action between `enter()` and `exit()`.

## call(Supplier)

Returns the result of the action, run between `enter()` and `exit()`.

## suspend(Runnable)

Gives the host its state back, runs the drawing of the host outside the guard, then enters again with the same depth.

## getBinding()

Returns the journaled binding through which every call must go.
