package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextUpperCamelCaseModifierTest {

	@Test
	public void raisesTheFirstLetterAndLowersTheRest() {
		Assert.assertEquals("Hello", TextModifier.UPPER_CAMEL_CASE.modify("hELLO"));
	}

	@Test
	public void joinsTheWordsInUpperCamelCase() {
		Assert.assertEquals("HelloBigWorld", TextModifier.UPPER_CAMEL_CASE.modify("hello big world"));
	}

}