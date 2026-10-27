---
summary: The record of the OpenGL states that JOID touched since the guard was entered, with their values before the first change.
---

The first change of a state during a recording reads and keeps its value; `restore()` writes the kept values back, in the order of [[GlStateKey]].
[[JournalGlBinding]] calls its methods for you.

Learn: [The state journal](/build/embedding-in-a-host/#the-state-journal).

## GlStateJournal(IGlBinding,GlCapabilities)

Creates an empty journal that reads and writes the state through the binding of the backend.

## start()

Starts a recording and forgets what it knew of the active unit, the bound textures and the bound vertex array.

## restore()

Stops the recording and writes back every kept value.

## excludeTexture(int)

Never records the parameters of that texture, a texture of JOID.

## includeTexture(int)

Records the parameters of that texture again, once JOID deleted it.

## excludeVertexArray(int)

Never records the attributes of that vertex array, an array of JOID.

## trackTexture(int)

Remembers the texture bound to the active unit.

## trackVertexArray(int)

Remembers the bound vertex array.

## trackActiveTexture(int)

Remembers the active texture unit.

## touch(GlStateKey)

Keeps the value of that state before its first change of the recording.

## touch(GlStateKey,int)

Keeps the value of that state of one container, such as one capability or one buffer target, before its first change of the recording.

## saveTexture()

Keeps the active unit and the texture bound to it.

## saveReadBuffer()

Keeps the read framebuffer and its read buffer.

## saveSampler(int)

Keeps the active unit and the sampler of that unit.

## saveFrameBuffer(int)

Keeps the framebuffers bound to that target: the draw one, the read one, or both.

## saveTextureParameter(int)

Keeps that parameter of the bound texture, unless the texture belongs to JOID.

## saveVertexAttribute(int)

Keeps that attribute of the bound vertex array, with the array and the array buffer, unless the array belongs to JOID.
