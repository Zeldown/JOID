# Testkit

The testkit (`joid-testkit` jar, module `testkit`, JUnit 4) checks that a backend renders JOID correctly. `RenderBridgeContractSuite` checks the render bridge contract in a few seconds, without reference images; `SnapshotSuite` plays scripted scenarios of the demo UIs and compares every capture, pixel for pixel, to references recorded on the same machine. Tools compare backends with each other and show the differences in an interactive report.

## Testing a backend in three classes

Implement `ISnapshotBackend` for your engine, then extend both suites in your tests. This is the snapshot backend of LWJGL 3:

```java
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.Platform;

import dev.joid.impl.lwjgl3.Backend;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;

public final class SnapshotBackend implements ISnapshotBackend {

    private long window;

    @Override
    public void create(final int width, final int height) {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, Platform.get() == Platform.MACOSX ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

        this.window = GLFW.glfwCreateWindow(width, height, "JOID snapshot", 0L, 0L);
        GLFW.glfwMakeContextCurrent(this.window);
        GLFW.glfwSwapInterval(0);
        GL.createCapabilities();
        Backend.register(this.window);
    }

    @Override
    public void frame(final Runnable draw) {
        draw.run();
    }

    @Override
    public SnapshotImage capture(final int width, final int height) {
        final ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
        GL11C.glReadBuffer(GL11C.GL_BACK);
        GL11C.glReadPixels(0, 0, width, height, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixels);
        return SnapshotImage.fromBytes(pixels, width, height, true, false);
    }

    @Override
    public void present() {
        GLFW.glfwSwapBuffers(this.window);
    }

    @Override
    public void destroy() {
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
    }

    @Override
    public String getRenderer() {
        return GL11C.glGetString(GL11C.GL_RENDERER);
    }

}
```

```java
public class SnapshotTest extends SnapshotSuite {

    @Override
    protected ISnapshotBackend createBackend() {
        return new SnapshotBackend();
    }

}
```

```java
public class RenderBridgeContractTest extends RenderBridgeContractSuite {

    @Override
    protected ISnapshotBackend createBackend() {
        return new SnapshotBackend();
    }

}
```

`SnapshotSuite` and `ISnapshotBackend` are in `dev.joid.test.snapshot`, `RenderBridgeContractSuite` in `dev.joid.test.contract`. Snapshot tests need a GPU.

## ISnapshotBackend

| Method | Contract |
|---|---|
| `create(int width, int height)` | Create an offscreen or hidden surface of that size, with an 8-bit stencil buffer, and register at least the render bridge of the backend. The snapshot suite registers its own window, audio and clock bridges afterwards. |
| `frame(Runnable draw)` | Run `draw` inside one frame of the engine, for example between `beginFrame()` and `endFrame()`. |
| `capture(int width, int height)` | Read back the pixels of the area that `viewport(0, 0, width, height)` covers, top row first, as a `SnapshotImage`. |
| `present()` | Present or swap the surface after the capture. |
| `destroy()` | Release the surface. |
| `getRenderer()` | The name of the GPU or renderer. It names the folder of the references. |

`SnapshotImage.fromBytes(ByteBuffer buffer, int width, int height, boolean bottomUp, boolean bgra)` builds an image from 4-byte pixels read back from the GPU: `bottomUp` when the first row of the buffer is the bottom one (OpenGL), `bgra` when the bytes are in BGRA order. The alpha channel is ignored: snapshots are opaque.

| Member of `SnapshotImage` | Description |
|---|---|
| `static read(File)` / `write(File)` | PNG input and output. |
| `getWidth()` / `getHeight()` / `getPixels()` | Size and ARGB pixels. |
| `compare(SnapshotImage reference, int tolerance)` | A `SnapshotDifference`: `getPixels()` counts the pixels whose largest channel difference is above `tolerance`, `getMaximum()` is the largest difference. Images of different sizes differ everywhere. |
| `isSame(SnapshotImage)` | Exact equality. |
| `fill(x, y, width, height, color)` | Fills a rectangle. |

## RenderBridgeContractSuite

The contract suite creates the backend once, on a 64×64 surface, and resets the render state before each test (identity matrix, `ortho(0, 64, 64, 0, 0, 10000)`, full viewport, no framebuffer, no shader, `resetTexture()`, `BlendState.NORMAL`, no depth, no culling, white color, line width 1, no smoothing).

| Test | Checks |
|---|---|
| `drawsVertexColors` | Vertex colors are drawn. |
| `drawsWithTheCurrentColor` | Vertices without color take the current color. |
| `compilesCoreShaders` | Every core shader of `/assets/shaders` compiles: `isActive()` is `true`. |
| `exposesTheLineState` | `getLineWidth()` and `isLineSmooth()` return what was set. |
| `uploadsArgbTextures` | A 2×2 ARGB texture shows its first texel at the top-left corner. |
| `minifiesThroughMipmaps` | Textures are not mipmapped by default; `mipmap(true)`, before or after the upload, makes a minified checkerboard average to grey. |
| `deletesTexturesTwice` | `ITexture.delete()` can be called twice. |
| `restoresTheStateOnPop` | `popState()` restores the framebuffer, the shader, the viewport and the line state. |
| `rendersIntoFrameBuffers` | What is drawn into a framebuffer can be drawn from its texture. |
| `followsTheOpenGlProjection` | `ortho(0, 64, 64, 0, ...)` puts the origin at the top-left corner of the capture. |
| `resetsToAnOpaqueWhiteTexture` | `resetTexture()` binds an opaque white texture. |
| `keepsTranslationsExact` | `getPixelGrid()` follows a scale and a fractional translation exactly. |
| `quantizesAMotionToWholePixels` | `quantize` rounds a motion to whole pixels. |
| `restoresTheMatrixAfterATransformation` | Applying and resetting a `Transformation` leaves the pixel grid unchanged. |

