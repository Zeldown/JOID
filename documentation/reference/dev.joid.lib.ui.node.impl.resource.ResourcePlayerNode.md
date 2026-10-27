---
summary: Plays a video or an animated image in its bounds, with its sound, and controls the playback.
---
Each player plays its own copy of the resource, made when the node is attached, so two players of the same resource play separately.
Before that copy exists, `play`, `pause`, `stop`, `restart` and `seek` are kept and applied when the playback starts; `getPlayback()` and `getVideo()` return `null`.
The node waits for its resource like a [[ResourceNode]] and takes its size the same way.

Learn: [Images and media](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## create(double,double)

Creates a player at `x`, `y` that takes the size of its resource.

## create(double,double,double,double)

Creates a player with a declared size; a size of `0` on an axis follows the ratio of the resource.

## ResourcePlayerNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## resource

The video or animated image to play.
Another resource restarts the playback; the same resource with the same properties keeps it.
Throws an `IllegalStateException` when the node is attached without one.

## stretch

How the frames fill the bounds.

## autoplay

If `true`, the playback starts as soon as the resource is ready; if `false`, it waits paused on its first frame, an animated image as a video.

## loop

If `true`, the playback starts again at the end instead of ending.

## volume

Volume of the sound of a video, from `0` for silence to `1` for the full volume.

## audioGroup

Group of the sound of a video, for the audio bridge to set the volume of several players at once.

## location

Position of the sound of a video, whose volume falls with its distance to the position of [[VideoAudioPlayer#audioListenerPosition]]; `null` plays it without position.

Default: none, the sound plays without position

## referenceDistance

Distance from `location` within which the sound plays at full volume; `null` keeps 5.

Default: 5, the distance of the video decoder

## maxDistance

Distance from `location` from which the sound is silent; in between, the volume falls as `(1 − t)²`, `t` going from 0 to 1 between the two distances; `null` keeps 50.

Default: 50, the distance of the video decoder

## play()

Starts the playback, or resumes it after a pause or a stop, and fires `onPlay`.
Throws an `IllegalStateException` when the resource has no playback, such as a still image.

## pause()

Pauses a playing resource and fires `onPause`.

## stop()

Stops the playback, back at the start, and fires `onStop`.

## toggle()

Pauses a playing resource, otherwise plays it.

## restart()

Plays the resource again from the start.

## seek(double)

Moves the playback to this time, in seconds.
A stopped resource pauses at that time and shows its frame.

## getDuration()

Length of the resource, in seconds; `0` before the playback exists.

## getProgress()

Share of the resource already played, from `0` to `1`.

## getPlayback()

The playback of the copy that this node plays, or `null` before the node is attached or when the resource has none.

## getVideo()

The video decoder of the copy that this node plays, or `null` when the resource is not a video or the node is not attached.

## isPaused()

If `true`, the playback is paused.

## isPlaying()

If `true`, the playback is playing.

## update()

Measures the resource on every frame, so that the node takes its size as soon as it loads.

## draw(double,double)

Starts the playback on the first frame, fires the playback events, then draws the current frame.

## detach()

Releases the copy that this node plays and its sound; attaching the node again starts from the beginning.

## onPlay(EventListener)

Adds a listener that runs when the playback starts or resumes.

## onPause(EventListener)

Adds a listener that runs when the playback pauses.

## onStop(EventListener)

Adds a listener that runs when the playback stops, by `stop()` or at the end of a resource that does not loop.

## onEnd(EventListener)

Adds a listener that runs when a resource that does not loop reaches its end, before `onStop`.

## onProgress(EventListener)

Adds a listener that runs on every frame where the progress changes, with the progress and the current time.
