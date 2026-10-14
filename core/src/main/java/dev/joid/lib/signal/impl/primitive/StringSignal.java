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

	public static @NonNull StringSignal of(final @NonNull String defaultValue) {
		final StringSignal instance = new StringSignal(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull StringSignal append(final String value) {
		final String updatedValue = this.peek() + value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal concat(final String str) {
		final String updatedValue = this.peek().concat(str);
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal replace(final char oldChar, final char newChar) {
		final String updatedValue = this.peek().replace(oldChar, newChar);
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal replace(final CharSequence target, final CharSequence replacement) {
		final String updatedValue = this.peek().replace(target, replacement);
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal toLowerCase() {
		final String updatedValue = this.peek().toLowerCase();
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal toUpperCase() {
		final String updatedValue = this.peek().toUpperCase();
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal trim() {
		final String updatedValue = this.peek().trim();
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal substring(final int beginIndex) {
		final String updatedValue = this.peek().substring(beginIndex);
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal substring(final int beginIndex, final int endIndex) {
		final String updatedValue = this.peek().substring(beginIndex, endIndex);
		this.set(updatedValue);
		return this;
	}

	public @NonNull StringSignal intern() {
		final String updatedValue = this.peek().intern();
		this.set(updatedValue);
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "StringSignal{null}" : "StringSignal{" + this.peek().toString() + "}";
	}

}