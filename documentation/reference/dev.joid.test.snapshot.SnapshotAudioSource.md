---
summary: An audio source that plays nothing, so that snapshot tests need no sound device.
---

[[SnapshotRunner]] registers an audio bridge that creates them.

Learn: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## SnapshotAudioSource()

Creates a simulated audio source.

## delete()

Does nothing.

## play()

Does nothing.

## stop()

Does nothing.

## pause()

Does nothing.

## gain(float)

Does nothing.

## group(Object)

Does nothing.

## write(short[])

Drops the samples.

## isPlaying()

Returns `false`.

## getBufferedSamples()

Returns `0`.
