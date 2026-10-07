package dev.joid.lib.utils.signal.replay;

import java.util.function.Supplier;

import dev.joid.lib.utils.signal.Signal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayCaption {

	private final Supplier<String> text;

	public static ReplayCaption create(final String text) {
		return new ReplayCaption(Signal.from(text));
	}

}