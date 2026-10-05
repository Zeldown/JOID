package be.zeldown.joid.lib.font.dto;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;

public class TextStyleTest {

	private static final ITextEffect FIRST  = new ITextEffect() {};
	private static final ITextEffect SECOND = new ITextEffect() {};

	@Test
	public void derivesFromItsBase() {
		final TextStyle base = TextStyle.create(FontWeight.LIGHT, true, Color.RED, TextStyleTest.FIRST);
		final TextStyle style = base.derive();
		Assert.assertSame(base, style.getBase());
		Assert.assertSame(FontWeight.LIGHT, style.getWeight());
		Assert.assertTrue(style.isItalic());
		Assert.assertSame(Color.RED, style.getColor());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), style.getEffects());
	}

	@Test
	public void resetsToItsBase() {
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE, TextStyleTest.FIRST).derive();
		style.weight(FontWeight.BLACK).italic(true).color(Color.RED).effect(TextStyleTest.SECOND).removeEffect(TextStyleTest.FIRST).reset();
		Assert.assertSame(FontWeight.REGULAR, style.getWeight());
		Assert.assertFalse(style.isItalic());
		Assert.assertSame(Color.WHITE, style.getColor());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), style.getEffects());
	}

	@Test
	public void isItsOwnBaseAtTheRoot() {
		final TextStyle style = TextStyle.create(FontWeight.BOLD, false, Color.WHITE, TextStyleTest.FIRST);
		Assert.assertSame(style, style.getBase());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), style.reset().getEffects());
	}

	@Test
	public void copiesASnapshot() {
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).derive();
		final TextStyle snapshot = style.copy();
		style.weight(FontWeight.BOLD).effect(TextStyleTest.FIRST);
		Assert.assertSame(FontWeight.REGULAR, snapshot.getWeight());
		Assert.assertTrue(snapshot.getEffects().isEmpty());
		Assert.assertSame(style.getBase(), snapshot.getBase());
	}

	@Test
	public void keepsEachEffectOnce() {
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE);
		style.effect(TextStyleTest.FIRST).effect(TextStyleTest.SECOND).effect(TextStyleTest.FIRST);
		Assert.assertEquals(Arrays.asList(TextStyleTest.FIRST, TextStyleTest.SECOND), style.getEffects());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyEffects() {
		TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).getEffects().add(TextStyleTest.FIRST);
	}

}