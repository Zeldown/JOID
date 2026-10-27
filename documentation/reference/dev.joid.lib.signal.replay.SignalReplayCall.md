---
summary: One setter call in the code of the application: the line of the caller and the name of the setter.
---

[[SignalReplay]] uses it as the key of its caches and to write its warnings.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## of(StackTraceElement,String)

Returns the call of `setter` made at the line `caller`.

## getCaller()

Returns the stack frame of the calling line.

## getSetter()

Returns the name of the setter.

## getKey()

Returns the key of the call: class, method, line and setter.

## describe(List,int)

Returns the start of the warning of this call, as in `MenuUI.java:42 text(...) reads clicks`; without names, it counts the signals read.
