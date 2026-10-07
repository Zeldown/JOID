# JOID showcase

Every designed scene of the README showcase, as a runnable module on the Vulkan backend. The module is not published: it is not part of the release jars, of the backend template or of the classpath of the core and the backends.

## Run it

```
./gradlew :showcase:run
```

`runShowcase` does the same. A window opens on a menu that lists the thirteen scenes; click a card to open one. Every scene is live: drag, scroll, type and click as in any application.

| Key | Action |
| --- | --- |
| Page Down | Next scene |
| Page Up | Previous scene |
| Esc | Back to the menu |
| F3 | Dev panel (the dev mode is on) |

The scenes are drawn on the 1920×1080 canvas and fit any window size.

## Layout

- `src/main/java/dev/joid/showcase`: the scenes (`ShowHero`, `ShowReactive`, `ShowType`, `ShowEffects`, `ShowMotion`, `ShowForm`, `ShowPlayer`, `ShowModel`, `ShowLists`, `ShowDrag`, `ShowTheme`, `ShowInspect`, `ShowDesign`), their base `ShowUI`, their nodes, kits and text effects, the menu `ShowMenu`, the scene list `ShowScene`, the window `ShowcaseWindow` and the headless renderer `ShowcaseRender`.
- `src/main/resources/assets/showcase`: the video and the colored teapot texture.
- `tools`: the scripts that render the README media.

## Render the README media again

The tools need Python 3 with Pillow, and FFmpeg for the video (the `FFMPEG` variable, the `PATH` or a WinGet install). They render on the Vulkan snapshot backend with a deterministic clock, so the same script always gives the same frames.

```
cd showcase/tools
python showcase.py all          # frames of every clip in showcase/build/frames/<clip>
python compose.py webp all      # documentation/content/images/showcase-<clip>.webp
python compose.py mp4           # documentation/content/images/showcase.mp4
```

- `showcase.py <clip> [first] [last]` renders one clip, or a range of its frames. The clips are `hero`, `reactive`, `type`, `effects`, `motion`, `form`, `player`, `model`, `lists`, `drag`, `theme`, `inspect` and `design`; each one is a mouse and keyboard script written in the file.
- `probe.py <clip> <frame> [frame ...]` renders a few frames on a contact sheet in `showcase/build/probe`, to check a script before rendering a whole clip.
- `compose.py` draws the pointer over the frames, then writes the WebP loops of the README and the crossfaded 1080p video.
- `render.py <scenario.txt> <folder>` renders any testkit scenario with the showcase classes, one PNG per `shot`.
- `teapot.py` generates the teapot model and texture of the demo, and the colored texture of the showcase.

The first render builds the module with Gradle (`:showcase:renderClasspath`), so do not run it while another Gradle build is running on the project.