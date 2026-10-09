package dev.joid.demo.ui.font.effect;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextStyle;
import dev.joid.lib.font.impl.glyph.SingleFaceFont;
import dev.joid.lib.font.impl.glyph.TextGlyph;

public class DemoRainbowTextEffectTest {

	@Test
	public void colorsTheGlyphAndKeepsItsAlpha() {
		final Color base = Color.WHITE.copyAlpha(0.5F);
		final DemoFace face = DemoFace.create(0.5F);
		final TextGlyph<DemoFace> glyph = TextGlyph.create(SingleFaceFont.of(face), face, 7, 'A', TextStyle.create(FontWeight.REGULAR, false, base), 0D, 0D, 40D, 20D, base);
		DemoRainbowTextEffect.inst().apply(glyph);
		Assert.assertNotSame(base, glyph.getColor());
		Assert.assertEquals(0.5F, glyph.getColor().a, 0F);
		Assert.assertFalse(glyph.getColor().r == 1F && glyph.getColor().g == 1F && glyph.getColor().b == 1F);
	}

}