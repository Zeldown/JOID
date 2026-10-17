package dev.joid.lib.draw.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.modifier.TextModifier;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public final class Text {

	private final List<TextElement> elementList;

	@Getter private Align         alignX;
	@Getter private Align         alignY;
	@Getter private TextOverflow  overflow;
	@Getter private TextModifier modifier;

	private double       width;
	private double       height;
	private List<Object> measured;

	private Text(final @NonNull List<@NonNull TextElement> elementList) {
		this.elementList = new LinkedList<>(elementList);
		this.alignX      = Align.START;
		this.alignY      = Align.START;
		this.overflow    = TextOverflow.NONE;
	}

	public static @NonNull Text create(final @NonNull TextElement @NonNull... elementList) {
		return new Text(Arrays.asList(elementList));
	}

	public static @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info) {
		return Text.create(TextElement.create(Signal.from(text), info));
	}

	public static @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info) {
		return Text.create(TextElement.create(text, info));
	}

	public @NonNull List<TextElement> getElementList() {
		return Collections.unmodifiableList(this.elementList);
	}

	public double getWidth() {
		this.measure();
		return this.width;
	}

	public double getHeight() {
		this.measure();
		return this.height;
	}

	public @NonNull String getText() {
		return this.elementList.stream().map(this::getText).collect(Collectors.joining());
	}

	public @NonNull String getText(final @NonNull TextElement element) {
		if (this.modifier == null) {
			return element.getText();
		}

		final TextInfo info = element.getInfo();
		final StringBuilder before = new StringBuilder();
		for (final TextElement previous : this.elementList) {
			if (previous == element) {
				final String start = this.modifier.modify(before.toString(), info);
				final String text = this.modifier.modify(before.append(element.getText()).toString(), info);
				return text.startsWith(start) ? text.substring(start.length()) : this.modifier.modify(element.getText(), info);
			}

			before.append(previous.getText());
		}

		return this.modifier.modify(element.getText(), info);
	}

	public @NonNull String getRawText() {
		return this.elementList.stream().map(TextElement::getRawText).collect(Collectors.joining());
	}

	public @NonNull FontBounds getBounds() {
		return FontBounds.create(this.getWidth(), this.getHeight());
	}

	public @NonNull TextElement get(final int index) {
		if (index < 0 || index >= this.elementList.size()) {
			throw new IndexOutOfBoundsException("The text has no element at index " + index + ", give an index from 0 and lower than its element count (" + this.elementList.size() + ")");
		}
		return this.elementList.get(index);
	}

	public @NonNull Text text(final @NonNull Object text) {
		return this.text(Signal.from(text));
	}

	public @NonNull Text text(final int index, final @NonNull Object text) {
		return this.text(index, Signal.from(text));
	}

	public @NonNull Text text(final @NonNull Supplier<?> text) {
		return this.text(0, text);
	}

	public @NonNull Text text(final int index, final @NonNull Supplier<?> text) {
		this.get(index).text(text);
		return this;
	}

	public @NonNull Text info(final @NonNull TextInfo info) {
		return this.info(0, info);
	}

	public @NonNull Text info(final int index, final @NonNull TextInfo info) {
		this.get(index).info(info);
		return this;
	}

	public @NonNull Text add(final @NonNull Text text) {
		text.getElementList().forEach(element -> this.elementList.add(element.copy()));
		return this;
	}

	public @NonNull Text add(final @NonNull TextElement @NonNull... elements) {
		Collections.addAll(this.elementList, elements);
		return this;
	}

	public @NonNull Text remove(final @NonNull TextElement element) {
		this.elementList.remove(element);
		return this;
	}

	public @NonNull Text clear() {
		this.elementList.clear();
		return this;
	}

	public @NonNull Text copy() {
		final Text copy = this.copyProperties();
		this.elementList.forEach(element -> copy.add(element.copy()));
		return copy;
	}

	public @NonNull Text copyProperties() {
		return Text.create().overflow(this.overflow).alignX(this.alignX).alignY(this.alignY).modifier(this.modifier);
	}

	public @NonNull Text align(final @NonNull Align align) {
		this.alignX = align;
		this.alignY = align;
		return this;
	}

	public @NonNull Text alignX(final @NonNull Align alignX) {
		this.alignX = alignX;
		return this;
	}

	public @NonNull Text alignY(final @NonNull Align alignY) {
		this.alignY = alignY;
		return this;
	}

	public @NonNull Text overflow(final @NonNull TextOverflow overflow) {
		this.overflow = overflow;
		return this;
	}

	public @NonNull Text modifier(final TextModifier modifier) {
		this.modifier = modifier;
		return this;
	}

	public boolean isEmpty() {
		return this.elementList.isEmpty();
	}

	public double dw(final double value) {
		return this.getWidth() / value;
	}

	public double dh(final double value) {
		return this.getHeight() / value;
	}

	public double aw(final double value) {
		return this.getWidth() + value;
	}

	public double ah(final double value) {
		return this.getHeight() + value;
	}

	private void measure() {
		final List<Object> key = this.measureKey();
		if (key.equals(this.measured)) {
			return;
		}

		this.measured     = key;
		this.width        = 0D;
		this.height       = this.elementList.stream().mapToDouble(element -> element.getInfo().getHeight(this.getText(element))).max().orElse(0D);

		TextInfo previous = null;
		for (final TextElement element : this.elementList) {
			final String text = this.getText(element);
			if (text.isEmpty()) {
				continue;
			}

			this.width += (previous == null ? 0D : previous.getLetterSpacing() * previous.getFontSize()) + element.getInfo().getWidth(text);
			previous = element.getInfo();
		}
	}

	private @NonNull List<Object> measureKey() {
		final List<Object> key = new ArrayList<>();
		key.add(this.modifier);
		for (final TextElement element : this.elementList) {
			final TextInfo info = element.getInfo();
			key.add(element.getRawText());
			key.add(element.getModifier());
			key.add(info.getFont());
			key.add(info.getFontSize());
			key.add(info.getWeight());
			key.add(info.isItalic());
			key.add(info.getLetterSpacing());
			key.add(info.getLineHeight());
			key.add(new ArrayList<>(info.getMarkups()));
		}
		return key;
	}

	@Override
	public String toString() {
		return "\"" + this.getText() + "\" - " + this.alignX + " / " + this.alignY + " - " + this.overflow + " [" + this.elementList.size() + "]";
	}

}