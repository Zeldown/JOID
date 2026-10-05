package dev.joid.demo.ui.font.effect;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoFace implements IFontFace {

	private final float advance;

	private int wide = -1;

	public static @NonNull DemoFace create(final float advance) {
		return new DemoFace(advance);
	}

	@Override
	public boolean isItalic() {
		return false;
	}

	@Override
	public @NonNull String getName() {
		return "Demo";
	}

	@Override
	public float getAscender() {
		return 0.8F;
	}

	@Override
	public float getDescender() {
		return -0.2F;
	}

	@Override
	public float getLineHeight() {
		return 1.2F;
	}

	@Override
	public float getUnderlineY() {
		return -0.1F;
	}

	@Override
	public float getUnderlineThickness() {
		return 0.05F;
	}

	@Override
	public @NonNull FontWeight getWeight() {
		return FontWeight.REGULAR;
	}

	@Override
	public boolean hasGlyph(final int codepoint) {
		return true;
	}

	@Override
	public float getAdvance(final int codepoint) {
		return codepoint == this.wide ? this.advance * 3F : this.advance;
	}

	public @NonNull DemoFace wide(final int wide) {
		this.wide = wide;
		return this;
	}

	@Override
	public float getKerning(final int previous, final int current) {
		return 0F;
	}

}