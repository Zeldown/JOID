package be.zeldown.joid.lib.font.dto.effect;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextStyle;
import be.zeldown.joid.lib.font.impl.glyph.dto.IFontFace;
import be.zeldown.joid.lib.font.impl.glyph.dto.TextGlyph;

public class ITextEffectTest {

	@Test
	public void leavesTheGlyphUntouchedByDefault() {
		final TextGlyph<IFontFace> glyph = TextGlyph.create(new Face(), 0, 'A', TextStyle.create(FontWeight.REGULAR, false, Color.WHITE), 1D, 2D, 10D, 5D, Color.WHITE);
		final ITextEffect effect = new ITextEffect() {};
		effect.apply(glyph);
		effect.background(glyph);
		effect.decorate(glyph);
		Assert.assertEquals('A', glyph.getCodepoint());
		Assert.assertSame(Color.WHITE, glyph.getColor());
		Assert.assertEquals(0D, glyph.getOffsetX(), 0D);
		Assert.assertEquals(0D, glyph.getOffsetY(), 0D);
	}

	private static final class Face implements IFontFace {

		@Override
		public boolean isItalic() {
			return false;
		}

		@Override
		public float getAscender() {
			return 0F;
		}

		@Override
		public float getDescender() {
			return 0F;
		}

		@Override
		public float getLineHeight() {
			return 0F;
		}

		@Override
		public float getUnderlineY() {
			return 0F;
		}

		@Override
		public FontWeight getWeight() {
			return FontWeight.REGULAR;
		}

		@Override
		public float getUnderlineThickness() {
			return 0F;
		}

		@Override
		public boolean hasGlyph(final int codepoint) {
			return true;
		}

		@Override
		public float getAdvance(final int codepoint) {
			return 0.5F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return 0F;
		}

	}

}