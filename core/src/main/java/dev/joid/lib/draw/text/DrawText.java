package dev.joid.lib.draw.text;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.TextElement;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
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

				return info.getFont().getFontProvider().drawText(pen, oy, drawText, info, runX, runY, runWidth, runHeight).getWidth();
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
					final double overflowStrWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(overflowStr));
					for (int i = 0; i <= elementText.length(); i++) {
						final String subText = elementText.substring(0, i);
						final double subTextWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(subText));
						if (overflowWidth + subTextWidth + overflowStrWidth > width && !subText.isEmpty()) {
							overflowText.add(element.copyWithText(subText.substring(0, subText.length() - 1)).modifier(null));
							overflow = true;
							break;
						}
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

		if (mode == TextMode.SPLIT || mode == TextMode.BOX) {
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

		return null;
	}

	public FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign) {
		return this.drawText(x, y, Text.create(text, info).align(horizontalAlign, verticalAlign));
	}

	public FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull String text, final @NonNull TextInfo info, final @NonNull Align horizontalAlign, final @NonNull Align verticalAlign, final @NonNull TextOverflow overflow, final @NonNull TextMode mode) {
		return this.drawText(x, y, width, height, Text.create(text, info).align(horizontalAlign, verticalAlign).overflow(overflow), mode);
	}

	public @NonNull List<@NonNull Text> getLines(final double width, final @NonNull Text text) {
		final List<Text> textList = new LinkedList<>();

		Text currentText = text.copyProperties().modifier(null);
		for (final TextElement element : text.getElementList()) {
			final String elementText = text.getText(element).replace("<br>", String.valueOf('\n'));
			int lastSplit = 0;
			for (int i = 0; i < elementText.length(); i++) {
				final char c = elementText.charAt(i);
				if (c == '\n' || c == '\r') {
					currentText.add(element.copyWithText(elementText.substring(lastSplit, i)).modifier(null));
					textList.add(currentText);
					currentText = text.copyProperties().modifier(null).add(element.copyWithText("").modifier(null));
					if (elementText.startsWith("\r\n", i)) {
						i++;
					}
					lastSplit = i + 1;
					continue;
				}

				final String part = elementText.substring(lastSplit, i + 1);
				final double elementWidth = FontUsage.trace(element.getOrigin(), () -> element.getInfo().getWidth(part));
				if (currentText.getWidth() + elementWidth > width) {
					int foundSplit = i;
					for (int j = i; j >= lastSplit; j--) {
						if (elementText.charAt(j) == ' ') {
							foundSplit = j;
							break;
						}
					}

					if (foundSplit > lastSplit || !currentText.getText().isEmpty()) {
						currentText.add(element.copyWithText(elementText.substring(lastSplit, foundSplit)).modifier(null));
						textList.add(currentText);
						currentText = text.copyProperties().modifier(null);
						lastSplit = elementText.charAt(foundSplit) == ' ' ? foundSplit + 1 : foundSplit;
					}
				}
			}

			if (lastSplit < elementText.length()) {
				currentText.add(element.copyWithText(elementText.substring(lastSplit)).modifier(null));
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

}