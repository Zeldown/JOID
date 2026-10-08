# libs

Download these files from the [JOID @JOID_VERSION@ release](https://github.com/Zeldown/JOID/releases) and put them in this folder:

| File | Used by |
|---|---|
| `joid-core-@JOID_VERSION@-dev.jar` | Compilation, tests, demo and the dev jar |
| `joid-core-@JOID_VERSION@-prod.jar` | The prod jar |
| `joid-tool-testkit-@JOID_VERSION@.jar` | Snapshot tests, contract tests, baseline and cross comparison |
| `joid-backend-lwjgl3-@JOID_VERSION@-dev.jar` | `renderBaseline`, the official rendering your backend is compared to |
| `joid-base-glfw-@JOID_VERSION@.jar`, `joid-base-openal-@JOID_VERSION@.jar` | Optional — the GLFW window and OpenAL audio bridges, when your engine uses them |
| `joid-base-opengl-@JOID_VERSION@.jar` | Optional — the OpenGL renderer, when your engine gives an OpenGL 2.0 to 4.6 context: implement its `IGl*Binding` interfaces and register `GlRenderBridge.create(binding)` |