---
summary: The bridge that creates the audio sources on which JOID plays the sound of videos.
---

The backend registers it in `BridgeHandler.AUDIO`, unless it is built with `audio(false)`; without it, videos play without sound.
The video player calls it on the render thread.
This backend plays the sound through the OpenAL context of its host, at the volume that the host gives each group:

```java file="com/example/backend/EngineBackend.java" region="audio"
```

Learn: [Audio bridge](/build/bridges/audio/), [base-openal](/build/base-modules/openal/).

## createSource(int,int)

Returns a stopped source that plays interleaved 16-bit samples at that sample rate, in hertz, with that number of channels.
