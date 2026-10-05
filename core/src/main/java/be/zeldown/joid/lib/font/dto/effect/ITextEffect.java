package be.zeldown.joid.lib.font.dto.effect;

import lombok.NonNull;

public interface ITextEffect {

	public default void apply(final @NonNull ITextGlyph glyph) {}

	public default void decorate(final @NonNull ITextGlyph glyph) {}

	public default void background(final @NonNull ITextGlyph glyph) {}

}