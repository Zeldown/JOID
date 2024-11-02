package be.zeldown.joid.lib.draw.text;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.draw.text.builder.TextElement;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public final class DrawText {

	@Getter private static DrawText instance;

	public DrawText() {
		if (DrawText.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawText.");
		}
		DrawText.instance = this;
	}

	/* [ Text Section ] */
	public @NonNull FontBounds drawText(double x, double y, final @NonNull Text text) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		if (text.getHorizontalAlignment().isCenter()) {
			x -= text.getWidth() / 2;
		} else if (text.getHorizontalAlignment().isEnd()) {
			x -= text.getWidth();
		}

		if (text.getVerticalAlignment().isCenter()) {
			y -= text.getHeight() / 2;
		} else if (text.getVerticalAlignment().isEnd()) {
			y -= text.getHeight();
		}

		for (final TextElement element : text.getElementList()) {
			final String drawText = text.getText(element);
			final TextInfo info = element.getInfo();

			double oy = y;
			if (text.getVerticalAlignment().isCenter()) {
				oy += (text.getHeight() - info.getHeight()) / 2;
			} else if (text.getVerticalAlignment().isEnd()) {
				oy += text.getHeight() - info.getHeight();
			}

			x += info.getFont().getFontProvider().drawText(x, oy, drawText, info).getWidth();
		}

		return text.getBounds();
	}

	public @NonNull FontBounds drawText(final double x, final double y, final double width, final double height, final @NonNull Text text, final @NonNull TextMode mode) {
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
				final Text overflowText = text.copyProperties();
				final String overflowStr = text.getOverflow() == null ? "" : text.getOverflow().getOverflow();

				boolean overflow = false;
				double overflowWidth = 0;
				for (final TextElement element : text.getElementList()) {
					final String elementText = text.getText(element);
					final double overflowStrWidth = element.getInfo().getWidth(overflowStr);
					for (int i = 0; i <= elementText.length(); i++) {
						final String subText = elementText.substring(0, i);
						final double subTextWidth = element.getInfo().getWidth(subText);
						if (overflowWidth + subTextWidth + overflowStrWidth > width && !subText.isEmpty()) {
							overflowText.add(element.copyWithText(subText.substring(0, subText.length() - 1)));
							overflow = true;
							break;
						}
					}

					if (overflow) {
						break;
					}

					overflowWidth += element.getInfo().getWidth(elementText);
					overflowText.add(element);
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

	public @NonNull List<@NonNull Text> getLines(final double width, final @NonNull Text text) {
		final List<Text> textList = new LinkedList<>();

		Text currentText = text.copyProperties();
		for (final TextElement element : text.getElementList()) {
			final String elementText = text.getText(element).replace("<br>", String.valueOf('\n'));
			int lastSplit = 0;
			for (int i = 0; i < elementText.length(); i++) {
				final char c = elementText.charAt(i);
				if (c == '\n' || c == '\r') {
					final int foundSplit = i;
					currentText.add(element.copyWithText(elementText.substring(lastSplit, foundSplit)));
					textList.add(currentText);
					currentText = text.copyProperties();
					lastSplit = foundSplit;
				}

				final double elementWidth = element.getInfo().getWidth(elementText.substring(lastSplit, i + 1));
				if (currentText.getWidth() + elementWidth >= width) {
					int foundSplit = i;
					for (int j = i - 1; j >= lastSplit; j--) {
						if (elementText.charAt(j) == ' ') {
							foundSplit = Math.min(elementText.length(), j + 1);
							break;
						}
					}

					currentText.add(element.copyWithText(elementText.substring(lastSplit, foundSplit)));
					textList.add(currentText);
					currentText = text.copyProperties();
					lastSplit = foundSplit;
				}

				if (i == elementText.length() - 1) {
					currentText.add(element.copyWithText(elementText.substring(lastSplit)));
				}
			}
		}

		if (!currentText.isEmpty()) {
			textList.add(currentText);
		}

		return textList;
	}

}