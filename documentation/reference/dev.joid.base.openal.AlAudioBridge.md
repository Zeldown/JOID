---
summary: The audio bridge of OpenAL, whose streaming sources call OpenAL through a binding.
---

```java fragment
final AlAudioBridge audio = AlAudioBridge.create(Lwjgl3AlBinding.inst()).ownContext(false);
```

Learn: [Create the bridge](/build/base-modules/openal/#create-the-bridge).

## create(IAlBinding)

Creates the bridge over the OpenAL binding of your library.

## gain

The function that maps the gain and the audio group of a source to the volume sent to OpenAL; setting it updates every live source.

## ownContext

If `true`, the bridge creates an OpenAL context on the default device when the first source needs one and destroys it when the JVM exits; if `false`, the host owns the context.

## groupVolume(Object,float)

Sets the volume of every source of that audio group, `1F` for 100 %, and updates the live sources.

## createSource(int,int)

Creates a streaming source, after creating the OpenAL context when the bridge owns it and none is current.

## releaseSource(AlAudioSource)

Forgets the source; [[AlAudioSource#delete]] calls it.

## getGroupVolume(Object)

Returns the volume of the audio group, `1F` when none was set.

## getBinding()

Returns the OpenAL binding of the bridge.
