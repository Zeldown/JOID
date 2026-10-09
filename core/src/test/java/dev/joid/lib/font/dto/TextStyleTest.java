package dev.joid.lib.font.dto;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.dto.effect.ITextEffect;

public class TextStyleTest {

	private static final ITextEffect FIRST  = new ITextEffect() {};
	private static final ITextEffect SECOND = new ITextEffect() {};

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
	public void carriesAFontFromItsBaseToItsCopies() {
		final IFont first = () -> null;
		final IFont second = () -> null;
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).font(first).derive();
		Assert.assertSame(first, style.getFont());
		final TextStyle snapshot = style.font(second).copy();
		Assert.assertSame(second, snapshot.getFont());
		Assert.assertSame(first, style.reset().getFont());
		Assert.assertNull(TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).getFont());
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
	public void keepsEachEffectOnce() {
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE);
		style.effect(TextStyleTest.FIRST).effect(TextStyleTest.SECOND).effect(TextStyleTest.FIRST);
		Assert.assertEquals(Arrays.asList(TextStyleTest.FIRST, TextStyleTest.SECOND), style.getEffects());
	}

	@Test
	public void isItsOwnBaseAtTheRoot() {
		final TextStyle style = TextStyle.create(FontWeight.BOLD, false, Color.WHITE, TextStyleTest.FIRST);
		Assert.assertSame(style, style.getBase());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), style.reset().getEffects());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyEffects() {
		TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).getEffects().add(TextStyleTest.FIRST);
	}

	@Test
	public void removesAnEffect() {
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE, TextStyleTest.FIRST, TextStyleTest.SECOND);
		Assert.assertEquals(Collections.singletonList(TextStyleTest.SECOND), style.removeEffect(TextStyleTest.FIRST).getEffects());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.SECOND), style.removeEffect(TextStyleTest.FIRST).getEffects());
	}

	@Test
	public void keepsItsOwnEffects() {
		final ITextEffect[] effects = {TextStyleTest.FIRST};
		final TextStyle style = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE, effects);
		effects[0] = TextStyleTest.SECOND;
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), style.getEffects());
	}

	@Test
	public void leavesItsBaseUntouched() {
		final TextStyle base = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE, TextStyleTest.FIRST);
		base.derive().weight(FontWeight.BOLD).italic(true).color(Color.RED).effect(TextStyleTest.SECOND).removeEffect(TextStyleTest.FIRST);
		Assert.assertSame(FontWeight.REGULAR, base.getWeight());
		Assert.assertFalse(base.isItalic());
		Assert.assertSame(Color.WHITE, base.getColor());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), base.getEffects());
	}

	@Test
	public void resetsToItsClosestBase() {
		final TextStyle parent = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).derive().weight(FontWeight.BOLD).italic(true);
		final TextStyle child = parent.derive().weight(FontWeight.BLACK).italic(false).reset();
		Assert.assertSame(parent, child.getBase());
		Assert.assertSame(FontWeight.BOLD, child.getWeight());
		Assert.assertTrue(child.isItalic());
	}

	@Test
	public void copiesItsStyle() {
		final TextStyle copy = TextStyle.create(FontWeight.LIGHT, true, Color.RED, TextStyleTest.FIRST).copy();
		Assert.assertSame(FontWeight.LIGHT, copy.getWeight());
		Assert.assertTrue(copy.isItalic());
		Assert.assertSame(Color.RED, copy.getColor());
		Assert.assertEquals(Collections.singletonList(TextStyleTest.FIRST), copy.getEffects());
		Assert.assertSame(copy, copy.getBase());
	}

}