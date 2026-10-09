package dev.joid.lib.signal.replay;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayCall {

	private final StackTraceElement caller;
	private final String            setter;

	public static SignalReplayCall create(final StackTraceElement caller, final String setter) {
		return new SignalReplayCall(caller, setter);
	}

	public String getKey() {
		return this.caller.getClassName() + "#" + this.caller.getMethodName() + ":" + this.caller.getLineNumber() + "#" + this.setter;
	}

	public String describe(final List<String> nameList, final int readCount) {
		final String file = this.caller.getFileName() != null ? this.caller.getFileName() : this.caller.getClassName().substring(this.caller.getClassName().lastIndexOf('.') + 1) + ".java";
		final String names = !nameList.isEmpty() ? String.join(", ", nameList) : readCount == 1 ? "a signal" : readCount + " signals";
		return file + (this.caller.getLineNumber() >= 0 ? ":" + this.caller.getLineNumber() : "") + " " + this.setter + "(...) reads " + names;
	}

}