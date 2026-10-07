package dev.joid.lib.utils.signal.replay;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignalReplayFailure {

	CLASS_NOT_FOUND("the bytecode of %s cannot be read", "Use map(...) or a lambda."),
	MEMBER_NOT_FOUND("%s does not exist at runtime", "Configure the ISignalReplayRemapper of the bridge or use map(...)."),
	CALL_NOT_FOUND("no call to %s(...) is found %s, the .class file on disk may no longer match the loaded class (recompiled since the launch)", "Restart the application, or use map(...) or a lambda."),
	AMBIGUOUS_CALL("several calls to %s(...) on this line give the same value from the same signals", "Write one call per line."),
	UNSUPPORTED_INSTRUCTION("the instruction %s is not supported", "Use map(...) or a lambda."),
	LAMBDA("the expression contains a lambda", "Move the lambda out of the expression or use Signal.from(() -> ...)."),
	LOCAL_COMBINED("%s is combined with a signal", "Use a field, map(...) or a lambda."),
	LOCAL_CONDITION("%s decides a condition", "Use a field, map(...) or a lambda."),
	VALUE_DIFFERS("replaying the expression gives %s (side effects, random, time)", "Use a lambda."),
	SIGNALS_DIFFER("replaying the expression reads other signals", "Use map(...) or a lambda."),
	ACCESS_REFUSED("the module of %s refuses the access", "Add opens %2$s to dev.joid in its module-info."),
	REPLAY_FAILED("replaying the expression failed (%s)", "Use map(...) or a lambda.");

	private final String reason;
	private final String advice;

}