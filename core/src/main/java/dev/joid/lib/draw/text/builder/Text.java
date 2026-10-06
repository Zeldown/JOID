package dev.joid.lib.draw.text.builder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public final class Text {

	@Getter
	private final List<TextElement> elementList;

	@Getter private TextOverflow  overflow;
	@Getter private ITextModifier modifier;
	@Getter private Align         verticalAlignment;
	@Getter private Align         horizontalAlignment;

	private double width;
	private double height;
	private String measuredText;

	protected Text() {
		this.elementList         = new LinkedList<>();
		this.horizontalAlignment = Align.START;
		this.verticalAlignment   = Align.START;
		this.overflow            = TextOverflow.NONE;
	}

	protected Text(final @NonNull List<@NonNull TextElement> elementList) {
		this.elementList         = new LinkedList<>(elementList);
		this.horizontalAlignment = Align.START;
		this.verticalAlignment   = Align.START;
		this.overflow            = TextOverflow.NONE;
	}

	public static final @NonNull Text create() {
		return new Text();
	}

	public static final @NonNull Text create(final @NonNull TextElement @NonNull... elementList) {
		return new Text(Arrays.asList(elementList));
	}

	public static final @NonNull Text create(final @NonNull List<@NonNull TextElement> elementList) {
		return new Text(elementList);
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info) {
		return new Text().add(TextElement.create(text, info));
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info) {
		return new Text().add(TextElement.create(text, info));
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign);
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).overflow(overflow);
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign);
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).overflow(overflow);
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info, final @NonNull Align align, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(align).overflow(overflow);
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign);
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final @NonNull Align align, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(align).overflow(overflow);
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign);
	}

	public static final @NonNull Text create(final @NonNull Object text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign).overflow(overflow);
	}

	public static final @NonNull Text create(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign, final @NonNull TextOverflow overflow) {
		return new Text().add(TextElement.create(text, info)).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign).overflow(overflow);
	}

	public final double getWidth() {
		this.measure();
		return this.width;
	}

	public final double getHeight() {
		this.measure();
		return this.height;
	}

	public final @NonNull String getText() {
		if (this.elementList.isEmpty()) {
			return "";
		}

		final String text = this.elementList.stream().map(TextElement::getText).reduce("", (a, b) -> a + b);
		return this.modifier != null ? this.modifier.modify(text) : text;
	}

	public final @NonNull String getText(final @NonNull TextElement element) {
		if (this.modifier == null) {
			return element.getText();
		}

		final StringBuilder before = new StringBuilder();
		for (final TextElement previous : this.elementList) {
			if (previous == element) {
				final String start = this.modifier.modify(before.toString());
				final String text = this.modifier.modify(before.append(element.getText()).toString());
				return text.startsWith(start) ? text.substring(start.length()) : this.modifier.modify(element.getText());
			}

			before.append(previous.getText());
		}

		return this.modifier.modify(element.getText());
	}

	public final @NonNull String getRawText() {
		if (this.elementList.isEmpty()) {
			return "";
		}

		return this.elementList.stream().map(TextElement::getText).reduce("", (a, b) -> a + b);
	}

	public final @NonNull FontBounds getBounds() {
		return new FontBounds(this.getWidth(), this.getHeight());
	}

	public @NonNull TextElement get(final int index) {
		return this.elementList.get(index);
	}

	public @NonNull Text text(final @NonNull String text) {
		return this.text(0, text);
	}

	public @NonNull Text text(final int index, final @NonNull String text) {
		if (index < 0 || index >= this.elementList.size()) {
			return this;
		}

		this.elementList.get(index).text(text);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text text(final @NonNull Supplier<?> text) {
		return this.text(0, text);
	}

	public @NonNull Text text(final int index, final @NonNull Supplier<?> text) {
		if (index < 0 || index >= this.elementList.size()) {
			return this;
		}

		this.elementList.get(index).text(text);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text info(final @NonNull TextInfo info) {
		return this.info(0, info);
	}

	public @NonNull Text info(final int index, final @NonNull TextInfo info) {
		if (index < 0 || index >= this.elementList.size()) {
			return this;
		}

		this.elementList.get(index).info(info);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text addAll(final @NonNull List<@NonNull TextElement> elementList) {
		this.elementList.addAll(elementList);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text add(final @NonNull Text builder) {
		this.elementList.addAll(builder.getElementList());
		this.measuredText = null;
		return this;
	}

	public @NonNull Text add(final @NonNull TextElement element) {
		this.elementList.add(element);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text remove(final @NonNull TextElement element) {
		this.elementList.remove(element);
		this.measuredText = null;
		return this;
	}

	public @NonNull Text clear() {
		this.elementList.clear();
		this.measuredText = null;
		return this;
	}

	public @NonNull Text copy() {
		return new Text(this.elementList).overflow(this.overflow).align(this.horizontalAlignment, this.verticalAlignment).modifier(this.modifier);
	}

	public @NonNull Text copyProperties() {
		return new Text().overflow(this.overflow).align(this.horizontalAlignment, this.verticalAlignment).modifier(this.modifier);
	}

	public @NonNull Text copyWithModifier(final ITextModifier modifier) {
		return new Text(this.elementList).overflow(this.overflow).align(this.horizontalAlignment, this.verticalAlignment).modifier(modifier);
	}

	public @NonNull Text copyWithVerticalAlign(final @NonNull Align align) {
		return new Text(this.elementList).overflow(this.overflow).horizontalAlign(this.horizontalAlignment).verticalAlign(align).modifier(this.modifier);
	}

	public @NonNull Text copyWithHorizontalAlign(final @NonNull Align align) {
		return new Text(this.elementList).overflow(this.overflow).horizontalAlign(align).verticalAlign(this.verticalAlignment).modifier(this.modifier);
	}

	public @NonNull Text copyWithOverflow(final @NonNull TextOverflow overflow) {
		return new Text(this.elementList).overflow(overflow).align(this.horizontalAlignment, this.verticalAlignment).modifier(this.modifier);
	}

	public final @NonNull Text align(final @NonNull Align horizontal, final @NonNull Align vertical) {
		this.horizontalAlignment = horizontal;
		this.verticalAlignment   = vertical;
		return this;
	}

	public final @NonNull Text verticalAlign(final @NonNull Align align) {
		this.verticalAlignment = align;
		return this;
	}

	public final @NonNull Text horizontalAlign(final @NonNull Align align) {
		this.horizontalAlignment = align;
		return this;
	}

	public final @NonNull Text overflow(final @NonNull TextOverflow overflow) {
		this.overflow = overflow;
		return this;
	}

	public final @NonNull Text modifier(final ITextModifier modifier) {
		this.modifier     = modifier;
		this.measuredText = null;
		return this;
	}

	public final boolean isEmpty() {
		return this.elementList.isEmpty();
	}

	public final double dw(final double value) {
		return this.getWidth() / value;
	}

	public final double dh(final double value) {
		return this.getHeight() / value;
	}

	public final double aw(final double value) {
		return this.getWidth() + value;
	}

	public final double ah(final double value) {
		return this.getHeight() + value;
	}

	private void measure() {
		final String text = this.getRawText();
		if (text.equals(this.measuredText)) {
			return;
		}

		this.measuredText = text;
		this.width        = this.elementList.stream().mapToDouble(element -> FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(this.getText(element)))).sum();
		this.height       = this.elementList.stream().mapToDouble(element -> FontUsage.trace(element.getOrigin(), () -> element.getInfo().getHeight(this.getText(element)))).max().orElse(0);
	}

	@Override
	public String toString() {
		return "\"" + this.getText() + "\" - " + this.horizontalAlignment + " / " + this.verticalAlignment + " - " + this.overflow + " [" + this.elementList.size() + "]";
	}

}