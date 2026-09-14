# Backends

Le cœur de JOID est agnostique au moteur. Les nœuds, les effets, le pipeline de shaders, les polices, les ressources et la vidéo n'appellent jamais directement OpenGL, Vulkan, GLFW ou OpenAL — ils passent par les bridges enregistrés dans `BridgeHandler`. Un backend est l'ensemble des classes qui implémentent ces bridges pour un moteur donné.

| Bridge | Responsabilité |
|---|---|
| `IUIBridge` | Héberge les UIs : ouverture / fermeture, rendu du hover, dispatch des entrées. Voir [Bridge](bridge.md). |
| `IWindowBridge` | Taille de la fenêtre, position de la souris, capture de la souris, état du clavier, presse-papier. |
| `IRenderBridge` | Piles de matrices, état de rendu, textures, framebuffers, shaders, appels de dessin. |
| `IAudioBridge` | Sources audio en streaming utilisées par le lecteur vidéo. |
| `IClockBridge` | Temps des animations, des tâches planifiées, des curseurs de texte, des doubles clics et de `Node.wait`. `SystemClockBridge` est enregistré par défaut, `ManualClockBridge` contrôle le temps à la main. |

Tous les bridges implémentent `IBridge`, et `BridgeHandler` expose un `BridgeRegistry` par type de bridge : `UI`, `WINDOW`, `RENDER`, `AUDIO` et `CLOCK`. Un registre garde tous les bridges enregistrés triés par `getIndex()` — `0` par défaut, le dernier enregistré l'emporte à égalité.

| Méthode | Résultat |
|---|---|
| `register(bridge)` | Ajoute le bridge au registre. |
| `get()` | Le bridge le plus prioritaire. Lève une `IllegalStateException` avec un message explicite si aucun n'a été enregistré. |
| `find(filter)` | Le bridge le plus prioritaire qui correspond au prédicat, ou `null`. |
| `getBridge(MyBridge.class)` | Le bridge le plus prioritaire de cette classe, ou `null`. |

`BridgeHandler.UI` est un `UIBridgeRegistry` : il ajoute `get(ui)` et `get(MyUI.class)`, qui retournent le bridge capable de gérer l'`UI`.

## Backends disponibles

Le dépôt est un build Gradle multi-modules. `core` contient la bibliothèque neutre, et chaque backend est un module sous `impl/` avec son `build.gradle`, ses shaders et une `DemoWindow` prête à lancer (`./gradlew :vulkan:runDemo`). LWJGL 3 et Vulkan partagent le module de fenêtre `glfw` et le module audio `openal`. Le module `testkit` contient le framework de tests de snapshot partagé par les backends.

| Module | Stack | Enregistrement | Shaders générés |
|---|---|---|---|
| `lwjgl2` | LWJGL 2.9.1 — pipeline fixe OpenGL, OpenAL | `Backend.register()` | GLSL 120 |
| `lwjgl3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL | `Backend.register(window)` | GLSL 330 |
| `vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL | `Backend.register(window)` | GLSL 450 Vulkan |

Les classes d'implémentation sont nommées par rôle — `Backend`, `RenderBridge`, `Shader`, `Texture`… — et leur package, `be.zeldown.joid.impl.<module>`, indique le moteur auquel elles appartiennent.

### LWJGL 2

Enregistrez le backend une fois le `Display` créé, puis enregistrez votre `IUIBridge` :

```java
import be.zeldown.joid.impl.lwjgl2.Backend;

Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
Backend.register();
BridgeHandler.UI.register(myBridge);
JOID.inst().load();
```

Le backend LWJGL 2 traduit chaque appel nativement vers le pipeline fixe et restaure l'état de l'hôte qu'il modifie, ce qui le rend sûr à l'intérieur d'un hôte LWJGL 2 existant. Il nécessite le dossier `native/` du module `lwjgl2` dans le `java.library.path`.

### LWJGL 3

Créez une fenêtre GLFW avec un contexte OpenGL 3.3 core et un stencil buffer, rendez-la courante, puis enregistrez :

```java
import be.zeldown.joid.impl.lwjgl3.Backend;

GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
GLFW.glfwMakeContextCurrent(window);
GL.createCapabilities();
Backend.register(window);
```

Les natives sont résolues depuis Maven pour l'OS courant par le `build.gradle` du module.

### Vulkan

Vulkan possède la swapchain, l'hôte pilote donc la frame explicitement :

