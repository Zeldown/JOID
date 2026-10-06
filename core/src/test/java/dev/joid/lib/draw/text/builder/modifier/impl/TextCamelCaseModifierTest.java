package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextCamelCaseModifierTest {

	@Test
	public void joinsTheWordsInCamelCase() {
		Assert.assertEquals("helloBigWorld", TextModifier.CAMEL_CASE.modify("hello big world"));
	}

	@Test
	public void lowersTheOtherLetters() {
		Assert.assertEquals("helloWorld", TextModifier.CAMEL_CASE.modify("HELLO WORLD"));
	}

	@Test
	public void splitsOnEverySeparator() {
		Assert.assertEquals("helloBigWorldAgain", TextModifier.CAMEL_CASE.modify("hello_big-world.again"));
	}

	@Test
	public void keepsAnEmptyText() {
		Assert.assertEquals("", TextModifier.CAMEL_CASE.modify(""));
	}

	@Test
	public void startsInLowerCaseAfterALeadingSeparator() {
		Assert.assertEquals("helloWorld", TextModifier.CAMEL_CASE.modify(" hello world"));
	}

	@Test
	public void keepsTheAccentedLetters() {
		Assert.assertEquals("déjàVu", TextModifier.CAMEL_CASE.modify("déjà vu"));
	}

}