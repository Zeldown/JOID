package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextUpperCaseModifierTest {

	@Test
	public void raisesEveryLetter() {
		Assert.assertEquals("HELLO WORLD 42", TextModifier.UPPER_CASE.modify("Hello world 42"));
	}

}