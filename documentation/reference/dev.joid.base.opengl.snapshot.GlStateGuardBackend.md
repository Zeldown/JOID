---
summary: The base of the state guard backends of OpenGL: the surface of a snapshot backend, the traps each context supports and the state read through a binding.
---

A subclass sets the traps and draws like a host with the OpenGL functions of its library; [[Lwjgl2StateGuardBackend]] and [[Lwjgl3StateGuardBackend]] are the two official ones.
It ships in the `snapshot` jar of base-opengl and in the dev and demo jars of the backends; the published `joid-base-opengl` jar does not hold it.

Learn: [Tools for hosts and tests](/build/base-modules/opengl/#tools-for-hosts-and-tests).

## GlStateGuardBackend(ISnapshotBackend,Supplier)

Creates the backend over that snapshot backend; the supplier gives the binding through which it reads the state, once the snapshot backend has created its context.

## create(int,int)

Creates the surface of the snapshot backend.

## destroy()

Destroys the surface of the snapshot backend.

## present()

Presents the surface of the snapshot backend.

## capture(int,int)

Captures the surface of the snapshot backend.

## getRenderer()

Returns the renderer of the snapshot backend.

## inject(StateTrap)

Sets that trap through `injectTrap`; `EVERYTHING` sets every trap that the context supports.

## supports(StateTrap)

If `true`, the context can set the trap: the fixed-function traps need a compatibility context, the others the capability they use, read from the [[GlRenderBridge]] registered.

## readState()

Returns the state of the context, read by [[GlStateSnapshot]] with the materials of `readMaterial`.

## isCompatibility()

If `true`, the context has a compatibility profile.

## getCapabilities()

Returns the capabilities of the [[GlRenderBridge]] registered.

## injectTrap(StateTrap)

Sets that trap, never `EVERYTHING`, in the state of the context.

## readMaterial(int,int,float[])

Writes the four values of that material parameter of that face into the array.
