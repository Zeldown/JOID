package be.zeldown.joid.lib.font;

import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import lombok.NonNull;

public interface FontProvider {

	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info);

	public double getWidth(final @NonNull String text, final @NonNull TextInfo info);

	public double getHeight(final @NonNull String text, final @NonNull TextInfo info);

	public double getLineHeight(final @NonNull TextInfo info);

}