---
summary: The static registries through which the core of JOID reaches its bridges, one registry per kind of bridge.
---

Each constant is a [[BridgeRegistry]] of one kind of bridge.
Before any backend, JOID registers a [[SystemClockBridge]] in `CLOCK`, a [[DirectThreadBridge]] in `THREAD` and an identity bridge in `SIGNAL_REPLAY`; [[Backend#register]] fills the others.
Your code reaches a registry when it needs a bridge directly, to read one or to add one, as [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own) does.

Learn: [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own), [The bridges](/build/bridges/).

## UI

The UI bridges, which hold the open UIs, receive the input and draw every frame.
Unlike the other registries, JOID uses all of its bridges at once, by priority.

## AUDIO

The audio bridge that plays the sound of videos.
It stays empty when the backend is built with `audio(false)`.

## CLOCK

The clock that animations, tweens, media and drags read.
It holds a [[SystemClockBridge]] until a bridge of higher priority is registered, such as a [[ManualClockBridge]] in a test.

## WINDOW

The window bridge: size of the drawable surface, mouse position, held keys, clipboard and cursor.

## RENDER

The render bridge, which draws every pixel and creates the textures, framebuffers and shaders.

## THREAD

The thread bridge, which names the render thread and runs the tasks of other threads on it.
It holds a [[DirectThreadBridge]] until the backend registers its own.

## SIGNAL_REPLAY

The bridge that maps the names compiled in a reactive expression to the names that exist at run time.
It holds an identity bridge until a host that renames classes registers its own.
