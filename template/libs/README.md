# libs

Download these files from the [JOID @JOID_VERSION@ release](https://github.com/Zeldown/JOID/releases) and put them in this folder:

| File | Used by |
|---|---|
| `joid-core-@JOID_VERSION@-demo.jar` | Compilation, tests, demo and the demo jar |
| `joid-core-@JOID_VERSION@-dev.jar` | The dev jar |
| `joid-core-@JOID_VERSION@-prod.jar` | The prod jar |
| `joid-tool-testkit-@JOID_VERSION@.jar` | Snapshot tests, contract tests, baseline and cross comparison |
| `joid-backend-lwjgl3-@JOID_VERSION@-demo.jar` | `renderBaseline`, the official rendering your backend is compared to |
| `joid-base-glfw-@JOID_VERSION@.jar`, `joid-base-openal-@JOID_VERSION@.jar` | Optional — the GLFW window bridge with its LWJGL 3 OpenAL binding `Lwjgl3AlBinding`, and the OpenAL audio bridge, when your engine uses them (`joid-base-glfw` needs `joid-base-openal`) |
| `joid-base-opengl-@JOID_VERSION@.jar` | Optional — the OpenGL renderer, when your engine gives an OpenGL 2.1 to 4.6 context: implement its `IGlBinding` and `IGlFrameBufferBinding` interfaces and register `new GlRenderBridge(binding)` |
