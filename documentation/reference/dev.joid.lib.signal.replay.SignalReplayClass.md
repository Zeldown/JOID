---
summary: Bytecode of one class of the application, read once for the replay, with the analyses of its methods.
---

When the class comes from a folder, the replay reads its `.class` file again after it changed on disk, as with hot reload.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## read(String,Class,ClassLoader)

Reads the bytecode of the class `name`, from the file of `type` when it has one, otherwise through `loader`.
Throws a [[SignalReplayException]] when no class loader gives its bytecode.

## isStale()

If `true`, the `.class` file changed on disk since it was read.

## method(MethodNode)

Returns the analysis of `method`, made on the first call.

## getFile()

Returns the `.class` file read, or `null` when the bytecode came from a class loader.

## getModified()

Returns the modification time of the file when it was read.

## getNode()

Returns the parsed bytecode.

## getLoader()

Returns the class loader that loads the classes named in the bytecode.

## getMethodMap()

Returns the analyses of the methods made so far.
