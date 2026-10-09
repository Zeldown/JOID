package dev.joid.lib.font.converter;

import lombok.NonNull;

public interface ITextConverter {

	public boolean supports(final @NonNull Object text);

	public @NonNull String convert(final @NonNull Object text);

}