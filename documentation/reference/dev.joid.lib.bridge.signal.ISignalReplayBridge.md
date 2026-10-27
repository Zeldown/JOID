---
summary: The bridge that maps the class, field and method names compiled in a reactive expression to the names that exist at run time.
---

The signal replay reads the bytecode of an expression given to a reactive setter and looks up what it reads by name.
A host that renames classes or members when the application loads registers this bridge in `BridgeHandler.SIGNAL_REPLAY`; every method returns the name unchanged by default.
This bridge reads the names of the run time from two tables that the host provides:

```java file="com/example/backend/EngineSignalReplayBridge.java" region="replay"
```

Learn: [Signal replay bridge](/build/bridges/signal-replay/), [How signals work](/use/in-depth/how-signals-work/).

## mapClass(String)

Returns the run-time internal name, with `/` separators, of the class compiled under that internal name.

## mapField(String,String,String)

Returns the run-time name of the field compiled with that owner, name and descriptor.

## mapMethod(String,String,String)

Returns the run-time name of the method compiled with that owner, name and descriptor.
