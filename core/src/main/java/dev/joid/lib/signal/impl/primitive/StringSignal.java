package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class StringSignal extends Signal<String> {

	public StringSignal() {
		super("");
	}

	public StringSignal(final @NonNull String value) {
		super(value);
	}

	public @NonNull StringSignal append(final String value) {
		return super.transform(current -> current + value);
	}

	public @NonNull StringSignal replace(final char oldChar, final char newChar) {
		return super.transform(current -> current.replace(oldChar, newChar));
	}

	public @NonNull StringSignal replace(final @NonNull CharSequence target, final @NonNull CharSequence replacement) {
		return super.transform(current -> current.replace(target, replacement));
	}

	public @NonNull StringSignal toLowerCase() {
		return super.transform(String::toLowerCase);
	}

	public @NonNull StringSignal toUpperCase() {
		return super.transform(String::toUpperCase);
	}

	public @NonNull StringSignal trim() {
		return super.transform(String::trim);
	}

	public @NonNull StringSignal substring(final int beginIndex) {
		return super.transform(current -> current.substring(beginIndex));
	}

	public @NonNull StringSignal substring(final int beginIndex, final int endIndex) {
		return super.transform(current -> current.substring(beginIndex, endIndex));
	}

}