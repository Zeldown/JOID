package dev.joid.lib.font;

import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import lombok.NonNull;

public interface IFontProvider {

	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info);

	public default @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		return this.drawText(x, y, text, info);
	}

	public double getLineHeight(final @NonNull TextInfo info);
	public double getWidth(final @NonNull String text, final @NonNull TextInfo info);
	public double getHeight(final @NonNull String text, final @NonNull TextInfo info);

}