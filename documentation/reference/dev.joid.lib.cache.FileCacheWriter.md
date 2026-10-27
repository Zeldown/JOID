---
summary: Writes the content of a cache entry into the file it is given.
---

Pass it as a lambda to [[FileCacheArea#compute]] or [[FileCacheArea#store]].

Learn: [Where JOID stores files](/use/learn/running/files/).

## write(File)

JOID calls it on the calling thread with a temporary path in the area: create that file, or a folder there, with the content of the entry.
Throw `IOException` on failure; the temporary file is then deleted and nothing is cached.
