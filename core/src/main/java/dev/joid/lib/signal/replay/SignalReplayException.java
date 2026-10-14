package dev.joid.lib.signal.replay;

import java.util.Locale;

import lombok.Getter;

@Getter
public class SignalReplayException extends RuntimeException {

	private final Object[]            arguments;
	private final SignalReplayFailure failure;

	public SignalReplayException(final SignalReplayFailure failure, final Object... arguments) {
		super(String.format(Locale.ROOT, failure.getReason(), arguments), null, false, false);
		this.failure   = failure;
		this.arguments = arguments;
	}

	public String getAdvice() {
		return String.format(Locale.ROOT, this.failure.getAdvice(), this.arguments);
	}

}