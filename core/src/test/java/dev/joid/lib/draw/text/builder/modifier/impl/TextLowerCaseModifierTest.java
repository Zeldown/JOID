package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextLowerCaseModifierTest {

	@Test
	public void lowersEveryLetter() {
		Assert.assertEquals("hello world 42", TextModifier.LOWER_CASE.modify("Hello WORLD 42"));
	}

}