```java
import be.zeldown.joid.impl.vulkan.Backend;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;

Configuration.STACK_SIZE.set(1024);
GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
final long window = GLFW.glfwCreateWindow(1920, 1080, "My app", 0L, 0L);
Backend.register(window);

final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
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

Un backend implémente `IWindowBridge`, `IAudioBridge` et `IRenderBridge`, et ne modifie jamais le module `core`. Ajoutez-le comme module sous `impl/`, incluez-le dans `settings.gradle` et appliquez `gradle/backend.gradle` pour le packager avec le cœur. Il peut aussi vivre dans son propre dépôt et ne dépendre que des jars de JOID, comme le décrit la section *Backend dans son propre dépôt* plus bas.

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
- Les lignes lissées sont dessinées par le cœur : le `Tessellator` étend chaque segment d'une ligne dessinée avec `lineSmooth(true)` en quad dans l'espace écran, et le shader `line` calcule la couverture antialiasée d'OpenGL, les backends ne dessinent donc que des triangles. `isLineSmooth()`, `getLineWidth()`, `getViewportWidth()` et `getViewportHeight()` exposent l'état dont il a besoin.

### Shaders

Les shaders vivent dans le module `core` et s'écrivent une seule fois en GLSL JOID (voir [Custom Shaders](../shaders/custom.md)). Le cœur analyse chaque étape en `ShaderSource` — varyings, uniforms, samplers, intégrés utilisés et corps — et passe les deux étapes à `createShader(ShaderSource, ShaderSource, BlendState)`. Le backend génère seulement les déclarations de son langage devant le corps :

| Backend | Langage | Déclarations générées |
|---|---|---|
| LWJGL 2 | GLSL 120 | `#define` des intégrés vers `gl_Vertex`, `gl_MultiTexCoord0`, `gl_Color`, `gl_ProjectionMatrix`, `gl_ModelViewMatrix`, `gl_FragColor`… et de `texture` vers `texture2D`. Les varyings deviennent des `varying`. |
| LWJGL 3 | GLSL 330 | Attributs aux locations `0` position, `1` uv, `2` couleur, `3` normale, uniforms intégrés, varyings `in` / `out`, `out vec4 fragColor`. |
| Vulkan | GLSL 450 | Mêmes attributs, les uniforms des deux étapes dans un seul bloc `std140` à `binding = 0`, samplers à partir de `binding = 1`, locations des varyings partagées par les deux étapes, `layout(location = 0) out vec4 fragColor`. |

Chaque en-tête généré se termine par une directive `#line`, pour que les erreurs de compilation pointent vers le fichier d'origine. Les samplers qui ne sont pas réglés via un `SamplerUniform` reçoivent la texture actuellement liée. Les backends LWJGL 3 et Vulkan dessinent sans shader lié via `/assets/shaders/fixed`, et enveloppent le `main` du fragment pour appliquer l'alpha test de l'état de rendu.

### Tests

Les tests de snapshot vivent dans le module `testkit`. Un backend implémente `ISnapshotBackend` — créer une surface hors écran de la taille demandée, exécuter une frame, capturer une zone de ses pixels, la libérer et nommer le renderer — et étend `SnapshotSuite` et `RenderBridgeContractSuite` dans ses tests :

```java
public class SnapshotTest extends SnapshotSuite {

    @Override
    protected ISnapshotBackend createBackend() {
        return new SnapshotBackend();
    }

}
```

`RenderBridgeContractTest` étend `RenderBridgeContractSuite` de la même façon. La suite de contrat vérifie le bridge de rendu sans images de référence, en quelques secondes : chaque shader du cœur compile, la couleur courante et les couleurs de sommets sont dessinées, les textures `ARGB` affichent leur premier texel en haut à gauche, `resetTexture()` lie une texture blanche opaque, les framebuffers gardent ce qui y est dessiné, `popState()` restaure le framebuffer, le shader, le viewport et l'état des lignes, une texture peut être supprimée deux fois et la projection OpenGL place l'origine en haut à gauche de la capture.

