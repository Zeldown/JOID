---
title: "Show a video"
description: "Play a looping video with its sound, a progress bar and a play and pause button."
genre: guide
sidebar:
  order: 2
---

A video is a resource that a player node draws and plays, with its sound.
This guide shows a looping video, a bar that follows its progress, and a button that pauses and plays it.

## What you will build

<figure>

![An 800 by 450 video playing in the center of the screen, a thin grey progress bar under it that grows with the video, and a white button that the pointer clicks twice: it reads Pause, then Play while the video stops, then Pause again](/captures/guides-video.webp)

<figcaption>
The bar and the label of the button follow the video on every frame.
Video: an excerpt of <a href="https://studio.blender.org/films/charge/">Charge</a> © Blender Foundation, cropped to 16:9, <a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>.
</figcaption>
</figure>

## Before you start

- Resources, the video formats JOID reads and the player node: [Images and media](/use/learn/describing-the-ui/media/).
- Values that follow another value on every frame: [Signals](/use/learn/interactivity/signals/).
- A backend with audio, the default: [Choosing a backend](/use/learn/running/backends/).
- A video file in the resources of your application, here `src/main/resources/videos/intro.mp4`.

## 1. Add the player

[[ResourcePlayerNode]] draws a resource that plays: an animated image or a video.
Give it the video, then the playback options:

```java file="com/example/guides/VideoGuideUI.java" region="player"
```

`CONTAIN` keeps the proportions of the video inside the node, `loop(true)` starts it again at the end, and `volume(0.8F)` plays the sound at 80 %.
The variable `player` keeps the node for the next steps.

The first video that a machine plays makes JOID download its FFmpeg natives into the cache folder; until they are ready, the node shows its skeleton.
[Play videos without network access](/use/get-started/installation/#play-videos-without-network-access) shows how to ship them with your application instead.

**Check:** run the UI: the video plays in the center of the window, with its sound, and starts again when it ends.

## 2. Show the progress

`getProgress()` returns how much of the video has played, from `0` to `1`.
A width given as a lambda reads it on every frame and turns it into a bar:

```java file="com/example/guides/VideoGuideUI.java" region="progress"
```

**Check:** the grey bar grows under the video, and empties when the video loops.

## 3. Pause and play

`toggle()` pauses the video on its current frame, or plays it again from there.
The label reads `isPlaying()` on every frame, so it always names the next action:

```java file="com/example/guides/VideoGuideUI.java" region="button"
```

**Check:** click the button.
The video and its sound stop, the bar stops growing and the label reads `Play`; a second click plays it again.

[Images and media](/use/learn/describing-the-ui/media/) lists the other controls of the player, such as `seek` and `restart`, and its listeners.

## If it does not work

1. A magenta and black checkerboard shows instead of the video, or nothing with the prod jar: the file cannot be read, and the dev jar prints `[JOID] The resource … cannot be read: …` with the cause.
   Check that the file is in the resources, under that path from their root (a leading `/` changes nothing).
2. The error output prints `[JOID] The FFmpeg natives of … cannot be downloaded from …`: the machine has no access to the repository, and every video fails; put the natives on the classpath ([Play videos without network access](/use/get-started/installation/#play-videos-without-network-access)).
3. The video plays without sound: the backend was registered with `audio(false)`, or the volume is `0F`.
4. The video starts before the user asks: add `autoplay(false)` and call `play()` from a click.
5. The picture is stretched: the default [[ResourceNode.StretchType#STRETCH]] fills the node; `CONTAIN` keeps the proportions, and `COVER` fills the node and crops the rest.

## Reference

- [[ResourcePlayerNode]]
- [[Resource]]
- [[ResourceNode.StretchType]]
