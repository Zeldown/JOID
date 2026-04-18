# Installation

JOID est distribué sous forme de fat JAR via les GitHub Releases. Pas de Maven Central, pas de gestionnaire de paquets — il suffit de déposer le JAR dans votre classpath et de pointer la JVM vers les bibliothèques natives.

## Téléchargement

Récupérez la dernière release sur [github.com/Zeldown/JOID/releases](https://github.com/Zeldown/JOID/releases).

Deux artefacts sont publiés par version :

| Artefact | Contenu | Utiliser quand |
|---|---|---|
| `joid-X.Y.Z-prod.jar` | Bibliothèque seule, `assets/dev/*` et `assets/test/*` retirés | Vous shippez votre app |
| `joid-X.Y.Z-dev.jar` | Inclut polices de démo, textures de test, vidéos d'exemple | Apprentissage / développement |

## Gradle

```groovy
dependencies {
    compile files('libs/joid-6.0.0-prod.jar')
}
```

Gradle legacy utilise `compile` ; Gradle moderne utilise `implementation`. Les deux fonctionnent.

## Maven

```xml
<dependency>
    <groupId>be.zeldown.joid</groupId>
    <artifactId>joid</artifactId>
    <version>6.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-6.0.0-prod.jar</systemPath>
</dependency>
```

## Bibliothèques natives

JOID s'appuie sur LWJGL 2 pour OpenGL/OpenAL. Le dossier `native/` dans le repo JOID contient les natives d'exécution (`lwjgl64.dll`, `OpenAL64.dll`, et leurs variantes de plateformes).

Copiez ce dossier à côté de votre projet et lancez avec :

```
-Djava.library.path=./native
```

> TIP: Si vous avez déjà les natives LWJGL 2 d'un autre projet, elles sont compatibles. Assurez-vous simplement que `OpenAL64.dll` (ou `libopenal.so` / `libopenal.dylib`) est présent — nécessaire pour l'audio du `VideoPlayerNode`.

## Dépendances

JOID shade ses petites dépendances utilitaires (Guava, Gson, commons-lang3, commons-compress, commons-io, vecmath) dans le fat JAR. **Les plus grosses dépendances d'exécution restent externes** pour que vous choisissiez les classifiers dont vous avez besoin et évitiez d'alourdir votre artefact.

Ajoutez ceci à votre `build.gradle` à côté de JOID :

```groovy
dependencies {
    compile files('libs/joid-6.0.0-prod.jar')

    compile 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'

    compile 'org.lwjgl.lwjgl:lwjgl:2.9.1'

    compile('org.bytedeco:javacv:1.5.9') { transitive = false }
    compile 'org.bytedeco:javacpp:1.5.9'
    compile('org.bytedeco:ffmpeg:6.0-1.5.9') { transitive = false }

    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:windows-x86_64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:macosx-x86_64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:macosx-arm64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:linux-x86_64'
}
```

Le bloc Maven équivalent :

```xml
<dependencies>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>1.18.34</version><scope>provided</scope></dependency>
    <dependency><groupId>org.lwjgl.lwjgl</groupId><artifactId>lwjgl</artifactId><version>2.9.1</version></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>javacv</artifactId><version>1.5.9</version><exclusions><exclusion><groupId>*</groupId><artifactId>*</artifactId></exclusion></exclusions></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>javacpp</artifactId><version>1.5.9</version></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><exclusions><exclusion><groupId>*</groupId><artifactId>*</artifactId></exclusion></exclusions></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>windows-x86_64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>macosx-x86_64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>macosx-arm64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>linux-x86_64</classifier></dependency>
</dependencies>
```

### Rôle de chaque dépendance

| Dépendance | Utilité | Requis |
|---|---|---|
| `lombok` | Génération de code (`@Getter`, `@Setter`, `@NonNull`) | Compile seulement — non shippée |
| `lwjgl 2.9.1` | Bindings OpenGL + OpenAL | Toujours |
| `javacv` + `javacpp` | Bindings Java pour FFmpeg | Seulement si `VideoPlayerNode` est utilisé |
| `ffmpeg:6.0-1.5.9` (base) | Classes de l'API FFmpeg | Seulement si `VideoPlayerNode` est utilisé |
| `ffmpeg:…:<plateforme>` | Natives `.dll` / `.so` / `.dylib` pour l'OS cible | Ne shippez que celles ciblées |

> NOTE: `transitive = false` sur `javacv` et `ffmpeg` empêche Gradle de rapatrier tous les classifiers pour toutes les plateformes (~700 Mo). Déclarez uniquement les classifiers que vous shippez vraiment.

### Shipping en production

**Ne fat-JAR pas votre app.** La disposition recommandée est un JAR d'application mince qui référence chaque JAR de dépendance depuis un dossier voisin `libraries/`, chargé via le classpath. Ça garde l'artefact d'application petit, vous permet de remplacer ou patcher une dépendance sans rebuild, et évite les cas tordus où `shadowJar` / Maven Shade réécrit les classes de `javacpp` / `ffmpeg` et casse l'extraction des natives.

Exemple de disposition pour un zip distributable :

```
my-app/
├── my-app.jar                           # votre code seul — pas de shading
├── libraries/                           # toutes les dépendances compile, un JAR par artefact
│   ├── joid-6.0.0-prod.jar
│   ├── lwjgl-2.9.1.jar
│   ├── javacv-1.5.9.jar
│   ├── javacpp-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9-windows-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-arm64.jar
│   └── ffmpeg-6.0-1.5.9-linux-x86_64.jar
├── native/                              # natives LWJGL 2 (OpenGL + OpenAL)
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

Si vous abandonnez le support vidéo, vous pouvez retirer `javacv`, `javacpp` et toutes les entrées `ffmpeg` de `libraries/` — `VideoPlayerNode` est le seul consommateur, et JOID échoue proprement si ses classes sont absentes à l'exécution.

## Vérification

Glissez ceci dans un `main` :

```java
public static void main(String[] args) {
    System.out.println("JOID version: " + JOID.VERSION);
}
```

Si la version installée s'affiche, vous êtes prêt. Continuez avec [Quick Start](quick-start.md).