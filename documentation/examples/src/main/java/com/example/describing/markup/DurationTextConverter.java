package com.example.describing.markup;

import java.time.Duration;

import dev.joid.lib.font.converter.ITextConverter;

// #region class
public final class DurationTextConverter implements ITextConverter {

	@Override
	public boolean supports(final Object text) {
		return text instanceof Duration;
	}

	@Override
	public String convert(final Object text) {
		final Duration duration = (Duration) text;
		return duration.toMinutes() + " min " + duration.getSeconds() % 60L + " s";
	}

}
// #endregion