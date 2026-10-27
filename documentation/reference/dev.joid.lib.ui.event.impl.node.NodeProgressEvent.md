---
summary: The event of `onProgress` on a resource player, with the progress of the playback.
---

[[ResourcePlayerNode]] fires it on the frames where the progress of its playback changed.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## create(N,double,double)

Creates the event for a node, the progress and the current time.

## getProgress()

Returns the part of the media played, from `0D` at the start to `1D` at the end.

## getCurrentTime()

Returns the position of the playback, in seconds.
