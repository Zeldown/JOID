# Backends

Le cœur de JOID est agnostique au moteur. Les nœuds, les effets, le pipeline de shaders, les polices, les ressources et la vidéo n'appellent jamais directement OpenGL, Vulkan, GLFW ou OpenAL — ils passent par quatre bridges enregistrés dans `BridgeHandler`. Un backend est l'ensemble des classes qui implémentent ces bridges pour un moteur donné.

| Bridge | Responsabilité |
|---|---|
| `IUIBridge` | Héberge les UIs : ouverture / fermeture, rendu du hover, dispatch des entrées. Voir [Bridge](bridge.md). |
| `IWindowBridge` | Taille de la fenêtre, position de la souris, capture de la souris, état du clavier, presse-papier. |
| `IRenderBridge` | Piles de matrices, état de rendu, textures, framebuffers, shaders, appels de dessin. |
| `IAudioBridge` | Sources audio en streaming utilisées par le lecteur vidéo. |

Tous les bridges implémentent `IBridge`, et `BridgeHandler` expose un `BridgeRegistry` par type de bridge : `UI`, `WINDOW`, `RENDER` et `AUDIO`. Un registre garde tous les bridges enregistrés triés par `getIndex()` — `0` par défaut, le dernier enregistré l'emporte à égalité.

| Méthode | Résultat |
|---|---|
| `register(bridge)` | Ajoute le bridge au registre. |
| `get()` | Le bridge le plus prioritaire. Lève une `IllegalStateException` avec un message explicite si aucun n'a été enregistré. |
| `find(filter)` | Le bridge le plus prioritaire qui correspond au prédicat, ou `null`. |
| `getBridge(MyBridge.class)` | Le bridge le plus prioritaire de cette classe, ou `null`. |

`BridgeHandler.UI` est un `UIBridgeRegistry` : il ajoute `get(ui)` et `get(MyUI.class)`, qui retournent le bridge capable de gérer l'`UI`.

## Backends disponibles

La branche `main` contient la bibliothèque neutre. Chaque backend vit dans sa propre branche, avec son `build.gradle`, ses shaders et une `DemoWindow` prête à lancer (`./gradlew runDemo`).

| Branche | Stack | Enregistrement | Shaders |
|---|---|---|---|
| `impl/lwjgl-2` | LWJGL 2.9.1 — pipeline fixe OpenGL, OpenAL | `LWJGL2Backend.register()` | GLSL 120 |
| `impl/lwjgl-3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL | `LWJGL3Backend.register(window)` | GLSL 330 |
| `impl/vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL | `VulkanBackend.register(window)` | GLSL 450 Vulkan |

### LWJGL 2

Enregistrez le backend une fois le `Display` créé, puis enregistrez votre `IUIBridge` :

```java
Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
LWJGL2Backend.register();
BridgeHandler.UI.register(myBridge);
JOID.inst().load();
```

Le backend LWJGL 2 traduit chaque appel nativement vers le pipeline fixe et restaure l'état de l'hôte qu'il modifie, ce qui le rend sûr à l'intérieur d'un hôte LWJGL 2 existant. Il nécessite le dossier `native/` dans le `java.library.path`.

### LWJGL 3

Créez une fenêtre GLFW avec un contexte OpenGL 3.3 core et un stencil buffer, rendez-la courante, puis enregistrez :

```java
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
GLFW.glfwMakeContextCurrent(window);
GL.createCapabilities();
LWJGL3Backend.register(window);
```

Les natives sont résolues depuis Maven pour l'OS courant par le `build.gradle` de la branche.

### Vulkan

Vulkan possède la swapchain, l'hôte pilote donc la frame explicitement :

```java
Configuration.STACK_SIZE.set(1024);
GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
VulkanBackend.register(window);

final VulkanRenderBridge render = (VulkanRenderBridge) BridgeHandler.RENDER.get();
while (!GLFW.glfwWindowShouldClose(window)) {
    GLFW.glfwPollEvents();
    bridge.update();
    render.beginFrame();
    bridge.draw();
    render.endFrame();
    render.present();
}
```

`Configuration.STACK_SIZE` doit être augmentée avant le premier appel LWJGL : la création de l'instance Vulkan énumère toutes les couches et extensions sur la pile thread-locale de LWJGL. Un device Vulkan 1.3 est requis ; les lignes lissées et épaisses sont activées quand le driver les supporte.

