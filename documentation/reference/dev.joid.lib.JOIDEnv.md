---
summary: The mode JOID runs in and the folders where it writes its data and its caches.
---
JOID turns dev mode on by itself when the dev classes are on the classpath (the dev jar), and demo mode when the demo classes are.
The folders come from the system properties `joid.data` and `joid.cache` when they are set.

Learn: [Dev tools](/use/get-started/dev-tools/), [Where JOID stores files](/use/learn/running/files/).

## isDevMode()

If `true`, the inspector, hot reload, the dev shortcuts and the dev warnings are on.

## devMode(boolean)

Turns dev mode on or off.
Throws an `IllegalStateException` when you turn it on without the dev classes of JOID on the classpath.

## isDemoMode()

If `true`, the demo classes of JOID are on the classpath.

## getDataDir()

Folder where JOID writes the stores and the remembered properties: `joid.data`, or the `joid` folder of the working directory.

## dataDir(File)

Sets the data folder; set it before the first UI opens.

## getCacheDir()

Folder where JOID writes the caches it can rebuild, such as generated font atlases: `joid.cache`, or a `joid` folder in the cache folder of the user on this operating system.

## cacheDir(File)

Sets the cache folder; set it before the backend registers.
