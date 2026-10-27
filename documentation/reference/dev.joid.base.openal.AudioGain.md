---
summary: A function that maps the gain and the audio group of a source to the volume sent to OpenAL.
---

A host returns the gain multiplied by its own volume for the group.

Learn: [Create the bridge](/build/base-modules/openal/#create-the-bridge).

## apply(float,Object)

Returns the volume to send to OpenAL for that gain and that audio group, which is `null` for a video without group.
