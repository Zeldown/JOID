package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StringSignal extends Signal<String> {

	public StringSignal(final String value) {
		super(value);
	}

	public static StringSignal of(final String defaultValue) {
		final StringSignal instance = new StringSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void append(final String value) {
		final String updatedValue = this.getOrDefault() + value;
		this.set(updatedValue);
	}

	public void concat(final String str) {
		final String updatedValue = this.getOrDefault().concat(str);
		this.set(updatedValue);
	}

	public void replace(final char oldChar, final char newChar) {
		final String updatedValue = this.getOrDefault().replace(oldChar, newChar);
		this.set(updatedValue);
	}

	public void replace(final CharSequence target, final CharSequence replacement) {
		final String updatedValue = this.getOrDefault().replace(target, replacement);
		this.set(updatedValue);
	}

	public void toLowerCase() {
		final String updatedValue = this.getOrDefault().toLowerCase();
		this.set(updatedValue);
	}

	public void toUpperCase() {
		final String updatedValue = this.getOrDefault().toUpperCase();
		this.set(updatedValue);
	}

	public void trim() {
		final String updatedValue = this.getOrDefault().trim();
		this.set(updatedValue);
	}

	public void substring(final int beginIndex) {
		final String updatedValue = this.getOrDefault().substring(beginIndex);
		this.set(updatedValue);
	}

	public void substring(final int beginIndex, final int endIndex) {
		final String updatedValue = this.getOrDefault().substring(beginIndex, endIndex);
		this.set(updatedValue);
	}

	public void intern() {
		final String updatedValue = this.getOrDefault().intern();
		this.set(updatedValue);
	}

}