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
		super.transform(current -> current + value);
		return this;
	}

	public @NonNull StringSignal replace(final char oldChar, final char newChar) {
		super.transform(current -> current.replace(oldChar, newChar));
		return this;
	}

	public @NonNull StringSignal replace(final CharSequence target, final CharSequence replacement) {
		super.transform(current -> current.replace(target, replacement));
		return this;
	}

	public @NonNull StringSignal toLowerCase() {
		super.transform(String::toLowerCase);
		return this;
	}

	public @NonNull StringSignal toUpperCase() {
		super.transform(String::toUpperCase);
		return this;
	}

	public @NonNull StringSignal trim() {
		super.transform(String::trim);
		return this;
	}

	public @NonNull StringSignal substring(final int beginIndex) {
		super.transform(current -> current.substring(beginIndex));
		return this;
	}

	public @NonNull StringSignal substring(final int beginIndex, final int endIndex) {
		super.transform(current -> current.substring(beginIndex, endIndex));
		return this;
	}

}