package dev.joid.lib.draw.text;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.TextElement;
import dev.joid.lib.draw.text.builder.TextOverflow;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.TextStyle;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public final class DrawText {

	@Getter
	private static DrawText instance;

	public DrawText() {
		if (DrawText.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawText.");
		}
		DrawText.instance = this;
	}

	public FontBounds drawText(final double x, final double y, final @NonNull Text text) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		final double runX = text.getHorizontalAlignment().isCenter() ? x - text.getWidth() / 2 : text.getHorizontalAlignment().isEnd() ? x - text.getWidth() : x;
		final double runY = text.getVerticalAlignment().isCenter() ? y - text.getHeight() / 2 : text.getVerticalAlignment().isEnd() ? y - text.getHeight() : y;
		final double runWidth = text.getWidth();
		final double runHeight = text.getHeight();

		double penX = runX;

		for (final TextElement element : text.getElementList()) {
			final String drawText = text.getText(element);
			final TextInfo info = element.getInfo();
			final double pen = penX;
			penX += FontUsage.trace(element.getOrigin(), () -> {
				double oy = runY;
				if (text.getVerticalAlignment().isCenter()) {
					oy += (text.getHeight() - info.getHeight()) / 2;
				} else if (text.getVerticalAlignment().isEnd()) {
					oy += text.getHeight() - info.getHeight();
				}

				return info.getFont().getTextRenderer().drawText(pen, oy, drawText, info, runX, runY, runWidth, runHeight).getWidth();
			});
		}

		return text.getBounds();
	}

	public FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull Text text, final @NonNull TextMode mode) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		if (mode == TextMode.NORMAL || mode == TextMode.OVERFLOW) {
			double ox = x;
			double oy = y;

			if (text.getHorizontalAlignment().isCenter()) {
				ox = x + width / 2;
			} else if (text.getHorizontalAlignment().isEnd()) {
				ox = x + width;
			}

			if (text.getVerticalAlignment().isCenter()) {
				oy = y + height / 2;
			} else if (text.getVerticalAlignment().isEnd()) {
				oy = y + height;
			}

			if (mode == TextMode.OVERFLOW) {
				final Text overflowText = text.copyProperties().modifier(null);
				final String overflowStr = text.getOverflow() == null || text.getWidth() <= width ? "" : text.getOverflow().getOverflow();

				boolean overflow = false;
				double overflowWidth = 0;
				for (final TextElement element : text.getElementList()) {
					final String elementText = text.getText(element);
					final int[] tags = DrawText.tags(element, elementText);
					final double overflowStrWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(overflowStr));
					int fit = 0;
					for (int i = 0; i <= elementText.length(); i++) {
						if (i < elementText.length() && tags[i] != -1 && tags[i] != i) {
							continue;
						}

						final String subText = elementText.substring(0, i);
						final double subTextWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(subText));
						if (overflowWidth + subTextWidth + overflowStrWidth > width && !subText.isEmpty()) {
							overflowText.add(element.copyWithText(elementText.substring(0, fit)).modifier(null));
							overflow = true;
							break;
						}

						fit = i;
					}

					if (overflow) {
						break;
					}

					overflowWidth += FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(elementText));
					overflowText.add(element.copyWithText(elementText).modifier(null));
				}

				if (overflowText.isEmpty() || overflowText.getText().isEmpty()) {
					return FontBounds.empty();
				}

				if (overflow && overflowText.getOverflow() != null) {
					final TextElement lastElement = overflowText.getElementList().get(overflowText.getElementList().size() - 1);
					lastElement.text(overflowText.getText(lastElement) + overflowText.getOverflow().getOverflow());
				}

				this.drawText(ox, oy, overflowText);
				return new FontBounds(width, overflowText.getHeight());
			}

			return this.drawText(ox, oy, text);
		}

		final List<Text> textList = this.getLines(width, text);
		if (textList.isEmpty()) {
			return FontBounds.empty();
		}

		double ox = x;
		double oy = y;

		if (text.getHorizontalAlignment().isCenter()) {
			ox = x + width / 2;
		} else if (text.getHorizontalAlignment().isEnd()) {
			ox = x + width;
		}

		if (text.getVerticalAlignment().isCenter()) {
			oy = y + height / 2;
			for (final Text line : textList) {
				oy -= line.dh(2);
			}
		} else if (text.getVerticalAlignment().isEnd()) {
			oy = y + height;
			for (final Text line : textList) {
				oy -= line.getHeight();
			}
		}

		double heightSum = 0;
		for (final Text line : textList) {
			if (mode == TextMode.BOX && (oy + line.getHeight() > y + height || oy < y)) {
				oy += line.getHeight();
				continue;
			}

			this.drawText(ox, oy, line.copyWithVerticalAlign(Align.START));
			oy += line.getHeight();
			heightSum += line.getHeight();
		}

		return new FontBounds(width, heightSum);
	}

	public FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign) {
		return this.drawText(x, y, Text.create(text, info).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign));
	}

	public FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign, final @NonNull TextOverflow overflow, final @NonNull TextMode mode) {
		return this.drawText(x, y, width, height, Text.create(text, info).horizontalAlign(horizontalAlign).verticalAlign(verticalAlign).overflow(overflow), mode);
	}

	public @NonNull List<@NonNull Text> getLines(final double width, final @NonNull Text text) {
		final List<Text> textList = new LinkedList<>();

		Text currentText = text.copyProperties().modifier(null);
		for (final TextElement element : text.getElementList()) {
			final String elementText = text.getText(element).replace("<br>", String.valueOf('\n'));
			final int[] tags = DrawText.tags(element, elementText);
			String opened = "";
			int lastSplit = 0;
			for (int i = 0; i < elementText.length(); i++) {
				if (tags[i] != -1) {
					continue;
				}

				final char c = elementText.charAt(i);
				if (c == '\n' || c == '\r') {
					currentText.add(element.copyWithText(opened + elementText.substring(lastSplit, i)).modifier(null));
					textList.add(currentText);
					currentText = text.copyProperties().modifier(null).add(element.copyWithText("").modifier(null));
					if (elementText.startsWith("\r\n", i)) {
						i++;
					}
					lastSplit = i + 1;
					opened = DrawText.opened(elementText, tags, lastSplit);
					continue;
				}

				final String part = opened + elementText.substring(lastSplit, i + 1);
				final double elementWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(part));
				if (currentText.getWidth() + elementWidth > width) {
					int foundSplit = i;
					for (int j = i; j >= lastSplit; j--) {
						if (elementText.charAt(j) == ' ' && tags[j] == -1) {
							foundSplit = j;
							break;
						}
					}

					if (foundSplit > lastSplit || !currentText.getText().isEmpty()) {
						currentText.add(element.copyWithText(opened + elementText.substring(lastSplit, foundSplit)).modifier(null));
						textList.add(currentText);
						currentText = text.copyProperties().modifier(null);
						lastSplit = elementText.charAt(foundSplit) == ' ' ? foundSplit + 1 : foundSplit;
						opened = DrawText.opened(elementText, tags, lastSplit);
					}
				}
			}

			if (lastSplit < elementText.length()) {
				currentText.add(element.copyWithText(opened + elementText.substring(lastSplit)).modifier(null));
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
		final int[] tags = new int[text.length()];
		Arrays.fill(tags, -1);

		final List<ITextMarkup> markups = element.getInfo().getMarkups();
		if (markups.isEmpty()) {
			return tags;
		}

		final TextStyle style = element.getInfo().getStyle().derive();
		for (int index = 0; index < text.length();) {
			final int consumed = TextMarkup.parse(markups, text, index, style);
			if (consumed > 0) {
				Arrays.fill(tags, index, index + consumed, index);
				index += consumed;
				continue;
			}

			index += Character.charCount(text.codePointAt(index));
		}
		return tags;
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