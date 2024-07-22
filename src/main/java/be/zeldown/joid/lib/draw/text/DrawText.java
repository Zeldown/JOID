package be.zeldown.joid.lib.draw.text;

import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.font.dto.text.TextOverflow;
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

	/**
	 * Draws the specified text at the given position with the provided text information.
	 *
	 * @param x     The x-coordinate of the starting position for the text.
	 * @param y     The y-coordinate of the starting position for the text.
	 * @param text  The text to be drawn. Must not be null.
	 * @param info  The text information, including font style and color. Must not be null.
	 * @return      The bounds of the drawn text.
	 * @throws NullPointerException if text or info is null.
	 */
	public FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
		switch (info.getAlign()) {
		case CENTER:
			return info.getFont().getFontProvider().drawText(x - info.getFont().getFontProvider().getWidth(text, info) / 2, y, text, info);
		case RIGHT:
			return info.getFont().getFontProvider().drawText(x - info.getFont().getFontProvider().getWidth(text, info), y, text, info);
		default:
			break;
		}

		return info.getFont().getFontProvider().drawText(x, y, text, info);
	}

	/**
	 * Draws the specified text at the given position with a maximum width, breaking lines as needed.
	 *
	 * @param x         The x-coordinate of the starting position for the text.
	 * @param y         The y-coordinate of the starting position for the text.
	 * @param maxWidth  The maximum width for each line of text.
	 * @param text      The text to be drawn. Must not be null.
	 * @param info      The text information, including font style and color. Must not be null.
	 * @return          The bounds of the drawn text.
	 * @throws NullPointerException if text or info is null.
	 */
	public FontBounds drawText(final double x, final double y, final double maxWidth, final @NonNull String text, final @NonNull TextInfo info) {
		final List<String> lines = this.getLines(maxWidth, text, info);

		double oy = y;
		for (final String line : lines) {
			this.drawText(x, oy, line, info);
			oy += this.getLineHeight(info);
		}

		return new FontBounds(maxWidth, lines.size() <= 1 ? this.getHeight(text, info) : oy - y);
	}

	/**
	 * Draws the specified text at the given position with a maximum width, overflow is replaced with the textOverflow.
	 *
	 * @param x             The x-coordinate of the starting position for the text.
	 * @param y             The y-coordinate of the starting position for the text.
	 * @param maxWidth      The maximum width for each line of text.
	 * @param text          The text to be drawn. Must not be null.
	 * @param info          The text information, including font style and color. Must not be null.
	 * @param textOverflow  The text to be drawn when the text overflows. Must not be null.
	 * @return              The bounds of the drawn text.
	 * @throws NullPointerException if text or info or textOverflow is null.
	 */
	public FontBounds drawText(final double x, final double y, final double maxWidth, final @NonNull String text, final @NonNull TextInfo info, final TextOverflow textOverflow) {
		if (text.isEmpty()) {
			return this.drawText(x, y, text, info);
		}

		String drawText = text;
		boolean overflow = false;
		for (int i = 0; i <= text.length(); i++) {
			final String subText = text.substring(0, i);
			final double subTextWidth = DrawUtils.TEXT.getWidth(subText, info);
			if (subTextWidth > maxWidth) {
				drawText = subText.substring(0, subText.length() - 1);
				overflow = true;
				break;
			}
		}

		if (overflow && textOverflow != null) {
			final String overflowText = textOverflow.getOverflow();
			for (int i = drawText.length(); i >= 0; i--) {
				final String subText = drawText.substring(0, i) + overflowText;
				final double subTextWidth = DrawUtils.TEXT.getWidth(subText, info);
				if (subTextWidth <= maxWidth) {
					drawText = subText;
					break;
				}
			}
		}

		return this.drawText(x, y, drawText, info);
	}

	/**
	 * Splits the input text into lines that fit within the specified maximum width.
	 *
	 * @param maxWidth  The maximum width for each line of text.
	 * @param text      The text to be split into lines. Must not be null.
	 * @param info      The text information, including font style and color. Must not be null.
	 * @return          A list of lines that fit within the maximum width.
	 * @throws NullPointerException if text or info is null.
	 */
	public List<String> getLines(final double maxWidth, final @NonNull String text, final @NonNull TextInfo info) {
		final List<String> texts = new ArrayList<>();

		int lastSplit = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				final int foundSplit = i;
				texts.add(text.substring(lastSplit, foundSplit));
				lastSplit = foundSplit;
			}

			final double width = this.getWidth(text.substring(lastSplit, i + 1), info);
			if (width >= maxWidth) {
				int foundSplit = i;
				for (int j = i - 1; j >= lastSplit; j--) {
					if (text.charAt(j) == ' ') {
						foundSplit = Math.min(text.length(), j + 1);
						break;
					}
				}

				texts.add(text.substring(lastSplit, foundSplit));
				lastSplit = foundSplit;
			}
		}

		texts.add(text.substring(lastSplit));
		return texts;
	}

	/**
	 * Gets the width of the specified text with the given text information.
	 *
	 * @param text  The text for which to calculate the width. Must not be null.
	 * @param info  The text information, including font style and color. Must not be null.
	 * @return      The width of the text.
	 * @throws NullPointerException if text or info is null.
	 */
	public double getWidth(final @NonNull String text, final @NonNull TextInfo info) {
		return info.getFont().getFontProvider().getWidth(text, info);
	}

	/**
	 * Gets the height of the specified text with the given text information.
	 *
	 * @param text  The text for which to calculate the height. Must not be null.
	 * @param info  The text information, including font style and color. Must not be null.
	 * @return      The height of the text.
	 * @throws NullPointerException if text or info is null.
	 */
	public double getHeight(final @NonNull String text, final @NonNull TextInfo info) {
		return info.getFont().getFontProvider().getHeight(text, info);
	}

	/**
	 * Gets the line height for the specified text information.
	 *
	 * @param info  The text information, including font style and color. Must not be null.
	 * @return      The line height.
	 * @throws NullPointerException if info is null.
	 */
	public double getLineHeight(final @NonNull TextInfo info) {
		return info.getFont().getFontProvider().getLineHeight(info);
	}

}