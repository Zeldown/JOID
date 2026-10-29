---
title: "Where JOID stores files"
description: "The data and cache folders of JOID, how to move them, and how to keep files of your own in the cache."
genre: learn
sidebar:
  order: 4
---

JOID writes to disk in two folders only: one for the data of your application, one for caches of the machine.
Knowing them tells you what to back up, what you can delete, and where to put them in an installed application.

**You will learn**
- What JOID writes in the data folder and in the cache folder
- Where the two folders are by default
- How to move them, and which setting wins
- How to keep files of your own in the cache, with a retention

## Know what JOID writes

The **data folder** belongs to your application: delete it and the users lose their settings.

| Path in the data folder | Written for |
|---|---|
| `store/<id>.store` | each store that [lives on the disk](/use/learn/state/stores-and-properties/) |
| `property/<UI class>.property` | the fields of a UI marked [[UIProperty]]; `<UI class>` is the full name of the class |

[Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk) and [Remember a field of a UI](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui) say when JOID writes them.

The **cache folder** belongs to the machine: JOID rebuilds anything in it when it is missing.
It is split into areas, one folder each:

| Area | Written for | Kept for |
|---|---|---|
| `msdf/` | the atlases that JOID [generates from your fonts](/use/in-depth/msdf-text/#what-happens-when-a-font-loads) | 30 days |
| `video/` | a copy of each video read from the classpath, a stream or a URL; a video read from a plain file plays from that file | 7 days |
| `ffmpeg/` | the FFmpeg natives that the first video downloads for the system, unless the classpath already holds them ([Images and media](/use/learn/describing-the-ui/media/)) | 30 days |
| `lwjgl/` | the LWJGL 2 natives, with the LWJGL 2 backend only | 30 days |

JOID creates a folder the first time it writes into it.
Each use of an entry renews it; an entry that nobody used for longer than its area keeps it is deleted when JOID starts, on a background thread, which prints the space it freed.

## Find the folders

| Folder | Default |
|---|---|
| Data | `joid`, in the working directory of the JVM |
| Cache on Windows | `%LOCALAPPDATA%\joid`, or `AppData\Local\joid` in the home folder when the variable is missing |
| Cache on macOS | `~/Library/Caches/joid` |
| Cache on Linux | `$XDG_CACHE_HOME/joid`, or `~/.cache/joid` when the variable is missing |

JOID prints both folders in its banner when the backend registers.
Your code reads them with [[JOIDEnv#getDataDir]] and [[JOIDEnv#getCacheDir]]:

```java file="com/example/running/RunningFolders.java" region="read"
```

## Move the folders

The working directory of an installed application is often read-only or shared, so give it a data folder of its own.
Set the folders on the backend, before `register()`:

```java file="com/example/running/RunningFolders.java" region="folders"
```

Without code, start the JVM with system properties:

```sh fragment
java -Djoid.data=/srv/my-application/data -Djoid.cache=/var/cache/my-application -jar my-application.jar
```

When several settings name a folder, the first one present wins:

1. [[Backend#dataDir]] and [[Backend#cacheDir]] on the backend, applied by `register()`;
2. the system properties `joid.data` and `joid.cache`, read when JOID starts;
3. the defaults of the table above.

## Keep files of your own in the cache

Your application can keep its own rebuildable files in the cache folder, with the same cleaning as JOID's.
Declare an area once, with [[FileCacheArea#of]] and the time an unused entry stays:

```java file="com/example/running/RunningFolders.java" region="area"
```

Then ask the area for an entry by its key:

```java file="com/example/running/RunningFolders.java" region="compute"
```

[[FileCacheArea#compute]] returns the file of the key when it exists.
Otherwise it gives your writer a temporary path, then moves what the writer created there to the key, so a crash never leaves half a file behind.
Two threads that compute the same key wait for each other, and the second one reads the file of the first.

[[FileCacheArea#remove]] deletes an entry, so that the next `compute` writes it again; [[FileCacheArea]] lists the other methods.

The writer creates a file, or a folder for an entry made of several files.
Area names and keys are plain file names: a letter or a digit, then letters, digits, `.`, `-` and `_`.

:::caution
An area has one retention.
Declaring the same name again with another duration throws `IllegalStateException`: give each use its own area.
:::

## Recap

- The **data folder** holds the stores that live on the disk and the `@UIProperty` fields: it belongs to the application.
- The **cache folder** holds font atlases, video copies and natives, in areas that JOID cleans at startup.
- The data folder defaults to **`joid` in the working directory**; the cache folder to the **cache folder of the system**.
- Priority: **`dataDir`/`cacheDir` on the backend**, then **`-Djoid.data`/`-Djoid.cache`**, then the defaults.
- **`JOIDEnv.getDataDir()`** and **`getCacheDir()`** give the folders in use; the banner prints them.
- **`FileCacheArea.of(name)`** keeps files of your own, written once by `compute` and deleted after their retention.

## Try it

**Settings lost after an update.**
A user reports that the settings of your application are lost after each update, which installs it in a new folder.
What do you change?

<details>
<summary>Solution</summary>

The data folder defaults to `joid` in the working directory, which moves with each install.
Set `dataDir(...)` on the backend to a folder in the user's home, such as `new File(System.getProperty("user.home"), ".my-application/data")`.

</details>

**Thumbnails for a week.**
Your application renders a thumbnail for each level, and may delete the ones unused for a week.
Declare the area.

<details>
<summary>Solution</summary>

```java fragment
private static final FileCacheArea THUMBNAILS = FileCacheArea.of("thumbnails").retention(Duration.ofDays(7L));
```

Then `THUMBNAILS.compute(level, file -> ...)` renders a thumbnail the first time and returns the file afterwards.

</details>
