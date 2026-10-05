# Installation

JOID est distribué sous forme de JARs via les GitHub Releases. Ils contiennent JOID et toutes les bibliothèques avec lesquelles il décode les médias : ajoutez le JAR de votre backend et les quelques bibliothèques listées dans *Dépendances* à votre classpath, et pointez la JVM vers les bibliothèques natives de votre moteur. Pas de Maven Central, pas de gestionnaire de paquets.

## Téléchargement

Récupérez la dernière release sur [github.com/Zeldown/JOID/releases](https://github.com/Zeldown/JOID/releases).

Chaque version est publiée pour chaque backend. Choisissez l'artefact correspondant à votre moteur :

| Backend | Préfixe d'artefact | Stack |
|---|---|---|
| LWJGL 2 | `joid-lwjgl2` | LWJGL 2.9.1 — pipeline fixe OpenGL, OpenAL |
| LWJGL 3 | `joid-lwjgl3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL |
| Vulkan | `joid-vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL |

Chaque backend existe en deux variantes :

| Artefact | Contenu | Utiliser quand |
|---|---|---|
| `joid-<backend>-X.Y.Z-prod.jar` | Bibliothèque seule, `assets/demo/*` retirés | Vous shippez votre app |
| `joid-<backend>-X.Y.Z-dev.jar` | Inclut polices de démo, textures de démo, vidéos d'exemple | Apprentissage / développement |

Un JAR de backend contient le cœur, les modules JOID qu'il utilise — `joid-glfw` et `joid-openal` pour LWJGL 3 et Vulkan — et les bibliothèques média :

- **FFmpeg** via JavaCV et JavaCPP, avec ses natives pour Windows x64, Linux x64, macOS Intel et macOS ARM, extraites à l'exécution — environ 95 Mo du JAR. Il garde ses packages `org.bytedeco`, dont JavaCPP a besoin pour trouver ses natives.
- **JSVG** (SVG) et **TwelveMonkeys ImageIO** (WebP), relocalisés sous `dev.joid.shaded`, pour ne jamais entrer en conflit avec les copies que votre application ou son hôte embarque. JOID instancie lui-même son lecteur WebP et n'enregistre rien dans ImageIO.

Un build depuis les sources avec `./gradlew build` (`-Pdev` pour la variante dev) copie chaque artefact de release — jars des backends, du cœur, du testkit, de glfw et d'openal et gabarit de backend — dans `build/libs`.

## Gradle

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')
}
```

Gradle legacy utilise `compile` ; Gradle moderne utilise `implementation`. Les deux fonctionnent. Ajoutez aussi les bibliothèques listées dans *Dépendances*.

## Maven

```xml
<dependency>
    <groupId>dev.joid</groupId>
    <artifactId>joid</artifactId>
    <version>6.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-lwjgl2-6.0.0-prod.jar</systemPath>
</dependency>
```

## Bibliothèques natives

Les natives dépendent du backend :

- **LWJGL 2** — le dossier `impl/lwjgl2/native/` du dépôt contient les natives OpenGL et OpenAL (`lwjgl64.dll`, `OpenAL64.dll`, et leurs variantes de plateformes). Copiez-le à côté de votre projet et lancez avec `-Djava.library.path=./native`.
- **LWJGL 3 et Vulkan** — les natives sont livrées sous forme de JARs classifiers Maven (`natives-windows`, `natives-linux`, `natives-macos`, `natives-macos-arm64`) que LWJGL extrait à l'exécution. Vulkan passe par le loader installé avec le driver GPU ; macOS nécessite en plus les natives `lwjgl-vulkan` (MoltenVK).

> TIP: OpenAL est nécessaire pour l'audio du `VideoPlayerNode` sur tous les backends.

## Dépendances

**Votre projet déclare les bibliothèques que JOID partage avec son hôte**, dans les versions avec lesquelles JOID est compilé et testé : celles que Minecraft 1.7.10 fournit déjà, dont certaines apparaissent dans l'API de JOID (les caches de `ResourceBuilder` sont des caches Guava, les stores lisent des objets Gson), et le LWJGL de votre backend. Tout le reste est dans le JAR.

Ajoutez ceci à votre `build.gradle` à côté de JOID :

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')

    compile 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'

    compile 'com.google.guava:guava:15.0'
    compile 'com.google.code.gson:gson:2.2.4'
    compile 'org.apache.commons:commons-lang3:3.1'
    compile 'org.apache.commons:commons-compress:1.8.1'
    compile 'commons-io:commons-io:2.4'
    compile 'java3d:vecmath:1.3.1'

    compile 'org.lwjgl.lwjgl:lwjgl:2.9.1'

}
```

Le bloc Maven équivalent :

