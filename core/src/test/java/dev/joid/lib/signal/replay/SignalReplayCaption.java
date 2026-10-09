package dev.joid.lib.signal.replay;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayCaption {

	private final Supplier<String> text;

	public static SignalReplayCaption create(final String text) {
		return new SignalReplayCaption(Signal.from(text));
	}

}