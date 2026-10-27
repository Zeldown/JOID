---
title: "Signal replay bridge"
description: "The contract of the signal replay bridge: map the names of a compiled class to the names that exist at run time."
genre: build
sidebar:
  order: 7
---

A setter given an expression that reads signals replays the bytecode of that expression to find the signals it reads ([How signals work](/use/in-depth/how-signals-work/)); a lambda needs no replay.
The replay resolves classes, fields and methods by the names written in the bytecode.
When a host renames classes or members at run time (an obfuscated or remapped environment), these names do not exist at run time and the replay cannot find them.

The replay bridge translates each name before the replay looks it up.
The core reads it through [[ISignalReplayBridge]], from `BridgeHandler.SIGNAL_REPLAY`.
By default, an identity bridge returns every name unchanged.

## Provided

Every method has an identity default; override the ones your environment needs.

| Member | Receives | Returns |
|---|---|---|
| [[ISignalReplayBridge#mapClass]] | an internal class name, with `/` separators | the internal name of the class at run time |
| [[ISignalReplayBridge#mapField]] | the owner, the name and the descriptor of a field, as compiled | the name of the field at run time |
| [[ISignalReplayBridge#mapMethod]] | the owner, the name and the descriptor of a method, as compiled | the name of the method at run time |

## Guarantees

| Rule | Why |
|---|---|
| Return a name that exists in the class loaded at run time, or the name unchanged. | The replay looks the member up by reflection, in the class and its superclasses. |
| Keep the methods fast and free of side effects. | The replay calls them for every class and member it resolves. |

Register your bridge above the identity one, before the first UI opens:

```java fragment
BridgeHandler.SIGNAL_REPLAY.register(bridge);
```
