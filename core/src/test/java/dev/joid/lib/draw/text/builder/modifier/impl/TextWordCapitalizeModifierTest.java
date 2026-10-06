package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextWordCapitalizeModifierTest {

	@Test
	public void raisesTheFirstLetterOfEachWord() {
		Assert.assertEquals("Hello Big World", TextModifier.WORD_CAPITALIZE.modify("hello big world"));
	}

	@Test
	public void keepsTheOtherLettersAsTheyAre() {
		Assert.assertEquals("Hello WORLD", TextModifier.WORD_CAPITALIZE.modify("hello wORLD"));
	}

}