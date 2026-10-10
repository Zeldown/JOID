package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;

public class StringSignal extends Signal<String> {

	public StringSignal() {}

	public StringSignal(final String value) {
		super(value);
	}

	public static StringSignal of(final String defaultValue) {
		final StringSignal instance = new StringSignal(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public void append(final String value) {
		final String current = this.peek();
		this.set(current == null ? value : current + value);
	}

	public void concat(final String str) {
		final String current = this.peek();
		this.set(current == null ? str : current.concat(str));
	}

	public void replace(final char oldChar, final char newChar) {
		final String updatedValue = this.peek().replace(oldChar, newChar);
		this.set(updatedValue);
	}

	public void replace(final CharSequence target, final CharSequence replacement) {
		final String updatedValue = this.peek().replace(target, replacement);
		this.set(updatedValue);
	}

	public void toLowerCase() {
		final String updatedValue = this.peek().toLowerCase();
		this.set(updatedValue);
	}

	public void toUpperCase() {
		final String updatedValue = this.peek().toUpperCase();
		this.set(updatedValue);
	}

	public void trim() {
		final String updatedValue = this.peek().trim();
		this.set(updatedValue);
	}

	public void substring(final int beginIndex) {
		final String updatedValue = this.peek().substring(beginIndex);
		this.set(updatedValue);
	}

	public void substring(final int beginIndex, final int endIndex) {
		final String updatedValue = this.peek().substring(beginIndex, endIndex);
		this.set(updatedValue);
	}

	public void intern() {
		final String updatedValue = this.peek().intern();
		this.set(updatedValue);
	}

	@Override
	public String toString() {
		return this.peek() == null ? "StringSignal{null}" : "StringSignal{" + this.peek().toString() + "}";
	}

}