## Conventions d'entrée

- Les coordonnées de la souris sont en pixels, origine au coin **supérieur gauche** de la fenêtre.
- Les touches utilisent l'enum neutre `Key`. `Key.LEFT_CONTROL.isDown()` interroge le bridge de fenêtre ; `keyPressed(char, Key, InternalContext)` reçoit la touche à l'origine de l'événement.
- Le caractère et la touche sont livrés ensemble. Les backends GLFW fusionnent les callbacks de touche et de caractère avant le dispatch.

## Écrire un backend

Un backend implémente `IWindowBridge`, `IAudioBridge` et `IRenderBridge`, et ne modifie jamais la branche `main`.

### Bridge de rendu

Deux approches sont supportées :

- **Natif** — implémentez directement `IRenderBridge` et transmettez chaque appel à une API à état (le backend LWJGL 2).
- **Émulé** — étendez `RenderBridge`. Les matrices et l'état sont suivis en Java (`getModelView()`, `getProjection()`, `getState()`), et votre implémentation fournit seulement `clear`, `clearStencil`, `draw`, `createTexture`, `createFrameBuffer` et `createShader`, en appliquant l'état courant à leur exécution (les backends LWJGL 3 et Vulkan).

Le contrat que respecte chaque backend :

- `draw(DrawMode, VertexBuffer)` reçoit des `TRIANGLES` ou des `LINES` depuis le `Tessellator`. Chaque sommet fait 32 octets en ordre natif : position `3×float` à l'offset 0, coordonnées de texture `2×float` à 12, couleur `RGBA8` à 20, normale `3×int8` à 24. `isTexture()`, `isColor()` et `isNormal()` indiquent les attributs présents — utilisez la couleur courante quand les couleurs sont absentes.
- Les matrices de projection suivent les conventions OpenGL (profondeur clip-space dans `[-1, 1]`, Y vers le haut, origine du viewport en bas à gauche). Les backends aux conventions différentes les convertissent.
- `resetTexture()` bind une texture blanche opaque, pour que les shaders puissent toujours échantillonner.
- `ITexture.upload` reçoit des entiers `ARGB`. `ITexture.delete()` peut être appelée plusieurs fois.
- Les framebuffers n'ont qu'un attachement couleur — ni profondeur ni stencil.
- `pushState()` / `popState()` restaurent tout ce qui a été réglé via le bridge, y compris le framebuffer lié, le viewport et le shader courant.
- `lighting(true)` signifie un terme ambiant de `0.6` plus une lumière directionnelle selon l'axe de vue, appliquée par sommet en flat shading.

### Shaders

Les shaders sont des assets du backend chargés depuis `/assets/shaders/<name>/<name>.vsh` et `.fsh`, chaque branche fournit donc le langage de son moteur :

| Backend | Langage | Conventions |
|---|---|---|
| LWJGL 2 | GLSL 120 | Attributs fixes (`gl_Vertex`, `gl_Color`, `gl_MultiTexCoord0`). |
| LWJGL 3 | GLSL 330 | Attributs aux locations `0` position, `1` uv, `2` couleur, `3` normale. Uniforms `uProjectionMatrix` et `uModelViewMatrix`. Sortie `out vec4 fragColor`. |
| Vulkan | GLSL 450 | Mêmes attributs. Uniforms du vertex dans un bloc `std140` à `binding = 0`, uniforms du fragment à `binding = 1`, samplers à partir de `binding = 2`. Varyings `vPosition`, `vTexCoord`, `vColor` aux locations `0`, `1`, `2`. Sortie `layout(location = 0) out vec4 fragColor`. |

Les samplers qui ne sont pas réglés via un `SamplerUniform` reçoivent la texture actuellement liée. Les backends LWJGL 3 et Vulkan enveloppent le `main` du fragment pour appliquer l'alpha test de l'état de rendu, c'est pourquoi la sortie doit s'appeler `fragColor`.

## Voir aussi

- [Bridge](bridge.md) — héberger les UIs et transmettre les entrées.
- [Custom Shaders](../shaders/custom.md) — écrire des shaders pour chaque backend.
