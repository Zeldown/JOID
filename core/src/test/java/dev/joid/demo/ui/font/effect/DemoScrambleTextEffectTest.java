package dev.joid.demo.ui.font.effect;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextStyle;
import dev.joid.lib.font.impl.glyph.dto.TextGlyph;

public class DemoScrambleTextEffectTest {

	@Test
	public void leavesSpacesAlone() {
		final TextGlyph<DemoFace> glyph = DemoScrambleTextEffectTest.glyph(' ', DemoFace.create(0.5F));
		DemoScrambleTextEffect.inst().apply(glyph);
		Assert.assertEquals(' ', glyph.getCodepoint());
	}

	@Test
	public void swapsForACharacterOfTheSameWidth() {
		final TextGlyph<DemoFace> glyph = DemoScrambleTextEffectTest.glyph('A', DemoFace.create(0.5F));
		DemoScrambleTextEffect.inst().apply(glyph);
		Assert.assertTrue("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(glyph.getCodepoint()) >= 0);
	}

	@Test
	public void keepsTheCharacterWithoutAnEqualWidth() {
		final TextGlyph<DemoFace> glyph = DemoScrambleTextEffectTest.glyph('@', DemoFace.create(0.5F).wide('@'));
		DemoScrambleTextEffect.inst().apply(glyph);
		Assert.assertEquals('@', glyph.getCodepoint());
	}

	private static TextGlyph<DemoFace> glyph(final int codepoint, final DemoFace face) {
		return TextGlyph.create(face, 3, codepoint, TextStyle.create(FontWeight.REGULAR, false, Color.WHITE), 0D, 0D, 40D, 20D, Color.WHITE);
	}

}