package dev.joid.lib.draw.text;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.utils.align.Align;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawText {

	private static final DrawText INSTANCE = new DrawText();

	public static @NonNull DrawText inst() {
		return DrawText.INSTANCE;
	}

	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull Text text) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		final double runX = text.getAlignX().isCenter() ? x - text.getWidth() / 2D : text.getAlignX().isEnd() ? x - text.getWidth() : x;
		final double runY = text.getAlignY().isCenter() ? y - text.getHeight() / 2D : text.getAlignY().isEnd() ? y - text.getHeight() : y;
		final double runWidth = text.getWidth();
		final double runHeight = text.getHeight();
		final double baseline = text.getElementList().stream().mapToDouble(element -> element.getInfo().getBaseline()).max().orElse(0D);

		double penX = runX;
		TextInfo previous = null;

		for (final TextElement element : text.getElementList()) {
			final String drawText = text.getText(element);
			final TextInfo info = element.getInfo();
			final boolean empty = drawText.isEmpty();
			penX += previous == null || empty ? 0D : previous.getLetterSpacing() * previous.getFontSize();
			penX += info.getFont().getTextRenderer().drawText(penX, runY + baseline - info.getBaseline(), drawText, info, runX, runY, runWidth, runHeight).getWidth();
			previous = empty ? previous : info;
		}

		return text.getBounds();
	}

	public @NonNull FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull Text text, final @NonNull TextMode mode) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		final double ox = text.getAlignX().isCenter() ? x + width / 2D : text.getAlignX().isEnd() ? x + width : x;
		if (mode == TextMode.NORMAL || mode == TextMode.OVERFLOW) {
			double oy = y;

			if (text.getAlignY().isCenter()) {
				oy = y + height / 2D;
			} else if (text.getAlignY().isEnd()) {
				oy = y + height;
			}

			if (mode == TextMode.OVERFLOW) {
				final Text overflowText = text.copyProperties().modifier(null);
				final String overflowStr = text.getWidth() <= width ? "" : text.getOverflow().getOverflow();

				boolean overflow = false;
				double overflowWidth = 0D;
				for (final TextElement element : text.getElementList()) {
					final String elementText = text.getText(element);
					final int[] tags = DrawText.tags(element, elementText);
					int fit = 0;
					for (int i = 0; i <= elementText.length(); i++) {
						if (i < elementText.length() && (tags[i] != -1 && tags[i] != i || Character.isLowSurrogate(elementText.charAt(i)))) {
							continue;
						}

						final String subText = elementText.substring(0, i);
						if (overflowWidth + element.getInfo().getWidth(subText + overflowStr) > width && !subText.isEmpty()) {
							overflowText.add(element.copyWithText(elementText.substring(0, fit)).modifier(null));
							overflow = true;
							break;
						}

						fit = i;
					}

					if (overflow) {
						break;
					}

					overflowWidth += elementText.isEmpty() ? 0D : element.getInfo().getWidth(elementText) + element.getInfo().getLetterSpacing() * element.getInfo().getFontSize();
					overflowText.add(element.copyWithText(elementText).modifier(null));
				}

				if (overflowText.isEmpty() || overflowText.getText().isEmpty()) {
					return FontBounds.empty();
				}

				if (overflow) {
					final TextElement lastElement = overflowText.getElementList().get(overflowText.getElementList().size() - 1);
					lastElement.text(overflowText.getText(lastElement) + overflowText.getOverflow().getOverflow());
				}

				this.drawText(ox, oy, overflowText);
				return FontBounds.create(width, overflowText.getHeight());
			}

			return this.drawText(ox, oy, text);
		}

		final List<Text> textList = this.getLines(width, text);
		if (textList.isEmpty()) {
			return FontBounds.empty();
		}

		double oy = y;

		if (text.getAlignY().isCenter()) {
			oy = y + height / 2D;
			for (final Text line : textList) {
				oy -= line.dh(2D);
			}
		} else if (text.getAlignY().isEnd()) {
			oy = y + height;
			for (final Text line : textList) {
				oy -= line.getHeight();
			}
		}

		double heightSum = 0D;
		for (final Text line : textList) {
			if (mode == TextMode.BOX && (oy + line.getHeight() > y + height || oy < y)) {
				oy += line.getHeight();
				continue;
			}

			this.drawText(ox, oy, line.alignY(Align.START));
			oy += line.getHeight();
			heightSum += line.getHeight();
		}

		return FontBounds.create(width, heightSum);
	}

	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align alignX, final @NonNull Align alignY) {
		return this.drawText(x, y, Text.create(text, info).alignX(alignX).alignY(alignY));
	}

	public @NonNull FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align alignX, final @NonNull Align alignY, final @NonNull TextOverflow overflow, final @NonNull TextMode mode) {
		return this.drawText(x, y, width, height, Text.create(text, info).alignX(alignX).alignY(alignY).overflow(overflow), mode);
	}

	public @NonNull List<@NonNull Text> getLines(final double width, final @NonNull Text text) {
		final List<Text> textList = new LinkedList<>();

		Text currentText = text.copyProperties().modifier(null);
		double spacing = 0D;
		for (final TextElement element : text.getElementList()) {
			final String elementText = text.getText(element).replace("<br>", String.valueOf('\n'));
			final int[] tags = DrawText.tags(element, elementText);
			String opened = "";
			int lastSplit = 0;
			for (int i = 0; i < elementText.length(); i++) {
				if (tags[i] != -1 || Character.isLowSurrogate(elementText.charAt(i))) {
					continue;
				}

				final char c = elementText.charAt(i);
				if (c == '\n' || c == '\r') {
					currentText.add(element.copyWithText(opened + elementText.substring(lastSplit, i)).modifier(null));
					textList.add(currentText);
					currentText = text.copyProperties().modifier(null).add(element.copyWithText("").modifier(null));
					spacing = 0D;
					if (elementText.startsWith("\r\n", i)) {
						i++;
					}
					lastSplit = i + 1;
					opened = DrawText.opened(elementText, tags, lastSplit);
					continue;
				}

				final String part = opened + elementText.substring(lastSplit, i + 1);
				final double elementWidth = element.getInfo().getWidth(part);
				if (currentText.getWidth() + spacing + elementWidth > width) {
					int foundSplit = currentText.getText().isEmpty() ? i : lastSplit;
					for (int j = i; j > lastSplit; j--) {
						if (elementText.charAt(j) == ' ' && tags[j] == -1) {
							foundSplit = j;
							break;
						}
					}

					if (foundSplit > lastSplit || !currentText.getText().isEmpty()) {
						currentText.add(element.copyWithText(opened + elementText.substring(lastSplit, foundSplit)).modifier(null));
						textList.add(currentText);
						currentText = text.copyProperties().modifier(null);
						spacing = 0D;
						lastSplit = elementText.charAt(foundSplit) == ' ' ? foundSplit + 1 : foundSplit;
						opened = DrawText.opened(elementText, tags, lastSplit);
						i--;
					}
				}
			}

			if (lastSplit < elementText.length()) {
				currentText.add(element.copyWithText(opened + elementText.substring(lastSplit)).modifier(null));
				spacing = element.getInfo().getLetterSpacing() * element.getInfo().getFontSize();
			}
		}

		if (!currentText.isEmpty()) {
			textList.add(currentText);
		}

		return textList;
	}

	public @NonNull List<@NonNull String> getLines(final double width, final @NonNull String text, final @NonNull TextInfo info) {
		return this.getLines(width, Text.create(text, info)).stream().map(Text::getText).collect(Collectors.toList());
	}

	private static @NonNull int[] tags(final @NonNull TextElement element, final @NonNull String text) {
		return TextMarkup.tags(element.getInfo().getMarkups(), text, element.getInfo().getStyle().derive());
	}

	private static @NonNull String opened(final @NonNull String text, final @NonNull int[] tags, final int end) {
		final StringBuilder opened = new StringBuilder();
		for (int i = 0; i < end; i++) {
			if (tags[i] != -1) {
				opened.append(text.charAt(i));
			}
		}
		return opened.toString();
	}

}