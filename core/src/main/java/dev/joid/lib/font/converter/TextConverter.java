package dev.joid.lib.font.converter;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextConverter {

	private static final Registry<ITextConverter> CONVERTERS = Registry.create();

	public static void register(final @NonNull ITextConverter converter) {
		TextConverter.CONVERTERS.register(converter);
	}

	public static void unregister(final @NonNull ITextConverter converter) {
		TextConverter.CONVERTERS.unregister(converter);
	}

	public static @NonNull String convert(final @NonNull Object text) {
		if (text instanceof String) {
			return (String) text;
		}

		final ITextConverter converter = TextConverter.CONVERTERS.find(candidate -> candidate.supports(text));
		return converter != null ? converter.convert(text) : text.toString();
	}

	public static @NonNull List<@NonNull String> convertLines(final @NonNull Object content) {
		final List<String> lines = new ArrayList<>();
		if (content instanceof Iterable) {
			for (final Object line : (Iterable<?>) content) {
				lines.add(TextConverter.convert(line));
			}
		} else {
			lines.add(TextConverter.convert(content));
		}
		return lines;
	}

}