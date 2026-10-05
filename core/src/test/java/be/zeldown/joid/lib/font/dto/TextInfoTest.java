package be.zeldown.joid.lib.font.dto;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.IFont;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.markup.ITextMarkup;
import be.zeldown.joid.lib.font.dto.markup.TextMarkup;

public class TextInfoTest {

	private static final IFont       FONT   = () -> null;
	private static final ITextEffect EFFECT = new ITextEffect() {};
	private static final ITextMarkup MARKUP = (text, index, style) -> 0;

	@Test
	public void copiesEveryStyle() {
		final TextInfo copy = TextInfo.create(TextInfoTest.FONT, 20F).weight(FontWeight.LIGHT).markups(TextInfoTest.MARKUP).effects(TextInfoTest.EFFECT).copy();
		Assert.assertSame(FontWeight.LIGHT, copy.getWeight());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.MARKUP), copy.getMarkups());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.EFFECT), copy.getStyle().getEffects());
	}

	@Test
	public void buildsTheBaseStyle() {
		final TextStyle style = TextInfo.create(TextInfoTest.FONT, 20F, Color.RED).weight(FontWeight.BOLD).italic(true).effects(TextInfoTest.EFFECT).getStyle();
		Assert.assertSame(FontWeight.BOLD, style.getWeight());
		Assert.assertTrue(style.isItalic());
		Assert.assertSame(Color.RED, style.getColor());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.EFFECT), style.getEffects());
	}

	@Test
	public void startsRegularWithoutEffect() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F);
		Assert.assertSame(FontWeight.REGULAR, info.getWeight());
		Assert.assertTrue(info.getStyle().getEffects().isEmpty());
	}

	@Test
	public void restrictsTheMarkupsOnDemand() {
		TextMarkup.register(TextInfoTest.MARKUP);
		try {
			Assert.assertTrue(TextInfo.create(TextInfoTest.FONT, 20F).markups().getMarkups().isEmpty());
		} finally {
			TextMarkup.unregister(TextInfoTest.MARKUP);
		}
	}

	@Test
	public void followsTheRegisteredMarkupsByDefault() {
		TextMarkup.register(TextInfoTest.MARKUP);
		try {
			Assert.assertTrue(TextInfo.create(TextInfoTest.FONT, 20F).getMarkups().contains(TextInfoTest.MARKUP));
		} finally {
			TextMarkup.unregister(TextInfoTest.MARKUP);
		}
	}

}