`CoreShaders` (`dev.joid.test.shader`) gives your own tests the core shaders: `getNames()` lists the shader folders of `/assets/shaders`, and `read(String name, ShaderStage stage)` parses one stage into a `ShaderSource`.

## SnapshotSuite

`SnapshotSuite` has one test per scenario: `matchesStaticSnapshots`, `matchesInteractionSnapshots`, `matchesTransitionSnapshots`, `matchesPopupSnapshots`, `matchesWindowSnapshots`, `matchesDevSnapshots` and `matchesResourceSnapshots`. The scenarios are part of the testkit jar, in `/snapshot/<name>.txt`, and drive the demo UIs of `dev.joid.demo`, so the snapshot suite needs the `dev` jars.

### A deterministic environment

When the first test starts, the suite creates the backend on a 1920×1080 surface and sets up:

| Part | Setup |
|---|---|
| Clock | A `ManualClockBridge` at 1,735,689,600,000 ms (2025-01-01 00:00 UTC). |
| Window | A virtual window bridge: size, mouse position, held keys and clipboard set by the scenario. |
| Audio | A silent audio bridge. |
| URLs | A locator that downloads each URL once into the cache folder, named after the SHA-1 of the URL, so later runs work offline. |
| JOID | Loaded with dev mode off and demo mode on. |
| UI bridge | The demo UI bridge, with an interface scale the scenario can change. |

Every scenario then starts from the same state: no UI, no key held, no mask, the clock back at its start, dev mode off and a 1920×1080 window. Time only moves with `wait` and `moveto`, in frames of 16 ms, so lerps, the frame time and the FPS counter give the same values on every run.

Before each shot, the suite renders until two consecutive frames are identical, every resource of the default cache that started decoding is loaded, and every decoder is settled (an animation shows the frame of the clock, an SVG has no raster pending). Videos and animated images follow the clock, so they are captured at a known frame.

| Failure | Meaning |
|---|---|
| `<shot>: <n> pixels differ from the reference, maximum channel delta <m>` | The capture does not match its reference. The message ends with the link of the report. |
| `<shot> never became stable while the clock was paused, <n> pixels kept changing` | The picture changes although time does not move: something reads the system time instead of the clock bridge. |
| `<shot> never settled: a resource kept decoding or rendering` | A decoder stayed unsettled. |
| `A resource never finished loading` | A resource was still decoding after 30 seconds. |
| `Unknown snapshot command: <line>` | A scenario line is wrong. |

## References, renders and settings

