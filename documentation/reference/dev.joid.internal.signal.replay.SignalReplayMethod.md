---
summary: Analysis of one method of the bytecode: frames, instructions, local variables and the expressions that define them.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## of(SignalReplayClass,MethodNode)

Analyses `method` of `replayClass`; throws the `AnalyzerException` of ASM when the bytecode is invalid.

## isStatic()

If `true`, the method is static and has no `this`.

## getMethod()

Returns the method of the bytecode.

## getFrames()

Returns the stack frames before each instruction.

## getInstructions()

Returns the instructions in their order.

## getDefinitionMap()

Returns, for each store of a local variable, the slice that computes the stored value.

## indexOf(AbstractInsnNode)

Returns the position of `instruction` in the method.

## expressionStart(Collection,int)

Returns the position of the first instruction of the expression whose values come from `sourceList` and which ends at `end`.

## localName(int,int)

Returns the name of the local variable `variable` at the instruction `index`, or `null` without debug information.

## describeLocal(int,int)

Returns how a warning names the local variable: `this`, `the local variable name` or `a local variable`.

## signalName(int)

Returns the name of the field, variable or method that gives the signal read by the instruction `index`, or `null`.

## isSignal(String)

If `true`, the class `owner` is a [[Signal]].

## isParameter(int)

If `true`, the local variable `variable` is a parameter of the method.

## member(AbstractInsnNode)

Returns the field or method that `instruction` names, resolved once with the [[ISignalReplayBridge]].

## type(Type)

Returns the class of `type`, loaded through the class loader of the class and mapped by the [[ISignalReplayBridge]].
