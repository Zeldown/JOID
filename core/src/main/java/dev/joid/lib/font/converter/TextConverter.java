package dev.joid.lib.font.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextConverter {

	private static final List<ITextConverter> CONVERTERS = new CopyOnWriteArrayList<>();

	public static void register(final @NonNull ITextConverter converter) {
		TextConverter.CONVERTERS.remove(converter);
		TextConverter.CONVERTERS.add(0, converter);
	}

	public static void unregister(final @NonNull ITextConverter converter) {
		TextConverter.CONVERTERS.remove(converter);
	}

	public static @NonNull String convert(final @NonNull Object text) {
		if (text instanceof String) {
			return (String) text;
		}

		for (final ITextConverter converter : TextConverter.CONVERTERS) {
			if (converter.supports(text)) {
				return converter.convert(text);
			}
		}

		return text.toString();
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