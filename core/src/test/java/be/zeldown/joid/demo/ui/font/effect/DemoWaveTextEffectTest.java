package be.zeldown.joid.demo.ui.font.effect;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextStyle;
import be.zeldown.joid.lib.font.impl.glyph.dto.TextGlyph;

public class DemoWaveTextEffectTest {

	@Test
	public void movesTheGlyphVerticallyOnly() {
		for (int index = 0; index < 20; index++) {
			final TextGlyph<DemoFace> glyph = TextGlyph.create(DemoFace.create(0.5F), index, 'A', TextStyle.create(FontWeight.REGULAR, false, Color.WHITE), 0D, 0D, 40D, 20D, Color.WHITE);
			DemoWaveTextEffect.inst().apply(glyph);
			Assert.assertEquals(0D, glyph.getOffsetX(), 0D);
			Assert.assertTrue(Math.abs(glyph.getOffsetY()) <= 5D);
		}
	}

}