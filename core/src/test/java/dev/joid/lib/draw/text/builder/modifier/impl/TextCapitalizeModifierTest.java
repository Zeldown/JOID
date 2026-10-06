package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextCapitalizeModifierTest {

	@Test
	public void raisesOnlyTheFirstLetter() {
		Assert.assertEquals("Hello world", TextModifier.CAPITALIZE.modify("hello world"));
	}

	@Test
	public void keepsTheOtherLettersAsTheyAre() {
		Assert.assertEquals("HELLO", TextModifier.CAPITALIZE.modify("hELLO"));
	}

	@Test
	public void keepsAnEmptyText() {
		Assert.assertEquals("", TextModifier.CAPITALIZE.modify(""));
	}

}