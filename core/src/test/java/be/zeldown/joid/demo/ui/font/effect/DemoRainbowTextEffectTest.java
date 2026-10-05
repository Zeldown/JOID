package be.zeldown.joid.demo.ui.font.effect;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextStyle;
import be.zeldown.joid.lib.font.impl.glyph.dto.TextGlyph;

public class DemoRainbowTextEffectTest {

	@Test
	public void colorsTheGlyphAndKeepsItsAlpha() {
		final Color base = Color.WHITE.copyAlpha(0.5F);
		final TextGlyph<DemoFace> glyph = TextGlyph.create(DemoFace.create(0.5F), 7, 'A', TextStyle.create(FontWeight.REGULAR, false, base), 0D, 0D, 40D, 20D, base);
		DemoRainbowTextEffect.inst().apply(glyph);
		Assert.assertNotSame(base, glyph.getColor());
		Assert.assertEquals(0.5F, glyph.getColor().a, 0F);
		Assert.assertFalse(glyph.getColor().r == 1F && glyph.getColor().g == 1F && glyph.getColor().b == 1F);
	}

}