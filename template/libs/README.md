# libs

Download these files from the [JOID @JOID_VERSION@ release](https://github.com/Zeldown/JOID/releases) and put them in this folder:

| File | Used by |
|---|---|
| `joid-core-@JOID_VERSION@-demo.jar` | Compilation, tests, demo and the demo jar |
| `joid-core-@JOID_VERSION@-dev.jar` | The dev jar |
| `joid-core-@JOID_VERSION@-prod.jar` | The prod jar |
| `joid-tool-testkit-@JOID_VERSION@.jar` | Snapshot tests, contract tests, baseline and cross comparison |
| `joid-backend-lwjgl3-@JOID_VERSION@-demo.jar` | `renderBaseline`, the official rendering your backend is compared to |
| `joid-base-lwjgl3-glfw-@JOID_VERSION@.jar` | Optional — the GLFW window bridge, input forwarder and key table, when your engine opens its window with GLFW through LWJGL 3 |
| `joid-base-openal-@JOID_VERSION@.jar`, `joid-base-lwjgl3-openal-@JOID_VERSION@.jar` | Optional — the OpenAL audio bridge, and its binding `Lwjgl3AlBinding` on the OpenAL of LWJGL 3, when your engine plays its sound with OpenAL (`joid-base-lwjgl3-openal` needs `joid-base-openal`) |
| `joid-base-opengl-@JOID_VERSION@.jar` | Optional — the OpenGL renderer, when your engine gives an OpenGL 2.1 to 4.6 context: implement its `IGlBinding` and `IGlFrameBufferBinding` interfaces and register `new GlRenderBridge(binding)` |
