package be.zeldown.joid.lib.font;

import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import lombok.NonNull;

public interface FontProvider {

	/**
	 * Draws the specified text at the given coordinates using the provided text information.
	 *
	 * @param x The x-coordinate of the starting position for drawing the text.
	 * @param y The y-coordinate of the starting position for drawing the text.
	 * @param text The text to be drawn.
	 * @param info The information about the text, such as font size, style, etc.
	 * @return The bounds of the drawn text.
	 * @throws NullPointerException If either the text or text information is {@code null}.
	 */
	public FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info);

	/**
	 * Gets the width of the specified text using the provided text information.
	 *
	 * @param text The text for which to calculate the width.
	 * @param info The information about the text, such as font size, style, etc.
	 * @return The width of the text in pixels.
	 * @throws NullPointerException If either the text or text information is {@code null}.
	 */
	public double getWidth(final @NonNull String text, final @NonNull TextInfo info);

	/**
	 * Gets the height of the specified text using the provided text information.
	 *
	 * @param text The text for which to calculate the height.
	 * @param info The information about the text, such as font size, style, etc.
	 * @return The height of the text in pixels.
	 * @throws NullPointerException If either the text or text information is {@code null}.
	 */
	public double getHeight(final @NonNull String text, final @NonNull TextInfo info);

	/**
	 * Gets the height of a line of text using the provided text information.
	 *
	 * @param info The information about the text, such as font size, style, etc.
	 * @return The height of a line of text in pixels.
	 * @throws NullPointerException If the text information is {@code null}.
	 */
	public double getLineHeight(final @NonNull TextInfo info);

}