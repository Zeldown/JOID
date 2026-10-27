---
title: "Audio bridge"
description: "The contract of the audio bridge: streaming sources of 16-bit samples for the sound of videos."
genre: build
sidebar:
  order: 3
---

The audio bridge plays the sound of videos.
JOID decodes the audio itself and streams interleaved signed 16-bit samples into sources that the bridge creates.
The core reads it through [[IAudioBridge]], from [[BridgeHandler#AUDIO]].

The bridge is optional: a backend whose `createAudioBridge()` returns `null`, or an application that calls `audio(false)` on the backend, registers none, and each video plays without sound, with one warning.

## Required

[[IAudioBridge]] has one method: [[IAudioBridge#createSource]] returns an [[IAudioSource]] for a sample rate and a channel count.
Each source implements:

| Member | What it does |
|---|---|
| [[IAudioSource#write]] | Appends interleaved samples to the queue of the source. |
| [[IAudioSource#getBufferedSamples]] | Counts the samples written and not played yet. |
| [[IAudioSource#play]], [[IAudioSource#pause]], [[IAudioSource#stop]] | Starts or resumes, pauses, or stops and drops the queue. |
| [[IAudioSource#isPlaying]] | Whether the source is playing. |
| [[IAudioSource#gain]] | Sets the volume, `1F` for 100 %; `0F` mutes the source and keeps its samples flowing. |
| [[IAudioSource#group]] | Receives the audio group that the application gave the video, or `null`. |
| [[IAudioSource#delete]] | Releases the source. |

## Provided

[[AudioDownmix#stereo]] mixes 3 to 8 channels down to stereo with the usual weights, for an API that plays at most two channels; it returns stereo and mono samples unchanged.

## Guarantees

| Rule | Why |
|---|---|
| Keep playing while samples arrive: when the queue ran dry and `write` adds samples to a playing source, play again. | The player writes ahead of the clock, but a slow frame can empty the queue. |
| Count `getBufferedSamples` in interleaved samples of at most two channels: after a downmix, count the stereo samples you queued. | The video player keeps about eight blocks of samples ahead of the clock with that count. |
| Map the group to a volume of your host. | An application tags a video with a group (music, voices); the host decides what each group weighs. |
| Expect every call on the render thread. | The player calls the bridge and its sources from the frame, so they need no lock. |

On OpenAL, [base-openal](/build/base-modules/openal/) gives a complete bridge, and its `gain(...)` maps the groups to the volumes of your host.
