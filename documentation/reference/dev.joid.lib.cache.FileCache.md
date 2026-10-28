---
summary: The cache folder of JOID, split into named areas whose entries expire when they go unused.
---

Ask for an area by name with [[FileCacheArea#of]] and keep it in a constant ([Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache)).
JOID keeps its own areas there too ([Know what JOID writes](/use/learn/running/files/#know-what-joid-writes)).

Every entry that has not been read for longer than the retention of its area is deleted when JOID starts.
Use the cache for files that you can build again; data that must survive goes in the data folder.

Learn: [Where JOID stores files](/use/learn/running/files/).

## clean()

Deletes the expired entries of every area of the cache folder, and the temporary files older than one day, then returns the number of bytes freed.
JOID runs it on a background thread when it starts and logs what it freed.
