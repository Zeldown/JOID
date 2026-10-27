---
summary: Operating system that JOID runs on, and where it keeps its cache there.
---

Learn: [Where JOID stores files](/use/learn/running/files/).

## current()

Returns the system named by the `os.name` property: macOS for a name with `mac` or `darwin`, Windows for a name with `win`, Linux otherwise.

## is64Bit()

If `true`, the `os.arch` property names a 64-bit architecture.

## getCacheDir(String,Function)

Returns the cache folder of the user of the folder `home` on this system, reading the environment variables through `environment`: `%LOCALAPPDATA%` (or `AppData/Local`) on Windows, `Library/Caches` on macOS, `$XDG_CACHE_HOME` (or `.cache`) on Linux.

## WINDOWS

Windows.

## MACOS

macOS.

## LINUX

Linux, and every system that is neither Windows nor macOS.
