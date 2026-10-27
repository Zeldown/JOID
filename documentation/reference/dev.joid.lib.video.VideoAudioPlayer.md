---
summary: Plays the sound of one video through the audio bridge, in step with its frames.
---

Each [[VideoResourceDecoder]] creates its own when the video has sound and an audio bridge is registered; you only call `audioListenerPosition`.
The volume is the volume of the video times 0.3, and with a location, times the falloff of the distance to the listener.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## VideoAudioPlayer(int,int,VideoResourceDecoder)

Creates the player of `owner`, for `channels` channels at `sampleRate` samples per second.

## audioListenerPosition(AudioListenerPosition)

Sets where the listener stands for every video placed with a location, such as the camera of your scene; `null` puts it at `(0, 0, 0)`.
Set it back to `null` when the screen that set it closes.

## cleanup()

Stops and deletes the audio source and drops the waiting samples.

## play()

Creates the audio source on its first call, then starts the sound from the samples to come; without an audio device, it warns once and the video plays silent.

## stop()

Stops the sound.

## pause()

Pauses the sound.

## resume()

Resumes the sound after `pause()`.

## flush()

Drops the waiting samples and stops the source, after a seek.

## update(double)

Sets the volume and the group of the source, then feeds it the samples for `elapsed` seconds of playback, skipping the samples the picture is ahead of; the decoder calls it on each draw.

## pushSamples(Buffer[],int)

Queues decoded samples, the first `skip` frames of samples dropped; the decoding thread calls it.

## isFull()

If `true`, the queue of samples is full, so the decoder waits.
