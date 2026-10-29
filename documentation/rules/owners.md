# Notion → owner page registry

Every notion is explained on ONE page. Any other page: one sentence max + link to the owner anchor.
A guide never redoes a Learn chapter: it is a short task that links the Learn page of each notion it uses.

A writer who needs a notion that is not listed adds a row here (owner = the page that teaches it). Paths are under `documentation/src/content/docs/`.

## Use JOID
| Notion | Owner page |
|---|---|
| What JOID is, who it is for, the two documentations | use/index (Introduction) |
| Installing JOID with a backend (Gradle), the dev and prod jars (dev tools only, never demos), the FFmpeg natives on the classpath for a machine without network access (section "Play videos without network access"; media keeps the download on demand) | use/get-started/installation |
| The 80 % tour | use/get-started/quick-start (teasers only, links everywhere) |
| How UIs stack: `zindex`, opening order, overlays on top, input from the top down, with the only stacking diagram | use/learn/describing-the-ui/overlays-and-popups (section "See how UIs stack"); uis, events, frame-lifecycle and build/bridges/ui give one sentence and a link |
| UI class, lifecycle (init, open, close, reload, renew), `JOID.open/close`; `@UIData` and every UI annotation option (background, closeable, zoomable, `zindex`…), except `projection` and `zlevel` | use/learn/describing-the-ui/uis (section "Configure a UI" for the options), reference for the full list |
| UI `projection` and `zlevel` (drawing a UI inside the projection of a host) | use/learn/running/embedding (section "Draw a UI inside your own projection"); uis gives one sentence and a link to that section |
| Overlays (`@UIDataOverlay`, `@UIDataOverlayRender`, `@UIDataOverlayInteraction`) | use/learn/describing-the-ui/overlays-and-popups |
| Popups (`@UIDataPopup`), modal dimming | use/learn/describing-the-ui/overlays-and-popups |
| Node, the tree, attach/detach, body, create pattern, fluent setters | use/learn/describing-the-ui/nodes |
| Visual catalog of every user node and control (generated cards: capture, one line, links to its Learn page and reference) | use/catalog (sidebar entry, linked from the Describing the UI opener) |
| Value vs `Supplier` setters (reactive setters) — the reasoning question, always worded "When does JOID compute this value again?" | use/learn/interactivity/signals (nodes page links to it) |
| Design canvas, scaling, anchors, view, interface scale, `@UIDataScale`, UI zoom (`UI.zoom`) | use/learn/describing-the-ui/design-canvas |
| Layout: container (`ContainerNode`), overflow and scroll (`OverflowProperty`, scrollbars, `beforeScroll`/`onScroll`/`onScrollToEnd`/`onScrollEndReached`), the layout rule | use/learn/describing-the-ui/layout |
| Flex layout (`FlexNode`) | use/learn/describing-the-ui/flex-layout |
| Grid layout (`GridNode`) | use/learn/describing-the-ui/grid-layout |
| Text, `TextNode`, `TextInfo` | use/learn/describing-the-ui/text |
| Fonts: loading a font (`MsdfFontLoader`), MSDF vs bitmap (user side) | use/learn/describing-the-ui/fonts |
| Text markup (`ITextMarkup`, `TextMarkup`), text styles (`TextStyle`), text effects (`ITextEffect`) and modifiers (`TextModifier`), text converters (`ITextConverter`, `TextConverter`) | use/learn/describing-the-ui/text-markup-and-effects |
| Images, resources, assets, playback (video/audio), `ResourcePlayerNode`, `StretchType`, the FFmpeg download (`VideoNatives`); preloading a resource before its first draw (`Resource.generate`, section "Know when an image is loaded"); the supported formats (image, animated, vector, video, audio), listed in this place only | use/learn/describing-the-ui/media (section "Supported formats" for the formats) |
| Showing a texture of the application (a resource of `GlBorrowedTexture.of` on LWJGL 2 and 3, `VulkanBorrowedTexture.of` on Vulkan) | use/learn/describing-the-ui/media (section "Show a texture of your application"); build/resource-resolvers keeps writing a resolver and links to it |
| Events: the three kinds of event methods (the prefix handle for the raw input, before for a change that a listener can cancel, on for what happened), the path of an input through the tree, consuming an input, cancelling a change, listener priorities and `MONITOR`, interactive(false) (routing to the bridges: one sentence and a link to running/embedding) | use/learn/interactivity/events |
| Hover, `isHovered`, tooltips and hover elements, hover progress (`hoverValue`, `hoverDuration`, `hoverEquation`) | use/learn/interactivity/hover-and-tooltips |
| Drag and drop, `DraggableProperty` | use/learn/interactivity/drag-and-drop |
| Keyboard, `Key`, `handleKeyPressed`/`handleCharTyped`, focus | use/learn/interactivity/keyboard |
| Escape closes the top UI when it is closeable and no node consumed the key | use/learn/interactivity/keyboard (section "Know who receives the keyboard"); uis gives one sentence and a link |
| Controls pattern (values/value/range, onChange, signal binding) | use/learn/interactivity/controls |
| Signals, `get`/`peek`, typed signals, collections, never changing the collection that `get()` returns | use/learn/interactivity/signals |
| Derived values (`map`, `Signal.from`), batch, your own code on a change (onChange and removeOnChange of a signal) | use/learn/state/derived-values |
| `watch`, `wait`, skeleton, rebuilding the tree | use/learn/state/rebuilding |
| Stores (`UIStore`, `UIStoreScope`), `@UIProperty` | use/learn/state/stores-and-properties |
| Tasks (`Task`): code run on a period or once after a delay, attached to a UI or a node | use/learn/state/tasks |
| Writing a signal from another thread (applied at the next frame on the render thread) | use/learn/state/tasks (section "Set a signal from another thread"); running/threads gives one sentence and a link |
| Colors, styling, gradients | use/learn/style-and-motion/styling |
| Node effects (rounded, border, shadow, blur, mask, transform…) | use/learn/style-and-motion/effects |
| Animation (animators, easing, tween engine — choose then use), following a moving target (`UI.lerpByFramerate`) | use/learn/style-and-motion/animation |
| UI transitions (in/out, custom transitions), the values of `PopupTransition` | use/learn/style-and-motion/transitions (overlays-and-popups and the popup guide give one sentence and a link) |
| Custom nodes (draw, hooks, callbacks of your own) | use/learn/going-further/custom-nodes |
| Drawing API: `DrawUtils` shapes and text | use/learn/going-further/drawing |
| Drawing resources, matrix stack, render state and blending (`Transformation`, `MatrixStack`, `RenderState`, `BlendState`) | use/learn/going-further/drawing-resources-and-transforms |
| `FrameBuffer`, `FrameBufferPool` (section "Borrow a temporary target"), `DrawModel`, `Tessellator` | use/learn/going-further/framebuffers-and-models (loading an `ObjModel`: one sentence and a link to 3d-models) |
| The texture of a resource in your own drawing (`Resource.bind`, `Resource.getTexture`, `Resource.prepareBind`) | use/learn/going-further/framebuffers-and-models (section "Use the texture of a resource"); fonts give one sentence and a link |
| Shaders for users (shader passes, uniforms, writing a GLSL pass) | use/learn/going-further/shaders |
| The JOID GLSL language: its rules and the list of built-in inputs, outputs and functions | use/learn/going-further/shaders (section "Write JOID GLSL"); build/shader-translation links to it for the list |
| Custom effects (`NodeEffect`) | use/learn/going-further/custom-effects |
| Custom fonts (`IFont`, `BitmapFont`, `BitmapCell`, `BitmapTextRenderer`, `GlyphFont`, `GlyphTextRenderer`), MSDF sources (`IMsdfSource`, `MsdfSource`, `OpenTypeMsdfSource`, `BinaryMsdfSource`) and the MSDF generator (`MsdfGenerator`, user side) | use/learn/going-further/fonts |
| Custom resource formats and decoders, asset locators (`IAssetLocator`, `AssetLocator`) | use/learn/going-further/custom-formats |
| Choosing and installing a backend (prerequisites and capabilities per backend of JOID's own backends lwjgl2, lwjgl3 and vulkan: JDK, GL/Vulkan versions, the capabilities table), the whole sequence of `register()` (section "Know what happens at register()": bridges, thread bridge, default UI bridge, input, data and cache folders, loading), the builder settings, `JOID.onReady` (section "Run code once JOID is ready") | use/learn/running/backends (build/writing-a-backend keeps only the hook to override at each step; the `Backend` reference and the other pages link and never list the sequence again) |
| Closing the UIs before the program ends (`closeAll()`), so that stores and remembered fields reach the disk, with the only loop that shows it | use/learn/running/backends (section "Close the UIs before the program ends"); every other page gives one sentence at most and links to that anchor |
| Running JOID inside your own loop/host (user side of embedding), drawing JOID over the scene of the host (a UI bridge that keeps the scene, registered before the backend) | use/learn/running/embedding (build/embedding-in-a-host keeps only `isMouseGrabbed()` of the window bridge of a host, shows no UI bridge and links here for its hooks; following the screens that JOID opens, `attachScreen`/`detachScreen`, is taught in use/learn/running/embedding, section "Follow the screens that JOID opens", and the Build page links to it) |
| Testing your UIs with the testkit (snapshots of your screens) | use/learn/running/testing |
| The clock of JOID (`BridgeHandler.CLOCK`): what follows it, and reading the time there rather than on the system | use/learn/running/testing (section "Keep every run identical"); writing a clock belongs to build/bridges/clock; every other page gives one sentence and a link |
| Every command of a snapshot scenario | reference (SnapshotRunner page, generated); the testing page shows the commands of its example and links to it |
| Dev mode (`JOIDEnv.devMode`), inspector, hot reload, the profiler (`@UIDataDebug`, `getDebug()`), warnings (and the warnings catalog link) | use/get-started/dev-tools |
| Frame lifecycle: the steps of one frame | use/in-depth/frame-lifecycle (embedding and the Build pages give one sentence and a link; build/bridges/render keeps only `beginFrame`/`endFrame`) |
| How signals track reads | use/in-depth/how-signals-work |
| Pixel alignment, edge smoothing (`EdgeSmoothing`) | use/in-depth/pixel-alignment |
| MSDF text rendering, the atlas that the first load of a font generates and its cache | use/in-depth/msdf-text (section "What happens when a font loads"; fonts, files, performance and the guides give one sentence and a link) |
| Every warning and exception message | reference/errors (generated) |
| Every key constant | reference (Key page, generated) |
| Terms | reference/glossary |
| How JOID works: the map of JOID without code (UIs and their stacking, the node tree and the life of a node, the design canvas, signals, the backend and its bridges, one frame), first Learn page; each notion in one to three sentences and a link to its owner | use/learn/how-joid-thinks |
| Position, size, anchors, aspect ratio, node `zindex`/`zlevel`, `visible`, `enabled`, `copy` | use/learn/describing-the-ui/position-and-size |
| Node lifecycle and its callbacks (`onInit`, `onChildAppend`, `onMount`, `onUpdate`, `onRender`, `onDraw`, `onDetach`) | use/learn/describing-the-ui/node-lifecycle |
| No production font is shipped: loading your own font is required | use/learn/describing-the-ui/fonts (the Pitfall); quick-start and the other pages give one sentence |
| Text fields (`TextFieldNode`, multiline, integer, `accept`/`normalize`/`allowEmpty`, `onSubmit`, focus, clipboard, custom field hooks), the single focused field across the UIs | use/learn/interactivity/text-fields (section "Move the focus" for the focus) |
| Cursors (`Cursor`, `Node.cursor`) | use/learn/interactivity/hover-and-tooltips (section "Change the cursor over a node") |
| Shortcuts and key resolvers (`Shortcut`, `KeyResolver`) | use/learn/interactivity/keyboard (section "Bind a shortcut") |
| Giving controls a look: abstract controls, writing a skin, a UI kit (`CheckboxNode`, `ToggleNode`, `SwitchNode`, `SelectorNode`, `SliderNode`, `ScrollbarNode`, `FieldNode`; charts have their own page) | use/learn/going-further/ui-kit |
| Options of each control (switch states, selector direction, slider range/step/thumb, progress with `ProgressNode`) | use/learn/interactivity/controls (one section per control) |
| The pending value of a control (a value that the bounds refuse, kept until they allow it) | use/learn/interactivity/controls (section "Keep a value that the bounds refuse") |
| The order of the events of one change of a control | use/learn/interactivity/controls (section "Know the order of a change") |
| A value that the user changes in a node of your own (`InteractiveValue`: start, bind, change, clamp, reapply) | use/learn/going-further/custom-nodes (section "Hold a value that the user changes"); controls gives the pattern from the user side |
| What updates and what freezes in a reactive expression (native expressions rule) | use/learn/interactivity/signals (section "What JOID follows") |
| UI masks (`UI.mask`) | use/learn/style-and-motion/effects (section "Clip your own drawing") |
| `ReorderableFlexNode` | use/learn/interactivity/reorderable-lists |
| Charts (`ChartNode`, `RadarChartNode`) | use/learn/going-further/charts |
| 3D models (`ModelNode`, `ModelViewerNode`), loading a Wavefront `.obj` file with `ObjModel` | use/learn/going-further/3d-models |
| Node layers (`layer`, `NodeLayer`) and external rasters (`DrawUtils.RASTER`) | use/learn/going-further/drawing (sections "Choose where to draw" and "Show what another renderer draws") |
| Threads: JOID is not thread-safe, the render thread, `ThreadUtils.runOnRenderThread`, futures brought back to the render thread (`RenderThreadFuture`) | use/learn/running/threads (tasks and signals written from another thread: one sentence and a link to state/tasks) |
| `WindowUIBridge` settings (background, hoverInfo, hover colors) and the default bridge | use/learn/running/backends (section "Style the window UI bridge") |
| Several UI bridges, their priorities and the routing across them, from the user side (`BridgeHandler`, `StackUIBridge`, `register(bridge, priority)`, which bridge opens a UI, receives the input, clears the window and sets the cursor), with the only example and the only diagram of the bridge stack | use/learn/running/embedding (section "Add a UI bridge of your own"); build/bridges, build/bridges/ui and the `BridgeRegistry` reference give one sentence and a link |
| Where JOID stores files (`dataDir`, `cacheDir`, system properties, defaults per OS), cache areas of your own (`FileCacheArea`) | use/learn/running/files |
| Shipping your application (prod jar, fonts, files) | use/guides/ship-your-application |
| Building a UI kit end to end | use/guides/build-a-ui-kit |
| Copying a mockup pixel for pixel (no tool named) | use/guides/copy-a-mockup |
| Troubleshooting by symptom ("A node does not appear", "Text or edges look blurry"…) | use/troubleshooting |
| Best practices (DO / DON'T / PREFER / AVOID) | use/best-practices |
| Performance and GPU resource lifetime | use/in-depth/performance |
| The cause and the fix of every message | documentation/reference/errors.md, one `## <key>` entry per message, shown on reference/errors |
| Visual galleries: effects, transitions, easing curves, text effects and markup, cursors | use/catalog (one section each, generated cards) |

## Build on JOID
| Notion | Owner page |
|---|---|
| Layers (core agnostic → base modules → final backends) | build/architecture |
| Each bridge contract (render, window, audio, thread, UI, input…) | build/bridges/<bridge> |
| `BridgeHandler` registries (the table of registries, backend side; the user side is on use/learn/running/embedding) | build/bridges (overview: the table, then one sentence and a link to running/embedding for priorities) |
| Writing a backend step by step | build/writing-a-backend |
| When a shader compiles: `compileProgram` runs at the first `isActive()`, once the constructor of the subclass is done | build/writing-a-backend (section "Shaders"); the Shader reference and build/shader-translation give one sentence and a link |
| base-openal, base-opengl, base-lwjgl2, base-lwjgl3-glfw, base-lwjgl3-openal, base-lwjgl3-opengl | build/base-modules/<module> |
| Render pipeline, draw calls, render state, mipmaps (backend side) | build/pipeline |
| Shader translation (backend side): what the translator adds, never the JOID GLSL list | build/shader-translation |
| Shader checking tools (`GlslExport`) | build/shader-translation (section "Check your translation"); build/testing-a-backend gives one sentence and a link |
| Framebuffers, depth and stencil (backend side) | build/framebuffers-and-depth |
| Cross-backend tests, reference snapshots, contract suites | build/testing-a-backend |
| The demo jar, demo launchers, running every demo on a backend, demo index (Use JOID never mentions demos) | build/demos |
| Texture and resource resolvers of a backend | build/resource-resolvers |
| Each final backend (lwjgl2, lwjgl3, vulkan): what it is made of, what differs | build/backends/<backend> |
| The contract of a Vulkan host (`IVulkanHost`, frame number, `releaseHandle`) | build/backends/vulkan (section "Run inside a Vulkan host"); build/embedding-in-a-host gives one sentence and a link |
| Embedding JOID in a GL host (contexts, state journal, sharing the window) | build/embedding-in-a-host |
| Input forwarding contract (press/release, consumption, `keyReleased`) | build/input |
| Writing your own UI bridge: the contract to implement, in the tables Required, Provided and Guarantees (`StackUIBridge`, `drawBackground`, `canHandle`, the hooks) | build/bridges/ui (it links to running/embedding for priorities and routing and shows no registration example; build/bridges and build/input give one sentence and a link) |