| Folder | Content |
|---|---|
| `<references>/<renderer>/<shot>.png` | The references. `<renderer>` is `getRenderer()` with every run of characters other than letters and digits replaced by `-`, without a leading or trailing `-`. |
| `<output>/<shot>.png` | The renders of the last run. |
| `<output>/report.html` and `<output>/report/` | The [report](#the-report). |
| `<cache>/` | The downloaded URLs. |

References belong to one machine and one GPU: keep them out of version control. A shot without reference is recorded as the reference on its first run. In update mode, every shot replaces its reference. After the suite, references whose name is not a shot of the scenarios are deleted, in every renderer folder (`Removed orphan reference <file>`).

The folders come from system properties, with defaults relative to the working directory:

| Property | Default |
|---|---|
| `joid.snapshot.references` | `.snapshots/references` |
| `joid.snapshot.output` | `build/snapshots/renders` |
| `joid.snapshot.cache` | `.snapshots/cache` |
| `joid.snapshot.update` | `false`; `true` replaces the references |

## Running the snapshots of JOID

In the JOID repository, each backend module runs its suites with these settings: references in `.snapshots/<module>`, renders in `build/snapshots/<module>`, cache in `.snapshots/cache`, one JVM per test class.

| Command | Result |
|---|---|
| `./gradlew test` | Every unit, contract and snapshot test. |
| `./gradlew :vulkan:test` | The tests of one backend. |
| `./gradlew updateSnapshots` | Runs the snapshot tests and replaces the references; `./gradlew :lwjgl3:updateSnapshots` updates one backend. |
| `./gradlew crossBackendTest` | Runs the tests of the three backends, then compares the LWJGL 3 and Vulkan renders to the LWJGL 2 ones within one level per channel. The report is written to `build/snapshots/cross/report.html`. |
| `./gradlew installLocalGitHook` | Installs the `pre-commit` and `pre-push` hooks of `scripts/`. `./gradlew build` installs them too. |

When a test fails, the build prints the link of the report.

### Git hooks

Both hooks run `scripts/run-tests`, which tests only what the changes touch:

- Nothing runs when no file of `core/`, `msdf/`, `impl/`, `testkit/`, `gradle/`, `build.gradle` or `settings.gradle` changed.
- A change in `core/`, `msdf/`, `testkit/`, `gradle/` or the build files runs the checks of `msdf`, `core` and `testkit` and the tests of every backend; a change in `impl/<backend>/` tests that backend; a change in `impl/glfw/` or `impl/openal/` tests LWJGL 3 and Vulkan.
- A backend that is not tested keeps its last renders when it has some, then `crossBackendTest` compares them all, offline.
- The `pre-commit` hook tests the staged changes alone: it stashes the rest, including untracked files, and restores it afterwards. The `pre-push` hook tests the pushed commits, unless the `pre-commit` hook already tested each of them.

## Comparing backends

Two command-line tools, in `joid-testkit`, let any build tool render and compare backends:

| Main class | Arguments | Result |
|---|---|---|
| `dev.joid.test.snapshot.SnapshotBaseline` | `<ISnapshotBackend class> <output directory>` | Renders every shot of every scenario with that backend (created with its no-argument constructor) into the directory. |
| `dev.joid.test.snapshot.SnapshotComparison` | `<report directory> <reference directory> <candidate directory>...` | Compares each candidate directory to the reference one within one level per channel, writes `report.html` into the report directory, and exits with status 1 when a shot differs or is missing. |

The [backend template](writing-a-backend.md#starting-from-the-template) uses them to compare your backend to the official LWJGL 3 rendering.

## The report

`report.html` opens straight from the disk, without a server. It lists the shots with their status (different, recorded or updated, identical) and their number of different pixels, with filters and a search field, and shows the selected shot in eight modes:

| Key | Mode |
|---|---|
| `1` | Side by side |
| `2` | Swipe |
| `3` | Onion skin |
| `4` | Blink (`Space` toggles the image) |
| `5` | Difference |
| `6` | Highlight of the different pixels |
| `7` | Reference |
| `8` | Render |

The wheel zooms around the cursor, dragging pans, `F` fits the image and `0` shows it at 1:1. Hovering a pixel shows its coordinates, both colors and the difference of each channel. `N` and `P` jump to the next and previous groups of different pixels, so even a single pixel is found. `Up` and `Down` select the previous and next shot, and the address `report.html#<shot>` opens a shot directly.

## Writing a scenario

A scenario is a text file with one command per line; empty lines and lines starting with `#` are ignored.

| Command | Effect |
|---|---|
| `ui <class>` | Closes every UI, adds a new instance of the UI class to the bridge, moves the mouse out of the window, and waits for a stable frame. |
| `open <class>` | Opens a new instance through `JOID.open`, with its transitions and popup rules, and waits for a stable frame. |
| `wait <ms>` | Advances the clock by frames of 16 ms, rendering each one. |
| `move <x> <y>` | Moves the mouse, in window pixels. |
| `moveto <x> <y> <ms>` | Moves the mouse progressively over that time, frame by frame, dragging while a button is pressed. |
| `press <button>` / `release` | Presses a `ClickType` (`LEFT`, `RIGHT`...), releases it. |
| `scroll <value>` | Scrolls, `120` per notch, negative downward. |
| `type <text>` | Types the text, holding `LEFT_SHIFT` for capital letters and shifted symbols. |
| `key <KEY>[+<KEY>...]` | Holds every key of the combination, sends the last one, then releases them, for example `key LEFT_CONTROL+A`. |
| `down <KEY>` / `up <KEY>` | Holds or releases a key for the following commands. |
| `resize <width> <height>` | Resizes the window, up to 1920×1080. |
| `zoom <level>` | Loads every opened UI again at that zoom. |
| `scale <factor>` | Sets the interface scale of the UI bridge. It is not reset between scenarios: end with `scale 1`. |
| `dev <true\|false>` | Turns dev mode on or off. A UI creates its dev overlay when it loads, so open the UI after `dev true`. |
| `mask <x> <y> <width> <height>` | Paints the rectangle in magenta in the following shots, to exclude content that cannot be deterministic. |
| `unmask` | Removes the masks. |
| `shot <name>` | Captures the window as `<name>.png`. |

```text
ui dev.joid.demo.ui.resource.UIDemoResource
wait 1500
shot resource-start
move 270 704
press LEFT
release
key SPACE
wait 500
shot resource-paused
```

Scenarios belong to the testkit. To add one to JOID, create `testkit/src/main/resources/snapshot/<name>.txt`, add `<name>` to the scenario list of `SnapshotRunner` and a `matches<Name>Snapshots` test to `SnapshotSuite`: the shots of a scenario missing from the list are recorded, then deleted as orphans. Run the tests once to record the new references.

## See also

- [Writing a Backend](writing-a-backend.md) — the template and the render contract the suites check.
- [Backends](backends.md) — the official backends and their snapshot backends.
- [Bridges](bridges.md#controlling-time-with-manualclockbridge) — the manual clock behind deterministic captures.