```xml
<dependencies>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>1.18.34</version><scope>provided</scope></dependency>
    <dependency><groupId>com.google.guava</groupId><artifactId>guava</artifactId><version>15.0</version></dependency>
    <dependency><groupId>com.google.code.gson</groupId><artifactId>gson</artifactId><version>2.2.4</version></dependency>
    <dependency><groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId><version>3.1</version></dependency>
    <dependency><groupId>org.apache.commons</groupId><artifactId>commons-compress</artifactId><version>1.8.1</version></dependency>
    <dependency><groupId>commons-io</groupId><artifactId>commons-io</artifactId><version>2.4</version></dependency>
    <dependency><groupId>java3d</groupId><artifactId>vecmath</artifactId><version>1.3.1</version></dependency>
    <dependency><groupId>org.lwjgl.lwjgl</groupId><artifactId>lwjgl</artifactId><version>2.9.1</version></dependency>
</dependencies>
```

### Backends LWJGL 3 et Vulkan

Remplacez la ligne `lwjgl 2.9.1` par les modules LWJGL 3 de votre backend et leurs natives :

```groovy
dependencies {
    compile 'org.lwjgl:lwjgl:3.3.4'
    compile 'org.lwjgl:lwjgl-glfw:3.3.4'
    compile 'org.lwjgl:lwjgl-openal:3.3.4'
    compile 'org.lwjgl:lwjgl-opengl:3.3.4'
    compile 'org.lwjgl:lwjgl-vulkan:3.3.4'
    compile 'org.lwjgl:lwjgl-shaderc:3.3.4'

    runtime 'org.lwjgl:lwjgl:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-glfw:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-openal:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-opengl:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-shaderc:3.3.4:natives-windows'
}
```

`lwjgl-opengl` n'est utile qu'au backend LWJGL 3, `lwjgl-vulkan` et `lwjgl-shaderc` qu'au backend Vulkan. Utilisez le classifier de natives de chaque plateforme que vous shippez.

### Rôle de chaque dépendance

| Dépendance | Utilité | Requis |
|---|---|---|
| `lombok` | Génération de code (`@Getter`, `@Setter`, `@NonNull`) | Compile seulement — non shippée |
| `guava`, `gson`, `commons-lang3`, `commons-compress`, `commons-io`, `vecmath` | Collections et caches, JSON, texte, archives, surveillance de fichiers et calcul vectoriel utilisés par le cœur | Toujours — un hôte qui les fournit déjà les apporte |
| `lwjgl` | Bindings du moteur du backend choisi | Toujours |

### Shipping en production

**Ne fat-JAR pas votre app.** La disposition recommandée est un JAR d'application mince qui référence chaque JAR de dépendance depuis un dossier voisin `libraries/`, chargé via le classpath. Ça garde l'artefact d'application petit, vous permet de remplacer ou patcher une dépendance sans rebuild, et ne shade jamais le JAR JOID une seconde fois : JavaCPP trouve les natives FFmpeg via leurs packages `org.bytedeco` d'origine.

Exemple de disposition pour un zip distributable :

```
my-app/
├── my-app.jar                           # votre code seul — pas de shading
├── libraries/                           # toutes les dépendances compile, un JAR par artefact
│   ├── joid-lwjgl2-6.0.0-prod.jar
│   ├── guava-15.0.jar
│   ├── gson-2.2.4.jar
│   ├── commons-lang3-3.1.jar
│   ├── commons-compress-1.8.1.jar
│   ├── commons-io-2.4.jar
│   ├── vecmath-1.3.1.jar
│   └── lwjgl-2.9.1.jar
├── native/                              # natives du backend LWJGL 2 (OpenGL + OpenAL)
│   ├── lwjgl64.dll
│   ├── OpenAL64.dll
│   └── …
└── run.bat                              # launcher
```

Un launcher minimal (`run.bat` sous Windows, `run.sh` sur *nix) :

```bat
java -Djava.library.path=./native -cp "my-app.jar;libraries/*" com.myapp.Main
```

```bash
#!/bin/sh
java -Djava.library.path=./native -cp "my-app.jar:libraries/*" com.myapp.Main
```

Le wildcard `libraries/*` étend tous les JARs du dossier — la JVM les récupère sans qu'il soit nécessaire de les lister un par un. Les JARs classifiers de FFmpeg contiennent les binaires natifs `.dll` / `.so` / `.dylib` ; JavaCPP les extrait depuis le classpath à l'exécution, il suffit donc qu'ils soient présents dans `libraries/`.

Pour produire cette disposition avec Gradle :

```groovy
task dist(type: Copy) {
    into 'build/dist/libraries'
    from configurations.runtimeClasspath    // toutes les deps résolues
}

task distApp(type: Jar) {
    archiveBaseName = 'my-app'
    destinationDirectory = file('build/dist')
    from sourceSets.main.output
    manifest { attributes 'Main-Class': 'com.myapp.Main' }
}

dist.dependsOn distApp
```

Après `gradle dist`, zippez `build/dist/` avec votre dossier `native/` et le launcher.

## Vérification

Glissez ceci dans un `main` :

```java
public static void main(String[] args) {
    System.out.println("JOID version: " + JOID.VERSION);
}
```

Si la version installée s'affiche, vous êtes prêt. Continuez avec [Quick Start](quick-start.md).