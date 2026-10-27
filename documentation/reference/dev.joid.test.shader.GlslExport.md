---
summary: Writes every built-in shader of JOID translated to every GLSL dialect, to check a translation with the compilers of each dialect.
---

Learn: [Check your translation](/build/shader-translation/#check-your-translation).

## main(String[])

Exports into the folder given as the only argument; with another number of arguments, prints its usage and exits with `2`.

## export(File)

Writes, in one subfolder per dialect, the `.vert` and `.frag` of every built-in shader and of its line variant.
Throws an `IOException` when a subfolder cannot be created.