La suite de snapshot enregistre un `ManualClockBridge`, puis joue chaque scénario de `testkit/src/main/resources/snapshot` — `static`, `interaction`, `transition`, `popup`, `window`, `dev` et `video`. Chaque scénario part du même état : aucune UI, l'horloge au même instant, une fenêtre 1920×1080, le mode dev désactivé, aucune touche enfoncée et aucun masque. Le temps n'avance qu'avec `wait` et `moveto`, par frames de 16 ms, et les lerps et le compteur de fps suivent le temps de frame mesuré sur l'horloge, chaque exécution rend donc les mêmes pixels. Chaque capture attend la fin du chargement des ressources, que chaque vidéo affiche l'image correspondant à l'horloge, puis deux frames consécutives identiques. Les vidéos et les GIF suivent l'horloge, une horloge en pause les fige donc, l'audio est coupé, et les ressources d'URL sont téléchargées une fois dans `.snapshots/cache` pour que les exécutions suivantes fonctionnent hors ligne.

Les références sont propres à chaque machine et carte graphique : elles sont stockées dans `.snapshots/<module>/<renderer>/`, ignoré par git. Une capture sans référence est enregistrée à la première exécution ; ensuite, chaque capture doit correspondre à sa référence au pixel près. Les références des captures retirées des scénarios sont supprimées après l'exécution.

Les suites lisent leurs dossiers dans des propriétés système, avec des valeurs par défaut qui fonctionnent depuis n'importe quel IDE ou outil de build : `joid.snapshot.references` (`.snapshots/references`), `joid.snapshot.output` (`build/snapshots/renders`), `joid.snapshot.cache` (`.snapshots/cache`) et `joid.snapshot.update`. Le build JOID les règle pour chaque module. Dans les backends JOID, `SnapshotBackend` vit dans le package `snapshot` des sources principales, pour que leur jar dev puisse rendre une référence, et leur jar prod l'exclut.

| Commande | Résultat |
|---|---|
| `./gradlew test` | Tests unitaires des shaders et tests de snapshot de chaque module. Les rendus et un `report.html` interactif sont écrits dans `build/snapshots/<module>`. |
| `./gradlew updateSnapshots` | Remplace les références après un changement visuel voulu. `./gradlew :vulkan:updateSnapshots` ne met à jour qu'un module. |
| `./gradlew crossBackendTest` | Lance les tests, puis compare les captures `lwjgl3` et `vulkan` à `lwjgl2` avec une tolérance d'un niveau par canal, qui absorbe les arrondis d'antialiasing de chaque driver. La comparaison est affichée dans `build/snapshots/cross/report.html`. |

Les tests de snapshot nécessitent un GPU. Les hooks installés par `./gradlew installLocalGitHook` lancent `scripts/run-tests` : le hook pre-commit teste les changements indexés et le hook pre-push les commits poussés, après avoir mis de côté tout le reste. Seuls les modules touchés par les changements sont testés — `core`, `testkit` et le build testent tous les backends, `glfw` et `openal` testent LWJGL 3 et Vulkan — et la comparaison entre backends réutilise les derniers rendus des autres backends.

#### Rapport

Chaque `report.html` liste les captures avec leur statut et leur nombre de pixels différents, et affiche la référence et le rendu de la capture sélectionnée selon huit modes : côte à côte, balayage, pelure d'oignon, clignotement, différence amplifiée, pixels surlignés, référence et rendu. La molette zoome autour du curseur jusqu'au pixel avec une grille, le glisser déplace la vue, et le survol d'un pixel affiche ses coordonnées, les deux couleurs et l'écart de chaque canal. `Next difference` regroupe les pixels différents en zones et zoome sur chacune, un pixel isolé est donc toujours retrouvé. Le rapport s'ouvre directement depuis le disque, sans serveur. Quand un test échoue, Gradle affiche le lien du rapport à la fin du build.

#### Scénarios

Un scénario est un fichier texte avec une commande par ligne ; `#` commence un commentaire.

| Commande | Effet |
|---|---|
| `ui <class>` | Ferme toutes les UIs, ouvre l'UI et sort la souris de la fenêtre. |
| `open <class>` | Ouvre l'UI via `JOID.open`, avec ses transitions et ses popups. |
| `wait <ms>` | Avance l'horloge frame par frame. |
| `move <x> <y>` | Déplace la souris. |
| `moveto <x> <y> <ms>` | Déplace la souris progressivement, en glissant tant qu'un bouton est enfoncé. |
| `press <button>` / `release` | Enfonce ou relâche un `ClickType`. |
| `scroll <value>` | Scrolle, `120` par cran. |
| `type <text>` | Tape le texte, en maintenant `LEFT_SHIFT` pour les majuscules et les symboles décalés. |
| `key <KEY>[+<KEY>...]` | Maintient chaque touche de la combinaison et envoie la dernière, par exemple `key LEFT_CONTROL+K`. |
| `down <KEY>` / `up <KEY>` | Maintient ou relâche une touche pour les commandes suivantes. |
| `resize <width> <height>` | Redimensionne la fenêtre, jusqu'à 1920×1080. |
| `zoom <level>` | Règle le zoom des UIs ouvertes. |
| `dev <true\|false>` | Active ou désactive le mode dev pour les UIs ouvertes ensuite. |
| `mask <x> <y> <width> <height>` | Remplit le rectangle dans les captures suivantes, pour exclure un contenu qui ne peut pas être déterministe comme l'usage mémoire. |
| `unmask` | Retire les masques. |
| `shot <name>` | Capture la fenêtre sous `<name>.png`. |

