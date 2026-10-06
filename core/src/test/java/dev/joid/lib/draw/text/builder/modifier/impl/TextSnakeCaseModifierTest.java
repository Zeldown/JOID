package dev.joid.lib.draw.text.builder.modifier.impl;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextSnakeCaseModifierTest {

	@Test
	public void joinsTheWordsWithUnderscores() {
		Assert.assertEquals("hello_big_world", TextModifier.SNAKE_CASE.modify("Hello Big World"));
	}

}