Pour ajouter une capture, ajoutez ses commandes à un scénario — ou ajoutez un fichier de scénario et son test `matches…Snapshots` à `SnapshotSuite` — puis lancez `./gradlew test` : les nouvelles captures sont enregistrées comme références.

`UIDemoVideo` masque son overlay de statistiques, qui affiche l'usage mémoire et les files du décodeur.

## Backend dans son propre dépôt

Un backend n'a pas besoin de vivre dans le dépôt JOID : chaque release publie les jars nécessaires pour en développer, tester et packager un ailleurs.

| Artefact | Contenu |
|---|---|
| `joid-core-X.Y.Z-dev.jar` | Le cœur avec ses dépendances embarquées et les assets de démo, pour compiler, tester et lancer la démo. |
| `joid-core-X.Y.Z-prod.jar` | Le même cœur sans `assets/demo`, embarqué dans le jar prod du backend. |
| `joid-testkit-X.Y.Z.jar` | `SnapshotSuite`, `RenderBridgeContractSuite`, les scénarios, le rapport, `SnapshotBaseline` et `SnapshotComparison`. Il nécessite JUnit 4. |
| `joid-glfw-X.Y.Z.jar`, `joid-openal-X.Y.Z.jar` | Les bridges de fenêtre GLFW et d'audio OpenAL, pour les moteurs qui les utilisent. |
| `joid-<backend>-X.Y.Z-dev.jar` | Les backends officiels, avec leur `SnapshotBackend` pour rendre une référence. |
| `joid-backend-template-X.Y.Z.zip` | Un projet Gradle de départ. |

Le gabarit compile avec les jars de son dossier `libs/` et déclare JavaCV et FFmpeg comme le cœur — la lecture vidéo en a besoin à l'exécution et aucun jar ne les embarque. Il compile tel quel, avec des bridges qui lèvent `UnsupportedOperationException` tant qu'ils ne sont pas implémentés :

| Commande | Résultat |
|---|---|
| `./gradlew test` | Les suites de contrat et de snapshot, avec les références dans `.snapshots/references`. |
| `./gradlew renderBaseline` | Rend les scénarios avec le `SnapshotBackend` de `joid-lwjgl3-X.Y.Z-dev.jar`, dans sa propre JVM. |
| `./gradlew crossBackendTest` | Compare les captures du backend à cette référence avec une tolérance d'un niveau par canal. |
| `./gradlew build` | Un jar dev et un jar prod qui embarquent le cœur correspondant. |
| `./gradlew testDevJar testProdJar` | Lance les tests sur les jars packagés. |
| `./gradlew runDemo` | Lance la fenêtre de démo. |

`SnapshotBaseline <classe du backend> <dossier de sortie>` rend chaque scénario avec un `ISnapshotBackend`, et `SnapshotComparison <dossier du rapport> <dossier de référence> <dossier comparé>...` compare des dossiers de captures, n'importe quel outil de build peut donc les lancer.

La classe `Backend` du gabarit appelle `JOID.checkVersion(version)` avant d'enregistrer les bridges : elle affiche un avertissement et renvoie `false` quand le JOID chargé a une autre version majeure que celle ciblée par le backend. Un backend s'appuie sur `be.zeldown.joid.lib.bridge` et ses sous-packages — bridges, état de rendu, sources et uniforms de shaders, textures, framebuffers et sommets —, sur `be.zeldown.joid.internal.JOID` pour charger JOID et vérifier sa version, et sur `be.zeldown.joid.demo` pour sa fenêtre de démo.

## Voir aussi

- [Bridge](bridge.md) — héberger les UIs et transmettre les entrées.
- [Custom Shaders](../shaders/custom.md) — écrire des shaders pour chaque backend.
